package com.ferreteria.inventario.catalog.repository;

import com.ferreteria.inventario.catalog.entity.Referencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferenciaRepository extends JpaRepository<Referencia, Long> {

    Optional<Referencia> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);

    boolean existsByProductoIdAndMarcaIdAndPresentacionId(Long productoId, Long marcaId, Long presentacionId);

    @Query("SELECT r FROM Referencia r WHERE (:activo IS NULL OR r.activo = :activo) AND " +
           "(:search IS NULL OR LOWER(r.producto.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.marca.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "r.codigoBarras LIKE CONCAT('%', :search, '%'))")
    Page<Referencia> searchReferencias(@Param("search") String search, @Param("activo") Boolean activo, Pageable pageable);
}
