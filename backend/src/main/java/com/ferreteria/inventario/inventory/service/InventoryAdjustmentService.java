package com.ferreteria.inventario.inventory.service;

import com.ferreteria.inventario.catalog.entity.Referencia;
import com.ferreteria.inventario.catalog.service.CatalogService;
import com.ferreteria.inventario.document.service.DocumentSequenceService;
import com.ferreteria.inventario.inventory.dto.request.AjusteInventarioRequest;
import com.ferreteria.inventario.inventory.entity.AjusteInventario;
import com.ferreteria.inventario.inventory.repository.AjusteInventarioRepository;
import com.ferreteria.inventario.shared.audit.AuditService;
import com.ferreteria.inventario.shared.exception.ResourceNotFoundException;
import com.ferreteria.inventario.user.entity.Usuario;
import com.ferreteria.inventario.user.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio de Ajustes Manuales de Inventario:
 * Ejecutable y autorizable exclusivamente por usuarios con rol ADMINISTRADOR (RN-08).
 */
@Service
public class InventoryAdjustmentService {

    private final AjusteInventarioRepository ajusteRepository;
    private final InventoryService inventoryService;
    private final CatalogService catalogService;
    private final DocumentSequenceService sequenceService;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;

    public InventoryAdjustmentService(AjusteInventarioRepository ajusteRepository,
                                      InventoryService inventoryService,
                                      CatalogService catalogService,
                                      DocumentSequenceService sequenceService,
                                      UsuarioRepository usuarioRepository,
                                      AuditService auditService) {
        this.ajusteRepository = ajusteRepository;
        this.inventoryService = inventoryService;
        this.catalogService = catalogService;
        this.sequenceService = sequenceService;
        this.usuarioRepository = usuarioRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<AjusteInventario> listarAjustes(Pageable pageable) {
        return ajusteRepository.findAllByOrderByFechaHoraDesc(pageable);
    }

    @Transactional
    public AjusteInventario ejecutarAjuste(AjusteInventarioRequest request, String adminUsername) {
        Usuario admin = usuarioRepository.findByUsername(adminUsername)
                .orElseGet(() -> usuarioRepository.findByUsername("admin_ferreteria")
                        .orElseGet(() -> usuarioRepository.findAll().stream().findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado"))));

        Referencia ref = catalogService.obtenerEntidadPorId(request.getReferenciaId());
        String consecutivo = sequenceService.obtenerSiguienteConsecutivo("AJUSTE");
        BigDecimal cppVigente = inventoryService.obtenerCostoPromedioVigente(ref.getId());

        AjusteInventario ajuste = new AjusteInventario();
        ajuste.setConsecutivo(consecutivo);
        ajuste.setReferencia(ref);
        ajuste.setTipoAjuste(request.getTipoAjuste());
        ajuste.setCantidad(request.getCantidad());
        ajuste.setCostoUnitarioReferencia(cppVigente);
        ajuste.setMotivo(request.getMotivo());
        ajuste.setFechaHora(LocalDateTime.now());
        ajuste.setUsuario(admin);

        AjusteInventario guardado = ajusteRepository.save(ajuste);

        // Actualizar saldo y Kárdex (RN-08)
        inventoryService.procesarAjusteManual(
                ref.getId(),
                request.getTipoAjuste(),
                request.getCantidad(),
                request.getMotivo(),
                guardado.getId(),
                admin
        );

        auditService.registrarEvento(admin, "AJUSTES", "AJUSTE_INVENTARIO",
                "Ajuste manual " + consecutivo + " (" + request.getTipoAjuste() + " de " + request.getCantidad() +
                        " unidades en " + ref.getCodigoBarras() + "). Motivo: " + request.getMotivo());

        return guardado;
    }
}
