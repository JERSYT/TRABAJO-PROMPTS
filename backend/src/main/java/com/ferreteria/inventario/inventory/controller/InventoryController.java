package com.ferreteria.inventario.inventory.controller;

import com.ferreteria.inventario.inventory.dto.request.AjusteInventarioRequest;
import com.ferreteria.inventario.inventory.dto.request.InventarioInicialRequest;
import com.ferreteria.inventario.inventory.dto.response.InventarioMovimientoResponse;
import com.ferreteria.inventario.inventory.dto.response.InventarioSaldoResponse;
import com.ferreteria.inventario.inventory.entity.AjusteInventario;
import com.ferreteria.inventario.inventory.service.InventoryAdjustmentService;
import com.ferreteria.inventario.inventory.service.InventoryService;
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
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryAdjustmentService adjustmentService;

    public InventoryController(InventoryService inventoryService,
                               InventoryAdjustmentService adjustmentService) {
        this.inventoryService = inventoryService;
        this.adjustmentService = adjustmentService;
    }

    @GetMapping("/stocks")
    public ResponseEntity<Page<InventarioSaldoResponse>> listarSaldos(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean bajoStock,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(inventoryService.listarSaldos(search, bajoStock, pageable));
    }

    @GetMapping("/references/{id}/movements")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Page<InventarioMovimientoResponse>> listarKardex(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(inventoryService.listarMovimientosPorReferencia(id, pageable));
    }

    @PostMapping("/initial")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<InventarioSaldoResponse> inicializarInventario(
            @Valid @RequestBody InventarioInicialRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        String username = admin != null ? admin.getUsername() : "SISTEMA";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.inicializarInventario(request, username));
    }

    @PostMapping("/adjustments")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AjusteInventario> ejecutarAjuste(
            @Valid @RequestBody AjusteInventarioRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        String username = admin != null ? admin.getUsername() : "SISTEMA";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adjustmentService.ejecutarAjuste(request, username));
    }

    @GetMapping("/adjustments")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Page<AjusteInventario>> listarAjustes(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adjustmentService.listarAjustes(pageable));
    }
}
