package com.ferreteria.inventario.sale.dto.response;

import java.math.BigDecimal;

public class VentaLineaResponse {
    private Long id;
    private Long referenciaId;
    private String productoNombre;
    private String codigoBarras;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal costoHistoricoUnitario;
    private BigDecimal subtotal;

    public VentaLineaResponse() {}

    public VentaLineaResponse(Long id, Long referenciaId, String productoNombre, String codigoBarras,
                              BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal costoHistoricoUnitario,
                              BigDecimal subtotal) {
        this.id = id;
        this.referenciaId = referenciaId;
        this.productoNombre = productoNombre;
        this.codigoBarras = codigoBarras;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costoHistoricoUnitario = costoHistoricoUnitario;
        this.subtotal = subtotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getCostoHistoricoUnitario() { return costoHistoricoUnitario; }
    public void setCostoHistoricoUnitario(BigDecimal costoHistoricoUnitario) { this.costoHistoricoUnitario = costoHistoricoUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
