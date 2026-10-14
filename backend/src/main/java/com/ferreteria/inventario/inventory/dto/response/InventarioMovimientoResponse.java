package com.ferreteria.inventario.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventarioMovimientoResponse {
    private Long id;
    private Long referenciaId;
    private String productoNombre;
    private String tipoMovimiento;
    private BigDecimal cantidad;
    private BigDecimal costoUnitarioMovimiento;
    private BigDecimal saldoResultante;
    private String documentoTipo;
    private Long documentoId;
    private String motivo;
    private LocalDateTime fechaHora;
    private String usuarioNombre;

    public InventarioMovimientoResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public BigDecimal getCostoUnitarioMovimiento() { return costoUnitarioMovimiento; }
    public void setCostoUnitarioMovimiento(BigDecimal costoUnitarioMovimiento) { this.costoUnitarioMovimiento = costoUnitarioMovimiento; }
    public BigDecimal getSaldoResultante() { return saldoResultante; }
    public void setSaldoResultante(BigDecimal saldoResultante) { this.saldoResultante = saldoResultante; }
    public String getDocumentoTipo() { return documentoTipo; }
    public void setDocumentoTipo(String documentoTipo) { this.documentoTipo = documentoTipo; }
    public Long getDocumentoId() { return documentoId; }
    public void setDocumentoId(Long documentoId) { this.documentoId = documentoId; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
}
