package com.ferreteria.inventario.purchase.controller;

import com.ferreteria.inventario.purchase.dto.request.CancelarDocumentoRequest;
import com.ferreteria.inventario.purchase.dto.request.CrearCompraRequest;
import com.ferreteria.inventario.purchase.dto.response.CompraResponse;
import com.ferreteria.inventario.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public ResponseEntity<Page<CompraResponse>> listarCompras(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(purchaseService.listarCompras(estado, proveedorId, desde, hasta, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.obtenerPorId(id));
    }

    @GetMapping("/consecutive/{consecutivo}")
    public ResponseEntity<CompraResponse> obtenerPorConsecutivo(@PathVariable String consecutivo) {
        return ResponseEntity.ok(purchaseService.obtenerPorConsecutivo(consecutivo));
    }

    @PostMapping
    public ResponseEntity<CompraResponse> registrarCompra(
            @Valid @RequestBody CrearCompraRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        CompraResponse creada = purchaseService.registrarCompra(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CompraResponse> cancelarCompra(
            @PathVariable Long id,
            @Valid @RequestBody CancelarDocumentoRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        String username = admin != null ? admin.getUsername() : "SISTEMA";
        return ResponseEntity.ok(purchaseService.cancelarCompra(id, request, username));
    }
}
