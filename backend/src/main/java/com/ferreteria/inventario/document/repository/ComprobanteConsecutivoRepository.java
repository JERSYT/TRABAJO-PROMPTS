package com.ferreteria.inventario.document.repository;

import com.ferreteria.inventario.document.entity.ComprobanteConsecutivo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComprobanteConsecutivoRepository extends JpaRepository<ComprobanteConsecutivo, Long> {

    /**
     * Bloqueo pesimista para garantizar la generación correlativa
     * ininterrumpida de consecutivos sin duplicidad bajo concurrencia (RN-12).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ComprobanteConsecutivo c WHERE c.tipoDocumento = :tipoDocumento")
    Optional<ComprobanteConsecutivo> findByTipoDocumentoForUpdate(@Param("tipoDocumento") String tipoDocumento);

    Optional<ComprobanteConsecutivo> findByTipoDocumento(String tipoDocumento);
}
