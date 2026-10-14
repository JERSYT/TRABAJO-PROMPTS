package com.ferreteria.inventario.shared.audit;

import com.ferreteria.inventario.user.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio transversal de auditoría básica (RN-11).
 * Registra usuario responsable, fecha/hora y descripción.
 */
@Service
public class AuditService {

    private final AuditoriaLogRepository auditoriaLogRepository;

    public AuditService(AuditoriaLogRepository auditoriaLogRepository) {
        this.auditoriaLogRepository = auditoriaLogRepository;
    }

    @Transactional
    public void registrarEvento(Usuario usuario, String modulo, String accion, String descripcion) {
        AuditoriaLog log = new AuditoriaLog(usuario, modulo, accion, descripcion);
        auditoriaLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaLogResponse> consultarLogs(String modulo, Long usuarioId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        return auditoriaLogRepository.searchLogs(modulo, usuarioId, desde, hasta, pageable)
                .map(log -> new AuditoriaLogResponse(
                        log.getId(),
                        log.getUsuario() != null ? log.getUsuario().getNombreCompleto() : "SISTEMA",
                        log.getFechaHora(),
                        log.getModulo(),
                        log.getAccion(),
                        log.getDescripcion()
                ));
    }
}
