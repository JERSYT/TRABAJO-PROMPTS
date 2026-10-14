package com.ferreteria.inventario.sale.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VentaResponse {
    private Long id;
    private String consecutivo;
    private Long clienteId;
    private String clienteNombre;
    private String clienteDocumento;
    private LocalDateTime fechaHora;
    private BigDecimal subtotal;
    private BigDecimal total;
    private BigDecimal montoPagadoEfectivo;
    private BigDecimal cambioDevuelto;
    private String estado;
    private String motivoCancelacion;
    private String usuarioNombre;
    private List<VentaLineaResponse> lineas;

    public VentaResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConsecutivo() { return consecutivo; }
    public void setConsecutivo(String consecutivo) { this.consecutivo = consecutivo; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getMontoPagadoEfectivo() { return montoPagadoEfectivo; }
    public void setMontoPagadoEfectivo(BigDecimal montoPagadoEfectivo) { this.montoPagadoEfectivo = montoPagadoEfectivo; }
    public BigDecimal getCambioDevuelto() { return cambioDevuelto; }
    public void setCambioDevuelto(BigDecimal cambioDevuelto) { this.cambioDevuelto = cambioDevuelto; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
    public List<VentaLineaResponse> getLineas() { return lineas; }
    public void setLineas(List<VentaLineaResponse> lineas) { this.lineas = lineas; }
}
