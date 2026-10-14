package com.ferreteria.inventario.purchase.service;

import com.ferreteria.inventario.catalog.entity.Referencia;
import com.ferreteria.inventario.catalog.service.CatalogService;
import com.ferreteria.inventario.customer.entity.Tercero;
import com.ferreteria.inventario.customer.service.ThirdPartyService;
import com.ferreteria.inventario.document.service.DocumentSequenceService;
import com.ferreteria.inventario.inventory.service.InventoryService;
import com.ferreteria.inventario.purchase.dto.request.CancelarDocumentoRequest;
import com.ferreteria.inventario.purchase.dto.request.CompraLineaRequest;
import com.ferreteria.inventario.purchase.dto.request.CrearCompraRequest;
import com.ferreteria.inventario.purchase.dto.response.CompraLineaResponse;
import com.ferreteria.inventario.purchase.dto.response.CompraResponse;
import com.ferreteria.inventario.purchase.entity.Compra;
import com.ferreteria.inventario.purchase.entity.CompraLinea;
import com.ferreteria.inventario.purchase.repository.CompraRepository;
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
public class PurchaseService {

    private final CompraRepository compraRepository;
    private final ThirdPartyService thirdPartyService;
    private final CatalogService catalogService;
    private final InventoryService inventoryService;
    private final DocumentSequenceService sequenceService;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;

    public PurchaseService(CompraRepository compraRepository,
                           ThirdPartyService thirdPartyService,
                           CatalogService catalogService,
                           InventoryService inventoryService,
                           DocumentSequenceService sequenceService,
                           UsuarioRepository usuarioRepository,
                           AuditService auditService) {
        this.compraRepository = compraRepository;
        this.thirdPartyService = thirdPartyService;
        this.catalogService = catalogService;
        this.inventoryService = inventoryService;
        this.sequenceService = sequenceService;
        this.usuarioRepository = usuarioRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<CompraResponse> listarCompras(String estado, Long proveedorId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        return compraRepository.searchCompras(estado, proveedorId, desde, hasta, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public CompraResponse obtenerPorId(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));
        return mapToResponse(compra);
    }

    @Transactional(readOnly = true)
    public CompraResponse obtenerPorConsecutivo(String consecutivo) {
        Compra compra = compraRepository.findByConsecutivo(consecutivo)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con consecutivo: " + consecutivo));
        return mapToResponse(compra);
    }

    /**
     * Registro y recepción completa de compra en una sola operación transaccional (RN-03, RN-04, RN-10).
     */
    @Transactional
    public CompraResponse registrarCompra(CrearCompraRequest request, String username) {
        Tercero proveedor = thirdPartyService.obtenerEntidadPorId(request.getProveedorId());
        if (!proveedor.isEsProveedor()) {
            throw new BusinessException("El tercero seleccionado no está configurado como proveedor");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));

        String consecutivo = sequenceService.obtenerSiguienteConsecutivo("COMPRA");

        Compra compra = new Compra();
        compra.setConsecutivo(consecutivo);
        compra.setProveedor(proveedor);
        compra.setUsuario(usuario);
        compra.setEstado("RECIBIDA");
        compra.setFechaHora(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (CompraLineaRequest lr : request.getLineas()) {
            Referencia ref = catalogService.obtenerEntidadPorId(lr.getReferenciaId());
            if (!ref.isActivo()) {
                throw new BusinessException("No se pueden registrar compras para productos inactivos: " + ref.getCodigoBarras());
            }

            BigDecimal subtotal = lr.getCantidad().multiply(lr.getCostoUnitario()).setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotal);

            CompraLinea linea = new CompraLinea(ref, lr.getCantidad(), lr.getCostoUnitario(), subtotal);
            compra.addLinea(linea);
        }

        compra.setTotal(total);
        Compra compraGuardada = compraRepository.save(compra);

        // Procesar entradas al inventario y actualizar el CPP (RN-04)
        for (CompraLinea linea : compraGuardada.getLineas()) {
            inventoryService.procesarEntradaCompra(
                    linea.getReferencia().getId(),
                    linea.getCantidad(),
                    linea.getCostoUnitario(),
                    compraGuardada.getId(),
                    usuario
            );
        }

        auditService.registrarEvento(usuario, "COMPRAS", "REGISTRAR_COMPRA",
                "Compra registrada " + consecutivo + " a proveedor " + proveedor.getRazonSocial() + " por total $" + total);

        return mapToResponse(compraGuardada);
    }

    /**
     * Cancelación de compra por parte del Administrador con motivo documentado (RN-03, RN-08).
     */
    @Transactional
    public CompraResponse cancelarCompra(Long id, CancelarDocumentoRequest request, String username) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));

        if ("CANCELADA".equalsIgnoreCase(compra.getEstado())) {
            throw new BusinessException("La compra ya se encuentra cancelada");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Compensar y revertir stock (bloquea si el stock ya fue consumido)
        for (CompraLinea linea : compra.getLineas()) {
            inventoryService.compensarSalidaCompra(
                    linea.getReferencia().getId(),
                    linea.getCantidad(),
                    linea.getCostoUnitario(),
                    compra.getId(),
                    "Anulación de compra " + compra.getConsecutivo() + ": " + request.getMotivo(),
                    usuario
            );
        }

        compra.setEstado("CANCELADA");
        compra.setMotivoCancelacion(request.getMotivo());
        Compra cancelada = compraRepository.save(compra);

        auditService.registrarEvento(usuario, "COMPRAS", "CANCELAR_COMPRA",
                "Cancelación de compra " + compra.getConsecutivo() + ". Motivo: " + request.getMotivo());

        return mapToResponse(cancelada);
    }

    public Compra obtenerEntidadPorId(Long id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));
    }

    private CompraResponse mapToResponse(Compra c) {
        CompraResponse res = new CompraResponse();
        res.setId(c.getId());
        res.setConsecutivo(c.getConsecutivo());
        res.setProveedorId(c.getProveedor().getId());
        res.setProveedorRazonSocial(c.getProveedor().getRazonSocial());
        res.setProveedorDocumento(c.getProveedor().getNumeroDocumento());
        res.setFechaHora(c.getFechaHora());
        res.setTotal(c.getTotal());
        res.setEstado(c.getEstado());
        res.setMotivoCancelacion(c.getMotivoCancelacion());
        res.setUsuarioNombre(c.getUsuario() != null ? c.getUsuario().getNombreCompleto() : "SISTEMA");

        var lineasDto = c.getLineas().stream().map(l -> new CompraLineaResponse(
                l.getId(),
                l.getReferencia().getId(),
                l.getReferencia().getNombreCompleto(),
                l.getReferencia().getCodigoBarras(),
                l.getCantidad(),
                l.getCostoUnitario(),
                l.getSubtotal()
        )).toList();
        res.setLineas(lineasDto);

        return res;
    }
}
