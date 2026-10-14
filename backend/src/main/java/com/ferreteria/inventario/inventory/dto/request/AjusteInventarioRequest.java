package com.ferreteria.inventario.inventory.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class AjusteInventarioRequest {

    @NotNull(message = "La referencia es obligatoria")
    private Long referenciaId;

    @NotBlank(message = "El tipo de ajuste es obligatorio")
    @Pattern(regexp = "^(INCREMENTO|DISMINUCION)$", message = "Tipo de ajuste inválido (INCREMENTO o DISMINUCION)")
    private String tipoAjuste;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    @NotBlank(message = "El motivo del ajuste es obligatorio")
    @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
    private String motivo;

    public AjusteInventarioRequest() {}

    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }

    public String getTipoAjuste() { return tipoAjuste; }
    public void setTipoAjuste(String tipoAjuste) { this.tipoAjuste = tipoAjuste; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
