package com.ferreteria.inventario.inventory.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class InventarioInicialRequest {

    @NotNull(message = "La referencia es obligatoria")
    private Long referenciaId;

    @NotNull(message = "La cantidad inicial es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidadInicial;

    @NotNull(message = "El costo unitario de referencia es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo unitario debe ser mayor a cero")
    private BigDecimal costoUnitario;

    public InventarioInicialRequest() {}

    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }

    public BigDecimal getCantidadInicial() { return cantidadInicial; }
    public void setCantidadInicial(BigDecimal cantidadInicial) { this.cantidadInicial = cantidadInicial; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }
}
