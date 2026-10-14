package com.ferreteria.inventario.purchase.repository;

import com.ferreteria.inventario.purchase.entity.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    Optional<Compra> findByConsecutivo(String consecutivo);

    @Query("SELECT c FROM Compra c WHERE (:estado IS NULL OR c.estado = :estado) AND " +
           "(:proveedorId IS NULL OR c.proveedor.id = :proveedorId) AND " +
           "(:desde IS NULL OR c.fechaHora >= :desde) AND " +
           "(:hasta IS NULL OR c.fechaHora <= :hasta) " +
           "ORDER BY c.fechaHora DESC")
    Page<Compra> searchCompras(
            @Param("estado") String estado,
            @Param("proveedorId") Long proveedorId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );
}
