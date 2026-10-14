package com.ferreteria.inventario.catalog.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ActualizarPrecioRequest {

    @NotNull(message = "El nuevo precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    private BigDecimal nuevoPrecio;

    private String motivo;

    public ActualizarPrecioRequest() {}

    public ActualizarPrecioRequest(BigDecimal nuevoPrecio, String motivo) {
        this.nuevoPrecio = nuevoPrecio;
        this.motivo = motivo;
    }

    public BigDecimal getNuevoPrecio() { return nuevoPrecio; }
    public void setNuevoPrecio(BigDecimal nuevoPrecio) { this.nuevoPrecio = nuevoPrecio; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
