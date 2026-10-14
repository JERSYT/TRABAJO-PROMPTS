package com.ferreteria.inventario.inventory.entity;

import com.ferreteria.inventario.catalog.entity.Referencia;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de Saldo de Inventario: mantiene el saldo físico disponible actual
 * y el Costo Promedio Ponderado (CPP) por referencia (RN-01, RN-04).
 */
@Entity
@Table(name = "inventario_saldo")
public class InventarioSaldo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "referencia_id", nullable = false, unique = true)
    private Referencia referencia;

    @Column(name = "cantidad_disponible", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadDisponible = BigDecimal.ZERO;

    @Column(name = "costo_promedio_ponderado", nullable = false, precision = 15, scale = 4)
    private BigDecimal costoPromedioPonderado = BigDecimal.ZERO;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public InventarioSaldo() {}

    public InventarioSaldo(Referencia referencia, BigDecimal cantidadDisponible, BigDecimal costoPromedioPonderado) {
        this.referencia = referencia;
        this.cantidadDisponible = cantidadDisponible;
        this.costoPromedioPonderado = costoPromedioPonderado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Referencia getReferencia() { return referencia; }
    public void setReferencia(Referencia referencia) { this.referencia = referencia; }

    public BigDecimal getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(BigDecimal cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }

    public BigDecimal getCostoPromedioPonderado() { return costoPromedioPonderado; }
    public void setCostoPromedioPonderado(BigDecimal costoPromedioPonderado) { this.costoPromedioPonderado = costoPromedioPonderado; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
