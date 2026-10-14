package com.ferreteria.inventario.returns.entity;

import com.ferreteria.inventario.catalog.entity.Referencia;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "devolucion_linea")
public class DevolucionLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "devolucion_id", nullable = false)
    private Devolucion devolucion;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "referencia_id", nullable = false)
    private Referencia referencia;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario_aplicado", nullable = false, precision = 15, scale = 4)
    private BigDecimal costoUnitarioAplicado;

    public DevolucionLinea() {}

    public DevolucionLinea(Referencia referencia, BigDecimal cantidad, BigDecimal costoUnitarioAplicado) {
        this.referencia = referencia;
        this.cantidad = cantidad;
        this.costoUnitarioAplicado = costoUnitarioAplicado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Devolucion getDevolucion() { return devolucion; }
    public void setDevolucion(Devolucion devolucion) { this.devolucion = devolucion; }

    public Referencia getReferencia() { return referencia; }
    public void setReferencia(Referencia referencia) { this.referencia = referencia; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getCostoUnitarioAplicado() { return costoUnitarioAplicado; }
    public void setCostoUnitarioAplicado(BigDecimal costoUnitarioAplicado) { this.costoUnitarioAplicado = costoUnitarioAplicado; }
}
