package com.ferreteria.inventario;

import com.ferreteria.inventario.catalog.entity.Marca;
import com.ferreteria.inventario.catalog.entity.Presentacion;
import com.ferreteria.inventario.catalog.entity.Producto;
import com.ferreteria.inventario.catalog.entity.Referencia;
import com.ferreteria.inventario.catalog.entity.UnidadMedida;
import com.ferreteria.inventario.catalog.repository.MarcaRepository;
import com.ferreteria.inventario.catalog.repository.PresentacionRepository;
import com.ferreteria.inventario.catalog.repository.ProductoRepository;
import com.ferreteria.inventario.catalog.repository.ReferenciaRepository;
import com.ferreteria.inventario.catalog.repository.UnidadMedidaRepository;
import com.ferreteria.inventario.inventory.entity.InventarioSaldo;
import com.ferreteria.inventario.inventory.repository.InventarioSaldoRepository;
import com.ferreteria.inventario.inventory.service.InventoryService;
import com.ferreteria.inventario.shared.exception.InsufficientStockException;
import com.ferreteria.inventario.user.entity.Rol;
import com.ferreteria.inventario.user.entity.Usuario;
import com.ferreteria.inventario.user.repository.RolRepository;
import com.ferreteria.inventario.user.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de concurrencia e integridad transaccional (RN-01):
 * Simula múltiples hilos ejecutando salidas simultáneas sobre un stock limitado
 * para comprobar que el bloqueo pesimista impida que el inventario quede negativo.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyInventoryTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventarioSaldoRepository saldoRepository;

    @Autowired
    private ReferenciaRepository referenciaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private PresentacionRepository presentacionRepository;

    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Referencia referenciaConcurrente;
    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMINISTRADOR")
                .orElseGet(() -> rolRepository.save(new Rol(null, "ROLE_ADMINISTRADOR", "Admin")));

        usuarioPrueba = usuarioRepository.findByUsername("user_concurrencia")
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setUsername("user_concurrencia");
                    u.setPasswordHash("hash");
                    u.setNombreCompleto("Usuario Concurrencia");
                    u.setActivo(true);
                    u.setRoles(Set.of(rolAdmin));
                    return usuarioRepository.save(u);
                });

        Producto prod = productoRepository.findByNombre("Cinta Concurrente")
                .orElseGet(() -> productoRepository.save(new Producto(null, "Cinta Concurrente", "Producto prueba", true)));
        Marca marca = marcaRepository.findByNombre("MarcaConc")
                .orElseGet(() -> marcaRepository.save(new Marca(null, "MarcaConc", true)));
        Presentacion pres = presentacionRepository.findByNombre("RolloConc")
                .orElseGet(() -> presentacionRepository.save(new Presentacion(null, "RolloConc", true)));
        UnidadMedida und = unidadMedidaRepository.findByCodigo("UND")
                .orElseGet(() -> unidadMedidaRepository.save(new UnidadMedida(null, "UND", "Unidad")));

        referenciaConcurrente = referenciaRepository.findByCodigoBarras("7709999990999")
                .orElseGet(() -> {
                    Referencia r = new Referencia();
                    r.setProducto(prod);
                    r.setMarca(marca);
                    r.setPresentacion(pres);
                    r.setUnidadMedida(und);
                    r.setCodigoBarras("7709999990999");
                    r.setPrecioVenta(new BigDecimal("10000.00"));
                    r.setActivo(true);
                    return referenciaRepository.save(r);
                });

        // Establecer un saldo exactamente de 5 unidades para la prueba
        InventarioSaldo saldo = saldoRepository.findByReferenciaId(referenciaConcurrente.getId())
                .orElseGet(() -> new InventarioSaldo(referenciaConcurrente, BigDecimal.ZERO, BigDecimal.ZERO));
        saldo.setCantidadDisponible(new BigDecimal("5.000"));
        saldo.setCostoPromedioPonderado(new BigDecimal("6000.0000"));
        saldoRepository.save(saldo);
    }

    @Test
    @DisplayName("Concurrencia: 10 hilos intentando descontar 1 unidad sobre un stock de 5 deben resultar en 5 éxitos, 5 rechazos y stock final en 0 (RN-01)")
    void testConcurrenciaVentasStockNegativo() throws InterruptedException {
        int totalHilos = 10;
        ExecutorService executor = Executors.newFixedThreadPool(totalHilos);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalHilos);

        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger fallosStock = new AtomicInteger(0);

        for (int i = 0; i < totalHilos; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // Esperar a que todos los hilos estén listos para disparar simultáneamente
                    inventoryService.descontarStockConBloqueo(
                            referenciaConcurrente.getId(),
                            new BigDecimal("1.000"),
                            "VENTA",
                            999L,
                            "Venta concurrente de prueba",
                            usuarioPrueba
                    );
                    exitos.incrementAndGet();
                } catch (InsufficientStockException e) {
                    fallosStock.incrementAndGet();
                } catch (Exception e) {
                    // Otras excepciones transaccionales
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown(); // Disparar todos los hilos concurrentes
        doneLatch.await(); // Esperar a que todos terminen
        executor.shutdown();

        // Verificar resultados de la contienda concurrente
        assertEquals(5, exitos.get(), "Exactamente 5 transacciones debieron completarse exitosamente");
        assertEquals(5, fallosStock.get(), "Exactamente 5 transacciones debieron ser rechazadas por falta de existencias");

        InventarioSaldo saldoFinal = saldoRepository.findByReferenciaId(referenciaConcurrente.getId()).orElseThrow();
        assertEquals(0, BigDecimal.ZERO.compareTo(saldoFinal.getCantidadDisponible()),
                "El saldo disponible final debe ser exactamente 0.000 y nunca un valor negativo");
    }
}
