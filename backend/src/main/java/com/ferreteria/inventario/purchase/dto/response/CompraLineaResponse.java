package com.ferreteria.inventario.purchase.dto.response;

import java.math.BigDecimal;

public class CompraLineaResponse {
    private Long id;
    private Long referenciaId;
    private String productoNombre;
    private String codigoBarras;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal subtotal;

    public CompraLineaResponse() {}

    public CompraLineaResponse(Long id, Long referenciaId, String productoNombre, String codigoBarras,
                               BigDecimal cantidad, BigDecimal costoUnitario, BigDecimal subtotal) {
        this.id = id;
        this.referenciaId = referenciaId;
        this.productoNombre = productoNombre;
        this.codigoBarras = codigoBarras;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
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
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
