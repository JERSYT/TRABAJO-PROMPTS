package com.ferreteria.inventario.sale.controller;

import com.ferreteria.inventario.purchase.dto.request.CancelarDocumentoRequest;
import com.ferreteria.inventario.sale.dto.request.CrearVentaRequest;
import com.ferreteria.inventario.sale.dto.response.VentaResponse;
import com.ferreteria.inventario.sale.service.SaleService;
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
@RequestMapping("/api/v1/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public ResponseEntity<Page<VentaResponse>> listarVentas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(saleService.listarVentas(estado, clienteId, desde, hasta, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.obtenerPorId(id));
    }

    @GetMapping("/consecutive/{consecutivo}")
    public ResponseEntity<VentaResponse> obtenerPorConsecutivo(@PathVariable String consecutivo) {
        return ResponseEntity.ok(saleService.obtenerPorConsecutivo(consecutivo));
    }

    @PostMapping
    public ResponseEntity<VentaResponse> registrarVenta(
            @Valid @RequestBody CrearVentaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        VentaResponse creada = saleService.registrarVenta(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VentaResponse> cancelarVenta(
            @PathVariable Long id,
            @Valid @RequestBody CancelarDocumentoRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        String username = admin != null ? admin.getUsername() : "SISTEMA";
        return ResponseEntity.ok(saleService.cancelarVenta(id, request, username));
    }
}
