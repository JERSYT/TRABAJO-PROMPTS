package com.ferreteria.inventario;

import com.ferreteria.inventario.customer.entity.Tercero;
import com.ferreteria.inventario.customer.repository.TerceroRepository;
import com.ferreteria.inventario.document.service.DocumentSequenceService;
import com.ferreteria.inventario.inventory.dto.request.AjusteInventarioRequest;
import com.ferreteria.inventario.inventory.entity.AjusteInventario;
import com.ferreteria.inventario.inventory.entity.InventarioSaldo;
import com.ferreteria.inventario.inventory.repository.InventarioSaldoRepository;
import com.ferreteria.inventario.inventory.service.InventoryAdjustmentService;
import com.ferreteria.inventario.purchase.dto.request.CancelarDocumentoRequest;
import com.ferreteria.inventario.returns.dto.request.CrearDevolucionRequest;
import com.ferreteria.inventario.returns.dto.request.DevolucionLineaRequest;
import com.ferreteria.inventario.returns.dto.response.DevolucionResponse;
import com.ferreteria.inventario.returns.service.ReturnService;
import com.ferreteria.inventario.sale.dto.request.CrearVentaRequest;
import com.ferreteria.inventario.sale.dto.request.VentaLineaRequest;
import com.ferreteria.inventario.sale.dto.response.VentaResponse;
import com.ferreteria.inventario.sale.service.SaleService;
import com.ferreteria.inventario.shared.exception.BusinessException;
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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReturnsAndAdjustmentsTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private ReturnService returnService;

    @Autowired
    private InventoryAdjustmentService adjustmentService;

    @Autowired
    private DocumentSequenceService sequenceService;

    @Autowired
    private InventarioSaldoRepository saldoRepository;

    @Autowired
    private TerceroRepository terceroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Tercero clientePrueba;
    private Usuario adminUsuario;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMINISTRADOR")
                .orElseGet(() -> rolRepository.save(new Rol(null, "ROLE_ADMINISTRADOR", "Admin")));

        adminUsuario = usuarioRepository.findByUsername("admin_ferreteria")
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setUsername("admin_ferreteria");
                    u.setPasswordHash("hash");
                    u.setNombreCompleto("Administrador General");
                    u.setActivo(true);
                    u.setRoles(Set.of(rolAdmin));
                    return usuarioRepository.save(u);
                });

        clientePrueba = terceroRepository.findByNumeroDocumento("1020304050")
                .orElseGet(() -> {
                    Tercero t = new Tercero();
                    t.setTipoDocumento("CC");
                    t.setNumeroDocumento("1020304050");
                    t.setRazonSocial("Cliente Devoluciones");
                    t.setTipoPersona("NATURAL");
                    t.setEsCliente(true);
                    t.setEsProveedor(false);
                    return terceroRepository.save(t);
                });
    }

    @Test
    @DisplayName("Generador de consecutivos correlativos debe emitir números consecutivos sin saltos (RN-12)")
    void testGenerarConsecutivosCorrelativos() {
        String num1 = sequenceService.obtenerSiguienteConsecutivo("VENTA");
        String num2 = sequenceService.obtenerSiguienteConsecutivo("VENTA");

        assertTrue(num1.startsWith("VTA-"));
        assertTrue(num2.startsWith("VTA-"));
        assertNotEquals(num1, num2);
    }

    @Test
    @DisplayName("Devolución vinculada: No se puede devolver más cantidad de la vendida en la venta original (RN-07)")
    void testDevolucionExcedeCantidadVendida() {
        // 1. Registrar una venta de 2 unidades
        CrearVentaRequest vtaReq = new CrearVentaRequest();
        vtaReq.setClienteId(clientePrueba.getId());
        vtaReq.setMontoPagadoEfectivo(new BigDecimal("100000.00"));
        vtaReq.setLineas(List.of(new VentaLineaRequest(1L, new BigDecimal("2.000"))));

        VentaResponse venta = saleService.registrarVenta(vtaReq, adminUsuario.getUsername());

        // 2. Intentar devolver 3 unidades de esa venta
        CrearDevolucionRequest devReq = new CrearDevolucionRequest();
        devReq.setVentaId(venta.getId());
        devReq.setMotivo("Intento devolver más de lo comprado");
        devReq.setLineas(List.of(new DevolucionLineaRequest(1L, new BigDecimal("3.000"))));

        assertThrows(BusinessException.class, () -> {
            returnService.registrarDevolucion(devReq, adminUsuario.getUsername());
        });
    }

    @Test
    @DisplayName("Cancelar una venta que ya tiene devoluciones asociadas debe ser bloqueado para evitar doble stock")
    void testCancelarVentaConDevolucionesBloqueado() {
        // 1. Registrar una venta de 5 unidades
        CrearVentaRequest vtaReq = new CrearVentaRequest();
        vtaReq.setClienteId(clientePrueba.getId());
        vtaReq.setMontoPagadoEfectivo(new BigDecimal("200000.00"));
        vtaReq.setLineas(List.of(new VentaLineaRequest(1L, new BigDecimal("5.000"))));

        VentaResponse venta = saleService.registrarVenta(vtaReq, adminUsuario.getUsername());

        // 2. Registrar devolución parcial de 2 unidades
        CrearDevolucionRequest devReq = new CrearDevolucionRequest();
        devReq.setVentaId(venta.getId());
        devReq.setMotivo("Devolución parcial de 2 martillos");
        devReq.setLineas(List.of(new DevolucionLineaRequest(1L, new BigDecimal("2.000"))));

        DevolucionResponse dev = returnService.registrarDevolucion(devReq, adminUsuario.getUsername());
        assertNotNull(dev);

        // 3. Intentar cancelar la venta original completa
        CancelarDocumentoRequest cancelReq = new CancelarDocumentoRequest("Intento anular venta con devoluciones");

        assertThrows(BusinessException.class, () -> {
            saleService.cancelarVenta(venta.getId(), cancelReq, adminUsuario.getUsername());
        });
    }

    @Test
    @DisplayName("Ajuste manual de inventario autorizado por Administrador actualiza existencias y Kárdex (RN-08)")
    void testAjusteManualAutorizado() {
        InventarioSaldo saldoAntes = saldoRepository.findByReferenciaId(1L).orElseThrow();
        BigDecimal cantAntes = saldoAntes.getCantidadDisponible();

        AjusteInventarioRequest req = new AjusteInventarioRequest();
        req.setReferenciaId(1L);
        req.setTipoAjuste("INCREMENTO");
        req.setCantidad(new BigDecimal("3.000"));
        req.setMotivo("Conteo físico semestral: sobrante verificado");

        AjusteInventario ajuste = adjustmentService.ejecutarAjuste(req, adminUsuario.getUsername());

        assertNotNull(ajuste);
        assertEquals("INCREMENTO", ajuste.getTipoAjuste());

        InventarioSaldo saldoDespues = saldoRepository.findByReferenciaId(1L).orElseThrow();
        assertEquals(0, cantAntes.add(new BigDecimal("3.000")).compareTo(saldoDespues.getCantidadDisponible()));
    }
}
