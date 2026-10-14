package com.ferreteria.inventario.returns.dto.response;

import java.math.BigDecimal;

public class DevolucionLineaResponse {
    private Long id;
    private Long referenciaId;
    private String productoNombre;
    private String codigoBarras;
    private BigDecimal cantidad;
    private BigDecimal costoUnitarioAplicado;

    public DevolucionLineaResponse() {}

    public DevolucionLineaResponse(Long id, Long referenciaId, String productoNombre, String codigoBarras,
                                   BigDecimal cantidad, BigDecimal costoUnitarioAplicado) {
        this.id = id;
        this.referenciaId = referenciaId;
        this.productoNombre = productoNombre;
        this.codigoBarras = codigoBarras;
        this.cantidad = cantidad;
        this.costoUnitarioAplicado = costoUnitarioAplicado;
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
    public BigDecimal getCostoUnitarioAplicado() { return costoUnitarioAplicado; }
    public void setCostoUnitarioAplicado(BigDecimal costoUnitarioAplicado) { this.costoUnitarioAplicado = costoUnitarioAplicado; }
}
