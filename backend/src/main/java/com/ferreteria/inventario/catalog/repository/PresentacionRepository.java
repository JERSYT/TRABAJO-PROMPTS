package com.ferreteria.inventario.catalog.repository;

import com.ferreteria.inventario.catalog.entity.Presentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PresentacionRepository extends JpaRepository<Presentacion, Long> {
    Optional<Presentacion> findByNombre(String nombre);
}
