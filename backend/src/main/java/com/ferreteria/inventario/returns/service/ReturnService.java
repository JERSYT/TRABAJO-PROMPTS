package com.ferreteria.inventario.returns.service;

import com.ferreteria.inventario.catalog.entity.Referencia;
import com.ferreteria.inventario.catalog.service.CatalogService;
import com.ferreteria.inventario.customer.entity.Tercero;
import com.ferreteria.inventario.customer.service.ThirdPartyService;
import com.ferreteria.inventario.document.service.DocumentSequenceService;
import com.ferreteria.inventario.inventory.service.InventoryService;
import com.ferreteria.inventario.returns.dto.request.CrearDevolucionRequest;
import com.ferreteria.inventario.returns.dto.request.DevolucionLineaRequest;
import com.ferreteria.inventario.returns.dto.response.DevolucionLineaResponse;
import com.ferreteria.inventario.returns.dto.response.DevolucionResponse;
import com.ferreteria.inventario.returns.entity.Devolucion;
import com.ferreteria.inventario.returns.entity.DevolucionLinea;
import com.ferreteria.inventario.returns.repository.DevolucionRepository;
import com.ferreteria.inventario.sale.entity.Venta;
import com.ferreteria.inventario.sale.entity.VentaLinea;
import com.ferreteria.inventario.sale.service.SaleService;
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
import java.time.LocalDateTime;

@Service
public class ReturnService {

    private final DevolucionRepository devolucionRepository;
    private final SaleService saleService;
    private final ThirdPartyService thirdPartyService;
    private final CatalogService catalogService;
    private final InventoryService inventoryService;
    private final DocumentSequenceService sequenceService;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;

