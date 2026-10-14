package com.ferreteria.inventario.catalog.controller;

import com.ferreteria.inventario.catalog.dto.request.ActualizarPrecioRequest;
import com.ferreteria.inventario.catalog.dto.request.CrearReferenciaRequest;
import com.ferreteria.inventario.catalog.dto.response.ReferenciaResponse;
import com.ferreteria.inventario.catalog.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/references")
    public ResponseEntity<Page<ReferenciaResponse>> listarReferencias(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(catalogService.listarReferencias(search, activo, pageable));
    }

    @GetMapping("/references/{id}")
    public ResponseEntity<ReferenciaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.obtenerPorId(id));
    }

    @GetMapping("/references/barcode/{barcode}")
    public ResponseEntity<ReferenciaResponse> obtenerPorCodigoBarras(@PathVariable String barcode) {
        return ResponseEntity.ok(catalogService.obtenerPorCodigoBarras(barcode));
    }

    @GetMapping("/masters")
    public ResponseEntity<java.util.Map<String, Object>> obtenerMaestros() {
        return ResponseEntity.ok(catalogService.obtenerMaestros());
    }

    @PostMapping("/references")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ReferenciaResponse> crearReferencia(
            @Valid @RequestBody CrearReferenciaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        ReferenciaResponse creada = catalogService.crearReferencia(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PatchMapping("/references/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ReferenciaResponse> alternarEstado(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        return ResponseEntity.ok(catalogService.alternarEstado(id, username));
    }

    @PatchMapping("/references/{id}/price")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO_OPERATIVO')")
    public ResponseEntity<ReferenciaResponse> actualizarPrecio(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPrecioRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        return ResponseEntity.ok(catalogService.actualizarPrecio(id, request, username));
    }

    @DeleteMapping("/references/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desactivarReferencia(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        catalogService.desactivarReferencia(id, username);
        return ResponseEntity.noContent().build();
    }
}
