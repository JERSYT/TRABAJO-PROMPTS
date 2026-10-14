package com.ferreteria.inventario.returns.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CrearDevolucionRequest {

    private Long ventaId; // Opcional (si es nulo, es no vinculada)
    private Long clienteId; // Opcional si ventaId está presente

    @NotBlank(message = "El motivo de la devolución es obligatorio")
    @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
    private String motivo;

    @NotEmpty(message = "Debe incluir al menos un producto a devolver")
    @Valid
    private List<DevolucionLineaRequest> lineas;

    public CrearDevolucionRequest() {}

    public Long getVentaId() { return ventaId; }
    public void setVentaId(Long ventaId) { this.ventaId = ventaId; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public List<DevolucionLineaRequest> getLineas() { return lineas; }
    public void setLineas(List<DevolucionLineaRequest> lineas) { this.lineas = lineas; }
}
