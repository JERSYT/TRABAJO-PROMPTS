package com.ferreteria.inventario.purchase.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CrearCompraRequest {

    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;

    @NotEmpty(message = "La compra debe contener al menos una línea")
    @Valid
    private List<CompraLineaRequest> lineas;

    public CrearCompraRequest() {}

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

    public List<CompraLineaRequest> getLineas() { return lineas; }
    public void setLineas(List<CompraLineaRequest> lineas) { this.lineas = lineas; }
}
