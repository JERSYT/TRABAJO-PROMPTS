package com.ferreteria.inventario.returns.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class DevolucionResponse {
    private Long id;
    private String consecutivo;
    private Long ventaId;
    private String ventaConsecutivo;
    private Long clienteId;
    private String clienteNombre;
    private String tipoDevolucion;
    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;
    private String usuarioNombre;
    private List<DevolucionLineaResponse> lineas;

    public DevolucionResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConsecutivo() { return consecutivo; }
    public void setConsecutivo(String consecutivo) { this.consecutivo = consecutivo; }
    public Long getVentaId() { return ventaId; }
    public void setVentaId(Long ventaId) { this.ventaId = ventaId; }
    public String getVentaConsecutivo() { return ventaConsecutivo; }
    public void setVentaConsecutivo(String ventaConsecutivo) { this.ventaConsecutivo = ventaConsecutivo; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getTipoDevolucion() { return tipoDevolucion; }
    public void setTipoDevolucion(String tipoDevolucion) { this.tipoDevolucion = tipoDevolucion; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
    public List<DevolucionLineaResponse> getLineas() { return lineas; }
    public void setLineas(List<DevolucionLineaResponse> lineas) { this.lineas = lineas; }
}
