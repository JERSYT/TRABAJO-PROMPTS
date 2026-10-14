package com.ferreteria.inventario.purchase.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CancelarDocumentoRequest {

    @NotBlank(message = "El motivo de cancelación es obligatorio")
    @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
    private String motivo;

    public CancelarDocumentoRequest() {}

    public CancelarDocumentoRequest(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
