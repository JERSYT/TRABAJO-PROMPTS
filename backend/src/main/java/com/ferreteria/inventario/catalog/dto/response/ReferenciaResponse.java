package com.ferreteria.inventario.catalog.dto.response;

import java.math.BigDecimal;

public class ReferenciaResponse {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private Long marcaId;
    private String marcaNombre;
    private Long presentacionId;
    private String presentacionNombre;
    private Long unidadMedidaId;
    private String unidadMedidaCodigo;
    private String codigoBarras;
    private BigDecimal precioVenta;
    private boolean activo;
    private BigDecimal stockDisponible;
    private BigDecimal costoPromedio;

    public ReferenciaResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public Long getMarcaId() { return marcaId; }
    public void setMarcaId(Long marcaId) { this.marcaId = marcaId; }
    public String getMarcaNombre() { return marcaNombre; }
    public void setMarcaNombre(String marcaNombre) { this.marcaNombre = marcaNombre; }
    public Long getPresentacionId() { return presentacionId; }
    public void setPresentacionId(Long presentacionId) { this.presentacionId = presentacionId; }
    public String getPresentacionNombre() { return presentacionNombre; }
    public void setPresentacionNombre(String presentacionNombre) { this.presentacionNombre = presentacionNombre; }
    public Long getUnidadMedidaId() { return unidadMedidaId; }
    public void setUnidadMedidaId(Long unidadMedidaId) { this.unidadMedidaId = unidadMedidaId; }
    public String getUnidadMedidaCodigo() { return unidadMedidaCodigo; }
    public void setUnidadMedidaCodigo(String unidadMedidaCodigo) { this.unidadMedidaCodigo = unidadMedidaCodigo; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public BigDecimal getStockDisponible() { return stockDisponible; }
    public void setStockDisponible(BigDecimal stockDisponible) { this.stockDisponible = stockDisponible; }
    public BigDecimal getCostoPromedio() { return costoPromedio; }
    public void setCostoPromedio(BigDecimal costoPromedio) { this.costoPromedio = costoPromedio; }
}
