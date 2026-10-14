package com.ferreteria.inventario.sale.repository;

import com.ferreteria.inventario.sale.entity.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    Optional<Venta> findByConsecutivo(String consecutivo);

    @Query("SELECT v FROM Venta v WHERE (:estado IS NULL OR v.estado = :estado) AND " +
           "(:clienteId IS NULL OR v.cliente.id = :clienteId) AND " +
           "(:desde IS NULL OR v.fechaHora >= :desde) AND " +
           "(:hasta IS NULL OR v.fechaHora <= :hasta) " +
           "ORDER BY v.fechaHora DESC")
    Page<Venta> searchVentas(
            @Param("estado") String estado,
            @Param("clienteId") Long clienteId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );
}
