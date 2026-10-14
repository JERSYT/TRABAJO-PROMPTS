package com.ferreteria.inventario.sale.entity;

import com.ferreteria.inventario.catalog.entity.Referencia;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidad VentaLinea: detalle de productos vendidos, congelando el precio
 * y el costo histórico unitario vigente al momento de la venta (RN-04).
 */
@Entity
@Table(name = "venta_linea")
public class VentaLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "referencia_id", nullable = false)
    private Referencia referencia;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "costo_historico_unitario", nullable = false, precision = 15, scale = 4)
    private BigDecimal costoHistoricoUnitario;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    public VentaLinea() {}

    public VentaLinea(Referencia referencia, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal costoHistoricoUnitario, BigDecimal subtotal) {
        this.referencia = referencia;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costoHistoricoUnitario = costoHistoricoUnitario;
        this.subtotal = subtotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public Referencia getReferencia() { return referencia; }
    public void setReferencia(Referencia referencia) { this.referencia = referencia; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getCostoHistoricoUnitario() { return costoHistoricoUnitario; }
    public void setCostoHistoricoUnitario(BigDecimal costoHistoricoUnitario) { this.costoHistoricoUnitario = costoHistoricoUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
