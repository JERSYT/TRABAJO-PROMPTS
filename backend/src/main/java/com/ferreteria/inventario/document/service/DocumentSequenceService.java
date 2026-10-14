package com.ferreteria.inventario.document.service;

import com.ferreteria.inventario.document.entity.ComprobanteConsecutivo;
import com.ferreteria.inventario.document.repository.ComprobanteConsecutivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de generar consecutivos internos correlativos
 * e ininterrumpidos mediante bloqueo pesimista en base de datos (RN-12).
 */
@Service
public class DocumentSequenceService {

    private final ComprobanteConsecutivoRepository consecutivoRepository;

    public DocumentSequenceService(ComprobanteConsecutivoRepository consecutivoRepository) {
        this.consecutivoRepository = consecutivoRepository;
    }

    @Transactional
    public String obtenerSiguienteConsecutivo(String tipoDocumento) {
        ComprobanteConsecutivo consecutivo = consecutivoRepository
                .findByTipoDocumentoForUpdate(tipoDocumento)
                .orElseGet(() -> {
                    String prefijo = switch (tipoDocumento) {
                        case "VENTA" -> "VTA-";
                        case "COMPRA" -> "CMP-";
                        case "DEVOLUCION" -> "DEV-";
                        case "AJUSTE" -> "AJU-";
                        default -> "DOC-";
                    };
                    ComprobanteConsecutivo nuevo = new ComprobanteConsecutivo(tipoDocumento, prefijo, 0L);
                    return consecutivoRepository.save(nuevo);
                });

        String siguiente = consecutivo.generarSiguienteConsecutivo();
        consecutivoRepository.save(consecutivo);
        return siguiente;
    }
}
