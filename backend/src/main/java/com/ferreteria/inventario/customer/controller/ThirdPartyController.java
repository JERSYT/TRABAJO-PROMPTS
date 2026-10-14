package com.ferreteria.inventario.customer.controller;

import com.ferreteria.inventario.customer.dto.request.CrearTerceroRequest;
import com.ferreteria.inventario.customer.dto.response.TerceroResponse;
import com.ferreteria.inventario.customer.service.ThirdPartyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/third-parties")
public class ThirdPartyController {

    private final ThirdPartyService thirdPartyService;

    public ThirdPartyController(ThirdPartyService thirdPartyService) {
        this.thirdPartyService = thirdPartyService;
    }

    @GetMapping
    public ResponseEntity<Page<TerceroResponse>> listarTerceros(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "TODOS") String tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(thirdPartyService.listarTerceros(search, tipo, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TerceroResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(thirdPartyService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<TerceroResponse> crearTercero(@Valid @RequestBody CrearTerceroRequest request) {
        TerceroResponse creado = thirdPartyService.crearTercero(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TerceroResponse> actualizarTercero(
            @PathVariable Long id,
            @Valid @RequestBody CrearTerceroRequest request) {
        return ResponseEntity.ok(thirdPartyService.actualizarTercero(id, request));
    }
}
