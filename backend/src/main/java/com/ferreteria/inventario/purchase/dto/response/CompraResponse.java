package com.ferreteria.inventario.purchase.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CompraResponse {
    private Long id;
    private String consecutivo;
    private Long proveedorId;
    private String proveedorRazonSocial;
    private String proveedorDocumento;
    private LocalDateTime fechaHora;
    private BigDecimal total;
    private String estado;
    private String motivoCancelacion;
    private String usuarioNombre;
    private List<CompraLineaResponse> lineas;

    public CompraResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConsecutivo() { return consecutivo; }
    public void setConsecutivo(String consecutivo) { this.consecutivo = consecutivo; }
    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }
    public String getProveedorRazonSocial() { return proveedorRazonSocial; }
    public void setProveedorRazonSocial(String proveedorRazonSocial) { this.proveedorRazonSocial = proveedorRazonSocial; }
    public String getProveedorDocumento() { return proveedorDocumento; }
    public void setProveedorDocumento(String proveedorDocumento) { this.proveedorDocumento = proveedorDocumento; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
    public List<CompraLineaResponse> getLineas() { return lineas; }
    public void setLineas(List<CompraLineaResponse> lineas) { this.lineas = lineas; }
}
