package com.ferreteria.inventario.sale.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class VentaLineaRequest {

    @NotNull(message = "La referencia de producto es obligatoria")
    private Long referenciaId;

    @NotNull(message = "La cantidad vendida es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    public VentaLineaRequest() {}

    public VentaLineaRequest(Long referenciaId, BigDecimal cantidad) {
        this.referenciaId = referenciaId;
        this.cantidad = cantidad;
    }

    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
}
