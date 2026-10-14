package com.ferreteria.inventario.inventory.repository;

import com.ferreteria.inventario.inventory.entity.InventarioSaldo;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventarioSaldoRepository extends JpaRepository<InventarioSaldo, Long> {

    Optional<InventarioSaldo> findByReferenciaId(Long referenciaId);

    /**
     * Consulta con Bloqueo Pesimista Exclusivo (SELECT ... FOR UPDATE)
     * para asegurar consistencia atómica y evitar existencias negativas
     * ante transacciones concurrentes (RN-01).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InventarioSaldo s WHERE s.referencia.id = :referenciaId")
    Optional<InventarioSaldo> findByReferenciaIdForUpdate(@Param("referenciaId") Long referenciaId);

    @Query("SELECT s FROM InventarioSaldo s WHERE (:search IS NULL OR " +
           "LOWER(s.referencia.producto.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "s.referencia.codigoBarras LIKE CONCAT('%', :search, '%')) AND " +
           "(:bajoStock = false OR s.cantidadDisponible <= 5)")
    Page<InventarioSaldo> searchSaldos(
            @Param("search") String search,
            @Param("bajoStock") boolean bajoStock,
            Pageable pageable
    );
}
