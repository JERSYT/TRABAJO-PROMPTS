package com.ferreteria.inventario.returns.repository;

import com.ferreteria.inventario.returns.entity.Devolucion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {
    Optional<Devolucion> findByConsecutivo(String consecutivo);
    List<Devolucion> findByVentaId(Long ventaId);
    boolean existsByVentaId(Long ventaId);
    Page<Devolucion> findAllByOrderByFechaHoraDesc(Pageable pageable);
}
