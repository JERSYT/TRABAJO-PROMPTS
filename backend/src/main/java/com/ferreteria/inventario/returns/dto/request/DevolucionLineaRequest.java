package com.ferreteria.inventario.returns.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class DevolucionLineaRequest {

    @NotNull(message = "La referencia es obligatoria")
    private Long referenciaId;

    @NotNull(message = "La cantidad devuelta es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    public DevolucionLineaRequest() {}

    public DevolucionLineaRequest(Long referenciaId, BigDecimal cantidad) {
        this.referenciaId = referenciaId;
        this.cantidad = cantidad;
    }

    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
}
