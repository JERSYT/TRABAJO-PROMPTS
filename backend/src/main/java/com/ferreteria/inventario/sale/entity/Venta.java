package com.ferreteria.inventario.sale.entity;

import com.ferreteria.inventario.customer.entity.Tercero;
import com.ferreteria.inventario.user.entity.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consecutivo", nullable = false, unique = true, length = 25)
    private String consecutivo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Tercero cliente;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total", nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "monto_pagado_efectivo", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoPagadoEfectivo;

    @Column(name = "cambio_devuelto", nullable = false, precision = 15, scale = 2)
    private BigDecimal cambioDevuelto = BigDecimal.ZERO;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "COMPLETADA"; // COMPLETADA, CANCELADA

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VentaLinea> lineas = new ArrayList<>();

    public Venta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConsecutivo() { return consecutivo; }
    public void setConsecutivo(String consecutivo) { this.consecutivo = consecutivo; }

    public Tercero getCliente() { return cliente; }
    public void setCliente(Tercero cliente) { this.cliente = cliente; }

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

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<VentaLinea> getLineas() { return lineas; }
    public void setLineas(List<VentaLinea> lineas) { this.lineas = lineas; }

    public void addLinea(VentaLinea linea) {
        lineas.add(linea);
        linea.setVenta(this);
    }
}
