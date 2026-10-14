package com.ferreteria.inventario.document.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la visualización e impresión de comprobantes comerciales
 * internos (Ventas, Compras, Devoluciones, Ajustes) simulando formato de mostrador.
 */
public class DocumentoComprobanteResponse {
    private String tipoDocumento; // VENTA, COMPRA, DEVOLUCION, AJUSTE
    private String consecutivo;
    private LocalDateTime fechaHora;
    private String ferreteriaNombre = "Ferretería El Tornillo Central";
    private String ferreteriaNit = "900.876.543-2";
    private String ferreteriaDireccion = "Calle 10 # 45-20, Bogotá D.C.";
    private String ferreteriaTelefono = "+57 (601) 234-5678";
    
    private String terceroNombre;
    private String terceroDocumento;
    private String terceroDireccion;
    private String terceroTelefono;
    
    private BigDecimal subtotal;
    private BigDecimal total;
    private BigDecimal montoPagado;
    private BigDecimal cambio;
    private String estado;
    private String usuarioResponsable;
    private String motivo; // En caso de ajuste o cancelación
    
    private List<LineaComprobante> lineas;

    public static class LineaComprobante {
        private String codigoBarras;
        private String descripcion;
        private BigDecimal cantidad;
        private String unidad;
        private BigDecimal valorUnitario;
        private BigDecimal subtotal;

        public LineaComprobante() {}

        public LineaComprobante(String codigoBarras, String descripcion, BigDecimal cantidad, String unidad, BigDecimal valorUnitario, BigDecimal subtotal) {
            this.codigoBarras = codigoBarras;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.unidad = unidad;
            this.valorUnitario = valorUnitario;
            this.subtotal = subtotal;
        }

        public String getCodigoBarras() { return codigoBarras; }
        public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public BigDecimal getCantidad() { return cantidad; }
        public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
        public String getUnidad() { return unidad; }
        public void setUnidad(String unidad) { this.unidad = unidad; }
        public BigDecimal getValorUnitario() { return valorUnitario; }
        public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }

    public DocumentoComprobanteResponse() {}

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getConsecutivo() { return consecutivo; }
    public void setConsecutivo(String consecutivo) { this.consecutivo = consecutivo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getFerreteriaNombre() { return ferreteriaNombre; }
    public void setFerreteriaNombre(String ferreteriaNombre) { this.ferreteriaNombre = ferreteriaNombre; }
    public String getFerreteriaNit() { return ferreteriaNit; }
    public void setFerreteriaNit(String ferreteriaNit) { this.ferreteriaNit = ferreteriaNit; }
    public String getFerreteriaDireccion() { return ferreteriaDireccion; }
    public void setFerreteriaDireccion(String ferreteriaDireccion) { this.ferreteriaDireccion = ferreteriaDireccion; }
    public String getFerreteriaTelefono() { return ferreteriaTelefono; }
    public void setFerreteriaTelefono(String ferreteriaTelefono) { this.ferreteriaTelefono = ferreteriaTelefono; }
    public String getTerceroNombre() { return terceroNombre; }
    public void setTerceroNombre(String terceroNombre) { this.terceroNombre = terceroNombre; }
    public String getTerceroDocumento() { return terceroDocumento; }
    public void setTerceroDocumento(String terceroDocumento) { this.terceroDocumento = terceroDocumento; }
    public String getTerceroDireccion() { return terceroDireccion; }
    public void setTerceroDireccion(String terceroDireccion) { this.terceroDireccion = terceroDireccion; }
    public String getTerceroTelefono() { return terceroTelefono; }
    public void setTerceroTelefono(String terceroTelefono) { this.terceroTelefono = terceroTelefono; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getMontoPagado() { return montoPagado; }
    public void setMontoPagado(BigDecimal montoPagado) { this.montoPagado = montoPagado; }
    public BigDecimal getCambio() { return cambio; }
    public void setCambio(BigDecimal cambio) { this.cambio = cambio; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getUsuarioResponsable() { return usuarioResponsable; }
    public void setUsuarioResponsable(String usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public List<LineaComprobante> getLineas() { return lineas; }
    public void setLineas(List<LineaComprobante> lineas) { this.lineas = lineas; }
}
