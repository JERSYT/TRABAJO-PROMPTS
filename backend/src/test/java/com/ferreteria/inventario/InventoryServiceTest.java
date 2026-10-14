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
import com.ferreteria.inventario.inventory.dto.request.InventarioInicialRequest;
import com.ferreteria.inventario.inventory.dto.response.InventarioSaldoResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InventoryServiceTest {

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

    private Referencia referenciaPrueba;
    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMINISTRADOR")
                .orElseGet(() -> rolRepository.save(new Rol(null, "ROLE_ADMINISTRADOR", "Admin")));

        usuarioPrueba = usuarioRepository.findByUsername("test_user_qa")
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setUsername("test_user_qa");
                    u.setPasswordHash("hash");
                    u.setNombreCompleto("Usuario QA Test");
                    u.setActivo(true);
                    u.setRoles(Set.of(rolAdmin));
                    return usuarioRepository.save(u);
                });

        Producto prod = productoRepository.findByNombre("Tornillo Hexagonal Test")
                .orElseGet(() -> productoRepository.save(new Producto(null, "Tornillo Hexagonal Test", "Tornillo de prueba", true)));
        Marca marca = marcaRepository.findByNombre("MarcaQA")
                .orElseGet(() -> marcaRepository.save(new Marca(null, "MarcaQA", true)));
        Presentacion pres = presentacionRepository.findByNombre("Caja x 50 Test")
                .orElseGet(() -> presentacionRepository.save(new Presentacion(null, "Caja x 50 Test", true)));
        UnidadMedida und = unidadMedidaRepository.findByCodigo("CJA")
                .orElseGet(() -> unidadMedidaRepository.save(new UnidadMedida(null, "CJA", "Caja")));

        referenciaPrueba = referenciaRepository.findByCodigoBarras("7709999990001")
                .orElseGet(() -> {
                    Referencia r = new Referencia();
                    r.setProducto(prod);
                    r.setMarca(marca);
                    r.setPresentacion(pres);
                    r.setUnidadMedida(und);
                    r.setCodigoBarras("7709999990001");
                    r.setPrecioVenta(new BigDecimal("25000.00"));
                    r.setActivo(true);
                    return referenciaRepository.save(r);
                });
    }

    @Test
    @DisplayName("Inicializar inventario debe registrar saldo y costo promedio ponderado inicial (RN-04)")
    void testInicializarInventario() {
        InventarioInicialRequest req = new InventarioInicialRequest();
        req.setReferenciaId(referenciaPrueba.getId());
        req.setCantidadInicial(new BigDecimal("10.000"));
        req.setCostoUnitario(new BigDecimal("12000.00"));

        InventarioSaldoResponse res = inventoryService.inicializarInventario(req, usuarioPrueba.getUsername());

        assertNotNull(res);
        assertEquals(0, new BigDecimal("10.000").compareTo(res.getCantidadDisponible()));
        assertEquals(0, new BigDecimal("12000.0000").compareTo(res.getCostoPromedioPonderado()));
    }

    @Test
    @DisplayName("Cálculo matemático exacto del Costo Promedio Ponderado tras compra sucesiva (RN-04)")
    void testRecalculoCostoPromedioPonderado() {
        // 1. Saldo actual: 10 unidades a $2.000 = $20.000 valor inventario
        InventarioSaldo saldo = saldoRepository.findByReferenciaId(referenciaPrueba.getId())
                .orElseGet(() -> new InventarioSaldo(referenciaPrueba, BigDecimal.ZERO, BigDecimal.ZERO));
        saldo.setCantidadDisponible(new BigDecimal("10.000"));
        saldo.setCostoPromedioPonderado(new BigDecimal("2000.0000"));
        saldoRepository.save(saldo);

        // 2. Compra nueva: 10 unidades a $3.000 = $30.000 valor compra
        // Nuevo CPP esperado: (20.000 + 30.000) / 20 = 50.000 / 20 = $2.500
        inventoryService.procesarEntradaCompra(
                referenciaPrueba.getId(),
                new BigDecimal("10.000"),
                new BigDecimal("3000.0000"),
                1L,
                usuarioPrueba
        );

        InventarioSaldo saldoActualizado = saldoRepository.findByReferenciaId(referenciaPrueba.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("20.000").compareTo(saldoActualizado.getCantidadDisponible()));
        assertEquals(0, new BigDecimal("2500.0000").compareTo(saldoActualizado.getCostoPromedioPonderado().setScale(4, RoundingMode.HALF_UP)));
    }

    @Test
    @DisplayName("Descontar existencias debe fallar con InsufficientStockException si supera el stock disponible (RN-01)")
    void testAntiStockNegativo() {
        InventarioSaldo saldo = saldoRepository.findByReferenciaId(referenciaPrueba.getId())
                .orElseGet(() -> new InventarioSaldo(referenciaPrueba, BigDecimal.ZERO, BigDecimal.ZERO));
        saldo.setCantidadDisponible(new BigDecimal("3.000"));
        saldo.setCostoPromedioPonderado(new BigDecimal("5000.0000"));
        saldoRepository.save(saldo);

        // Intentar descontar 5 unidades cuando solo hay 3 disponibles
        assertThrows(InsufficientStockException.class, () -> {
            inventoryService.descontarStockConBloqueo(
                    referenciaPrueba.getId(),
                    new BigDecimal("5.000"),
                    "VENTA",
                    1L,
                    "Venta de prueba",
                    usuarioPrueba
            );
        });

        // Verificar que el stock permanezca en 3 unidades y nunca negativo
        InventarioSaldo saldoFinal = saldoRepository.findByReferenciaId(referenciaPrueba.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("3.000").compareTo(saldoFinal.getCantidadDisponible()));
    }
}
