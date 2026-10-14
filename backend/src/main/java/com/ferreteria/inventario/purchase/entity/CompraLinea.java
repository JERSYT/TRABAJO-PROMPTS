package com.ferreteria.inventario.purchase.entity;

import com.ferreteria.inventario.catalog.entity.Referencia;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "compra_linea")
public class CompraLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "referencia_id", nullable = false)
    private Referencia referencia;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", nullable = false, precision = 15, scale = 4)
    private BigDecimal costoUnitario;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    public CompraLinea() {}

    public CompraLinea(Referencia referencia, BigDecimal cantidad, BigDecimal costoUnitario, BigDecimal subtotal) {
        this.referencia = referencia;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.subtotal = subtotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Compra getCompra() { return compra; }
    public void setCompra(Compra compra) { this.compra = compra; }

    public Referencia getReferencia() { return referencia; }
    public void setReferencia(Referencia referencia) { this.referencia = referencia; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
