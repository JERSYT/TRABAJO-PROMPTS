package com.ferreteria.inventario.returns.controller;

import com.ferreteria.inventario.returns.dto.request.CrearDevolucionRequest;
import com.ferreteria.inventario.returns.dto.response.DevolucionResponse;
import com.ferreteria.inventario.returns.service.ReturnService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/returns")
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @GetMapping
    public ResponseEntity<Page<DevolucionResponse>> listarDevoluciones(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(returnService.listarDevoluciones(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DevolucionResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(returnService.obtenerPorId(id));
    }

    @GetMapping("/consecutive/{consecutivo}")
    public ResponseEntity<DevolucionResponse> obtenerPorConsecutivo(@PathVariable String consecutivo) {
        return ResponseEntity.ok(returnService.obtenerPorConsecutivo(consecutivo));
    }

    @PostMapping
    public ResponseEntity<DevolucionResponse> registrarDevolucion(
            @Valid @RequestBody CrearDevolucionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        DevolucionResponse creada = returnService.registrarDevolucion(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}
