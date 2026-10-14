package com.ferreteria.inventario.shared.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Controlador de comprobación técnica de salud del proceso backend.
 * Permite validar que el servidor web se encuentra levantado y respondiendo peticiones HTTP.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ferreteria-inventario-backend",
                "timestamp", Instant.now().toString(),
                "javaVersion", System.getProperty("java.version"),
                "environment", "ready"
        ));
    }
}
