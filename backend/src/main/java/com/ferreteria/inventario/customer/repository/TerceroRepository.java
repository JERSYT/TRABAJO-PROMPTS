package com.ferreteria.inventario.customer.repository;

import com.ferreteria.inventario.customer.entity.Tercero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TerceroRepository extends JpaRepository<Tercero, Long> {

    Optional<Tercero> findByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumento(String numeroDocumento);

    @Query("SELECT t FROM Tercero t WHERE (:search IS NULL OR LOWER(t.razonSocial) LIKE LOWER(CONCAT('%', :search, '%')) OR t.numeroDocumento LIKE CONCAT('%', :search, '%')) AND (:soloClientes = false OR t.esCliente = true) AND (:soloProveedores = false OR t.esProveedor = true)")
    Page<Tercero> searchTerceros(
            @Param("search") String search,
            @Param("soloClientes") boolean soloClientes,
            @Param("soloProveedores") boolean soloProveedores,
            Pageable pageable
    );
}
