package com.ferreteria.inventario.inventory.entity;

import com.ferreteria.inventario.catalog.entity.Referencia;
import com.ferreteria.inventario.user.entity.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad AjusteInventario: operaciones manuales autorizadas exclusivamente
 * por el Administrador con motivo documentado obligatorio (RN-08).
 */
@Entity
@Table(name = "ajuste_inventario")
public class AjusteInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consecutivo", nullable = false, unique = true, length = 25)
    private String consecutivo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "referencia_id", nullable = false)
    private Referencia referencia;

    @Column(name = "tipo_ajuste", nullable = false, length = 20)
    private String tipoAjuste; // INCREMENTO, DISMINUCION

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario_referencia", nullable = false, precision = 15, scale = 4)
    private BigDecimal costoUnitarioReferencia;

    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public AjusteInventario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConsecutivo() { return consecutivo; }
    public void setConsecutivo(String consecutivo) { this.consecutivo = consecutivo; }

    public Referencia getReferencia() { return referencia; }
    public void setReferencia(Referencia referencia) { this.referencia = referencia; }

    public String getTipoAjuste() { return tipoAjuste; }
    public void setTipoAjuste(String tipoAjuste) { this.tipoAjuste = tipoAjuste; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getCostoUnitarioReferencia() { return costoUnitarioReferencia; }
    public void setCostoUnitarioReferencia(BigDecimal costoUnitarioReferencia) { this.costoUnitarioReferencia = costoUnitarioReferencia; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
