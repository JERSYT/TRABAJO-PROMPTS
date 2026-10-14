package com.ferreteria.inventario.shared.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Long> {

    @Query("SELECT a FROM AuditoriaLog a WHERE (:modulo IS NULL OR a.modulo = :modulo) AND " +
           "(:usuarioId IS NULL OR a.usuario.id = :usuarioId) AND " +
           "(:desde IS NULL OR a.fechaHora >= :desde) AND " +
           "(:hasta IS NULL OR a.fechaHora <= :hasta) " +
           "ORDER BY a.fechaHora DESC")
    Page<AuditoriaLog> searchLogs(
            @Param("modulo") String modulo,
            @Param("usuarioId") Long usuarioId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );
}
