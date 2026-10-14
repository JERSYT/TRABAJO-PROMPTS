package com.ferreteria.inventario.purchase.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CompraLineaRequest {

    @NotNull(message = "La referencia de producto es obligatoria")
    private Long referenciaId;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo unitario debe ser mayor a cero")
    private BigDecimal costoUnitario;

    public CompraLineaRequest() {}

    public CompraLineaRequest(Long referenciaId, BigDecimal cantidad, BigDecimal costoUnitario) {
        this.referenciaId = referenciaId;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
    }

    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }
}
