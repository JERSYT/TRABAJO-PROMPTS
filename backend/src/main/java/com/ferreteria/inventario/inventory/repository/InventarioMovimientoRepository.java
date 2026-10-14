package com.ferreteria.inventario.inventory.repository;

import com.ferreteria.inventario.inventory.entity.InventarioMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {

    Page<InventarioMovimiento> findByReferenciaIdOrderByFechaHoraDesc(Long referenciaId, Pageable pageable);

    Page<InventarioMovimiento> findByReferenciaIdAndFechaHoraBetweenOrderByFechaHoraDesc(
            Long referenciaId,
            LocalDateTime desde,
            LocalDateTime hasta,
            Pageable pageable
    );

    boolean existsByReferenciaId(Long referenciaId);
}
