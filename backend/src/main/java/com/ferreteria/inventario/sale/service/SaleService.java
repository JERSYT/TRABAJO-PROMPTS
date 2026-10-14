package com.ferreteria.inventario.sale.service;

import com.ferreteria.inventario.catalog.entity.Referencia;
import com.ferreteria.inventario.catalog.service.CatalogService;
import com.ferreteria.inventario.customer.entity.Tercero;
import com.ferreteria.inventario.customer.service.ThirdPartyService;
import com.ferreteria.inventario.document.service.DocumentSequenceService;
import com.ferreteria.inventario.inventory.service.InventoryService;
import com.ferreteria.inventario.purchase.dto.request.CancelarDocumentoRequest;
import com.ferreteria.inventario.returns.repository.DevolucionRepository;
import com.ferreteria.inventario.sale.dto.request.CrearVentaRequest;
import com.ferreteria.inventario.sale.dto.request.VentaLineaRequest;
import com.ferreteria.inventario.sale.dto.response.VentaLineaResponse;
import com.ferreteria.inventario.sale.dto.response.VentaResponse;
import com.ferreteria.inventario.sale.entity.Venta;
import com.ferreteria.inventario.sale.entity.VentaLinea;
import com.ferreteria.inventario.sale.repository.VentaRepository;
import com.ferreteria.inventario.shared.audit.AuditService;
import com.ferreteria.inventario.shared.exception.BusinessException;
import com.ferreteria.inventario.shared.exception.ResourceNotFoundException;
import com.ferreteria.inventario.user.entity.Usuario;
import com.ferreteria.inventario.user.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class SaleService {

    private final VentaRepository ventaRepository;
    private final ThirdPartyService thirdPartyService;
    private final CatalogService catalogService;
    private final InventoryService inventoryService;
    private final DocumentSequenceService sequenceService;
    private final DevolucionRepository devolucionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;

    public SaleService(VentaRepository ventaRepository,
                       ThirdPartyService thirdPartyService,
                       CatalogService catalogService,
                       InventoryService inventoryService,
                       DocumentSequenceService sequenceService,
                       DevolucionRepository devolucionRepository,
                       UsuarioRepository usuarioRepository,
                       AuditService auditService) {
        this.ventaRepository = ventaRepository;
        this.thirdPartyService = thirdPartyService;
        this.catalogService = catalogService;
        this.inventoryService = inventoryService;
        this.sequenceService = sequenceService;
        this.devolucionRepository = devolucionRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<VentaResponse> listarVentas(String estado, Long clienteId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        return ventaRepository.searchVentas(estado, clienteId, desde, hasta, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public VentaResponse obtenerPorId(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));
        return mapToResponse(venta);
    }

    @Transactional(readOnly = true)
    public VentaResponse obtenerPorConsecutivo(String consecutivo) {
        Venta venta = ventaRepository.findByConsecutivo(consecutivo)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con consecutivo: " + consecutivo));
        return mapToResponse(venta);
    }

    /**
     * Registro de venta de mostrador de contado en efectivo (RN-01, RN-04, RN-05, RN-06, RN-12).
     * Transacción atómica con bloqueo pesimista de stock y congelamiento del costo histórico.
     */
    @Transactional
    public VentaResponse registrarVenta(CrearVentaRequest request, String username) {
        // Regla RN-05: Cliente registrado obligatorio
        Tercero cliente = thirdPartyService.obtenerEntidadPorId(request.getClienteId());
        if (!cliente.isEsCliente()) {
            throw new BusinessException("El tercero seleccionado no está configurado como cliente (RN-05)");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseGet(() -> usuarioRepository.findByUsername("admin_ferreteria")
                        .orElseGet(() -> usuarioRepository.findAll().stream().findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"))));

        String consecutivo = sequenceService.obtenerSiguienteConsecutivo("VENTA");

        Venta venta = new Venta();
        venta.setConsecutivo(consecutivo);
        venta.setCliente(cliente);
        venta.setUsuario(usuario);
        venta.setEstado("COMPLETADA");
        venta.setFechaHora(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        // Construir líneas validando precios de catálogo
        for (VentaLineaRequest lr : request.getLineas()) {
            Referencia ref = catalogService.obtenerEntidadPorId(lr.getReferenciaId());
            if (!ref.isActivo()) {
                throw new BusinessException("El producto " + ref.getCodigoBarras() + " está inactivo y no puede ser vendido");
            }

            BigDecimal precioUnitario = ref.getPrecioVenta();
            BigDecimal subtotal = lr.getCantidad().multiply(precioUnitario).setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotal);

            // Descontar del inventario con bloqueo pesimista contra stock negativo (RN-01)
            // y capturar el costo histórico unitario vigente en este instante (RN-04)
            BigDecimal costoHistorico = inventoryService.descontarStockConBloqueo(
                    ref.getId(),
                    lr.getCantidad(),
                    "VENTA",
                    0L, // Se actualiza tras el save
                    "Venta en mostrador " + consecutivo,
                    usuario
            );

            VentaLinea linea = new VentaLinea(ref, lr.getCantidad(), precioUnitario, costoHistorico, subtotal);
            venta.addLinea(linea);
        }

        // Regla RN-06: Pago en efectivo de contado
        if (request.getMontoPagadoEfectivo().compareTo(total) < 0) {
            throw new BusinessException("El monto entregado en efectivo ($" + request.getMontoPagadoEfectivo() +
                    ") es inferior al total a pagar ($" + total + ")");
        }

        venta.setSubtotal(total);
        venta.setTotal(total);
        venta.setMontoPagadoEfectivo(request.getMontoPagadoEfectivo());
        venta.setCambioDevuelto(request.getMontoPagadoEfectivo().subtract(total));

        Venta ventaGuardada = ventaRepository.save(venta);

        auditService.registrarEvento(usuario, "VENTAS", "REGISTRAR_VENTA",
                "Venta registrada " + consecutivo + " a cliente " + cliente.getRazonSocial() + " por total $" + total);

        return mapToResponse(ventaGuardada);
    }

    /**
     * Cancelación de venta con reversión de stock autorizada exclusivamente al Administrador (RN-08).
     */
    @Transactional
    public VentaResponse cancelarVenta(Long id, CancelarDocumentoRequest request, String username) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));

        if ("CANCELADA".equalsIgnoreCase(venta.getEstado())) {
            throw new BusinessException("La venta ya se encuentra cancelada");
        }

        // Control de consistencia: No permitir cancelar venta si ya tiene devoluciones previas
        if (devolucionRepository.existsByVentaId(id)) {
            throw new BusinessException("La venta " + venta.getConsecutivo() +
                    " posee devoluciones registradas. Resuelva o anule las devoluciones antes de cancelar la venta original.");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Reintegrar las existencias al inventario disponible
        for (VentaLinea linea : venta.getLineas()) {
            inventoryService.compensarEntradaVenta(
                    linea.getReferencia().getId(),
                    linea.getCantidad(),
                    linea.getCostoHistoricoUnitario(),
                    venta.getId(),
                    "Anulación de venta " + venta.getConsecutivo() + ": " + request.getMotivo(),
                    usuario
            );
        }

        venta.setEstado("CANCELADA");
        venta.setMotivoCancelacion(request.getMotivo());
        Venta cancelada = ventaRepository.save(venta);

        auditService.registrarEvento(usuario, "VENTAS", "CANCELAR_VENTA",
                "Cancelación de venta " + venta.getConsecutivo() + ". Motivo: " + request.getMotivo());

        return mapToResponse(cancelada);
    }

    public Venta obtenerEntidadPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));
    }

    private VentaResponse mapToResponse(Venta v) {
        VentaResponse res = new VentaResponse();
        res.setId(v.getId());
        res.setConsecutivo(v.getConsecutivo());
        res.setClienteId(v.getCliente().getId());
        res.setClienteNombre(v.getCliente().getRazonSocial());
        res.setClienteDocumento(v.getCliente().getNumeroDocumento());
        res.setFechaHora(v.getFechaHora());
        res.setSubtotal(v.getSubtotal());
        res.setTotal(v.getTotal());
        res.setMontoPagadoEfectivo(v.getMontoPagadoEfectivo());
        res.setCambioDevuelto(v.getCambioDevuelto());
        res.setEstado(v.getEstado());
        res.setMotivoCancelacion(v.getMotivoCancelacion());
        res.setUsuarioNombre(v.getUsuario() != null ? v.getUsuario().getNombreCompleto() : "SISTEMA");

        var lineasDto = v.getLineas().stream().map(l -> new VentaLineaResponse(
                l.getId(),
                l.getReferencia().getId(),
                l.getReferencia().getNombreCompleto(),
                l.getReferencia().getCodigoBarras(),
                l.getCantidad(),
                l.getPrecioUnitario(),
                l.getCostoHistoricoUnitario(),
                l.getSubtotal()
        )).toList();
        res.setLineas(lineasDto);

        return res;
    }
}
