package com.ferreteria.inventario.sale.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para registrar una venta en mostrador.
 * Nota de seguridad: El cliente NO envía precios, costos ni totales;
 * el backend los calcula y certifica con el catálogo y CPP vigentes.
 */
public class CrearVentaRequest {

    @NotNull(message = "El cliente es obligatorio para registrar la venta (RN-05)")
    private Long clienteId;

    @NotNull(message = "El monto pagado en efectivo es obligatorio (RN-06)")
    @DecimalMin(value = "0.01", message = "El monto pagado debe ser mayor a cero")
    private BigDecimal montoPagadoEfectivo;

    @NotEmpty(message = "La venta debe incluir al menos una línea de producto")
    @Valid
    private List<VentaLineaRequest> lineas;

    public CrearVentaRequest() {}

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public BigDecimal getMontoPagadoEfectivo() { return montoPagadoEfectivo; }
    public void setMontoPagadoEfectivo(BigDecimal montoPagadoEfectivo) { this.montoPagadoEfectivo = montoPagadoEfectivo; }

    public List<VentaLineaRequest> getLineas() { return lineas; }
    public void setLineas(List<VentaLineaRequest> lineas) { this.lineas = lineas; }
}
