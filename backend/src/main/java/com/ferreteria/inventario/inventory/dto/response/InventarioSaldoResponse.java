package com.ferreteria.inventario.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventarioSaldoResponse {
    private Long id;
    private Long referenciaId;
    private String codigoBarras;
    private String productoNombre;
    private String marcaNombre;
    private String presentacionNombre;
    private String unidadMedida;
    private BigDecimal cantidadDisponible;
    private BigDecimal costoPromedioPonderado;
    private LocalDateTime fechaActualizacion;

    public InventarioSaldoResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getMarcaNombre() { return marcaNombre; }
    public void setMarcaNombre(String marcaNombre) { this.marcaNombre = marcaNombre; }
    public String getPresentacionNombre() { return presentacionNombre; }
    public void setPresentacionNombre(String presentacionNombre) { this.presentacionNombre = presentacionNombre; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public BigDecimal getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(BigDecimal cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    public BigDecimal getCostoPromedioPonderado() { return costoPromedioPonderado; }
    public void setCostoPromedioPonderado(BigDecimal costoPromedioPonderado) { this.costoPromedioPonderado = costoPromedioPonderado; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
