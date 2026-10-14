package com.ferreteria.inventario.inventory.repository;

import com.ferreteria.inventario.inventory.entity.AjusteInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AjusteInventarioRepository extends JpaRepository<AjusteInventario, Long> {
    Optional<AjusteInventario> findByConsecutivo(String consecutivo);
    Page<AjusteInventario> findAllByOrderByFechaHoraDesc(Pageable pageable);
}
