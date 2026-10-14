package com.ferreteria.inventario.inventory.entity;

import com.ferreteria.inventario.catalog.entity.Referencia;
import com.ferreteria.inventario.user.entity.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad InventarioMovimiento: registro inmutable del Kárdex de inventario.
 */
@Entity
@Table(
    name = "inventario_movimiento",
    indexes = {
        @Index(name = "idx_mov_ref_fecha", columnList = "referencia_id, fecha_hora"),
        @Index(name = "idx_mov_doc", columnList = "documento_tipo, documento_id")
    }
)
public class InventarioMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "referencia_id", nullable = false)
    private Referencia referencia;

    @Column(name = "tipo_movimiento", nullable = false, length = 30)
    private String tipoMovimiento; // INICIALIZACION, COMPRA, VENTA, DEVOLUCION_CLIENTE, AJUSTE_ENTRADA, AJUSTE_SALIDA, CANCELACION_VENTA, CANCELACION_COMPRA

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario_movimiento", nullable = false, precision = 15, scale = 4)
    private BigDecimal costoUnitarioMovimiento;

    @Column(name = "saldo_resultante", nullable = false, precision = 12, scale = 3)
    private BigDecimal saldoResultante;

    @Column(name = "documento_tipo", nullable = false, length = 20)
    private String documentoTipo; // VENTA, COMPRA, DEVOLUCION, AJUSTE

    @Column(name = "documento_id", nullable = false)
    private Long documentoId;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public InventarioMovimiento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Referencia getReferencia() { return referencia; }
    public void setReferencia(Referencia referencia) { this.referencia = referencia; }

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

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
