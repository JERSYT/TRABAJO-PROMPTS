package com.ferreteria.inventario.document.controller;

import com.ferreteria.inventario.document.dto.response.DocumentoComprobanteResponse;
import com.ferreteria.inventario.document.service.DocumentService;
import com.ferreteria.inventario.shared.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/{tipo}/{consecutivo}")
    public ResponseEntity<DocumentoComprobanteResponse> obtenerComprobante(
            @PathVariable String tipo,
            @PathVariable String consecutivo) {
        String tipoNorm = tipo.toUpperCase();
        DocumentoComprobanteResponse doc = switch (tipoNorm) {
            case "VENTA", "SALES" -> documentService.obtenerComprobanteVenta(consecutivo);
            case "COMPRA", "PURCHASES" -> documentService.obtenerComprobanteCompra(consecutivo);
            case "DEVOLUCION", "RETURNS" -> documentService.obtenerComprobanteDevolucion(consecutivo);
            default -> throw new BusinessException("Tipo de documento no soportado para emisión de comprobante: " + tipo);
        };
        return ResponseEntity.ok(doc);
    }
}
