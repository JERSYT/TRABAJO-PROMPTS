package com.ferreteria.inventario.catalog.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class CrearReferenciaRequest {

    private Long productoId;
    private String productoNombre;

    private Long marcaId;
    private String marcaNombre;

    private Long presentacionId;
    private String presentacionNombre;

    private Long unidadMedidaId;
    private String unidadMedidaCodigo;

    @NotBlank(message = "El código de barras es obligatorio")
    @Size(min = 3, max = 50, message = "El código de barras debe tener entre 3 y 50 caracteres")
    private String codigoBarras;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a cero")
    private BigDecimal precioVenta;

    public CrearReferenciaRequest() {}

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
}