    public ReturnService(DevolucionRepository devolucionRepository,
                         SaleService saleService,
                         ThirdPartyService thirdPartyService,
                         CatalogService catalogService,
                         InventoryService inventoryService,
                         DocumentSequenceService sequenceService,
                         UsuarioRepository usuarioRepository,
                         AuditService auditService) {
        this.devolucionRepository = devolucionRepository;
        this.saleService = saleService;
        this.thirdPartyService = thirdPartyService;
        this.catalogService = catalogService;
        this.inventoryService = inventoryService;
        this.sequenceService = sequenceService;
        this.usuarioRepository = usuarioRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<DevolucionResponse> listarDevoluciones(Pageable pageable) {
        return devolucionRepository.findAllByOrderByFechaHoraDesc(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public DevolucionResponse obtenerPorId(Long id) {
        Devolucion dev = devolucionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devolución no encontrada con ID: " + id));
        return mapToResponse(dev);
    }

    @Transactional(readOnly = true)
    public DevolucionResponse obtenerPorConsecutivo(String consecutivo) {
        Devolucion dev = devolucionRepository.findByConsecutivo(consecutivo)
                .orElseThrow(() -> new ResourceNotFoundException("Devolución no encontrada con consecutivo: " + consecutivo));
        return mapToResponse(dev);
    }

    /**
     * Registro de devolución de cliente (RN-04, RN-07).
     * Modalidad vinculada a venta (utiliza costo histórico registrado) o libre (utiliza CPP vigente).
     */
    @Transactional
    public DevolucionResponse registrarDevolucion(CrearDevolucionRequest request, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseGet(() -> usuarioRepository.findByUsername("admin_ferreteria")
                        .orElseGet(() -> usuarioRepository.findAll().stream().findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"))));

        String consecutivo = sequenceService.obtenerSiguienteConsecutivo("DEVOLUCION");

        Devolucion devolucion = new Devolucion();
        devolucion.setConsecutivo(consecutivo);
        devolucion.setMotivo(request.getMotivo());
        devolucion.setUsuario(usuario);
        devolucion.setFechaHora(LocalDateTime.now());
        devolucion.setEstado("APROBADA");

        boolean esVinculada = request.getVentaId() != null;
        devolucion.setTipoDevolucion(esVinculada ? "VINCULADA" : "NO_VINCULADA");

        final Venta ventaOriginal = esVinculada ? saleService.obtenerEntidadPorId(request.getVentaId()) : null;
        if (esVinculada) {
            devolucion.setVenta(ventaOriginal);
            devolucion.setCliente(ventaOriginal.getCliente());
        } else if (request.getClienteId() != null) {
            Tercero cliente = thirdPartyService.obtenerEntidadPorId(request.getClienteId());
            devolucion.setCliente(cliente);
        }

        for (DevolucionLineaRequest lr : request.getLineas()) {
            Referencia ref = catalogService.obtenerEntidadPorId(lr.getReferenciaId());
            BigDecimal costoAplicado;

            if (esVinculada) {
                // Buscar la línea correspondiente en la venta original
                final Long refId = ref.getId();
                VentaLinea lineaVenta = ventaOriginal.getLineas().stream()
                        .filter(lv -> lv.getReferencia().getId().equals(refId))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException("El producto " + ref.getCodigoBarras() + " no formó parte de la venta original " + ventaOriginal.getConsecutivo()));

                if (lr.getCantidad().compareTo(lineaVenta.getCantidad()) > 0) {
                    throw new BusinessException("La cantidad a devolver (" + lr.getCantidad() + ") supera la cantidad vendida (" + lineaVenta.getCantidad() + ") en la venta " + ventaOriginal.getConsecutivo());
                }

                // Utilizar costo histórico congelado en la venta (RN-04)
                costoAplicado = lineaVenta.getCostoHistoricoUnitario();
            } else {
                // Utilizar costo promedio ponderado vigente (RN-04)
                costoAplicado = inventoryService.obtenerCostoPromedioVigente(ref.getId());
            }

            DevolucionLinea linea = new DevolucionLinea(ref, lr.getCantidad(), costoAplicado);
            devolucion.addLinea(linea);

            // Reincorporar automáticamente al inventario disponible (RN-07)
            inventoryService.reintegrarStock(
                    ref.getId(),
                    lr.getCantidad(),
                    costoAplicado,
                    "DEVOLUCION",
                    0L,
                    "Reintegro por devolución " + consecutivo + ": " + request.getMotivo(),
                    usuario
            );
        }

        Devolucion devGuardada = devolucionRepository.save(devolucion);

        auditService.registrarEvento(usuario, "DEVOLUCIONES", "REGISTRAR_DEVOLUCION",
                "Devolución registrada " + consecutivo + " (" + devolucion.getTipoDevolucion() + "). Motivo: " + request.getMotivo());

        return mapToResponse(devGuardada);
    }

    public Devolucion obtenerEntidadPorId(Long id) {
        return devolucionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devolución no encontrada con ID: " + id));
    }

    private DevolucionResponse mapToResponse(Devolucion d) {
        DevolucionResponse res = new DevolucionResponse();
        res.setId(d.getId());
        res.setConsecutivo(d.getConsecutivo());
        if (d.getVenta() != null) {
            res.setVentaId(d.getVenta().getId());
            res.setVentaConsecutivo(d.getVenta().getConsecutivo());
        }
        if (d.getCliente() != null) {
            res.setClienteId(d.getCliente().getId());
            res.setClienteNombre(d.getCliente().getRazonSocial());
        }
        res.setTipoDevolucion(d.getTipoDevolucion());
        res.setFechaHora(d.getFechaHora());
        res.setMotivo(d.getMotivo());
        res.setEstado(d.getEstado());
        res.setUsuarioNombre(d.getUsuario() != null ? d.getUsuario().getNombreCompleto() : "SISTEMA");

        var lineasDto = d.getLineas().stream().map(l -> new DevolucionLineaResponse(
                l.getId(),
                l.getReferencia().getId(),
                l.getReferencia().getNombreCompleto(),
                l.getReferencia().getCodigoBarras(),
                l.getCantidad(),
                l.getCostoUnitarioAplicado()
        )).toList();
        res.setLineas(lineasDto);

        return res;
    }
}
