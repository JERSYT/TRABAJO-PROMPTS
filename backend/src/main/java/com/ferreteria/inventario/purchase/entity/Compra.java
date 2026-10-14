package com.ferreteria.inventario.purchase.entity;

import com.ferreteria.inventario.customer.entity.Tercero;
import com.ferreteria.inventario.user.entity.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consecutivo", nullable = false, unique = true, length = 25)
    private String consecutivo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Tercero proveedor;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "total", nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "RECIBIDA"; // RECIBIDA, CANCELADA

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompraLinea> lineas = new ArrayList<>();

    public Compra() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConsecutivo() { return consecutivo; }
    public void setConsecutivo(String consecutivo) { this.consecutivo = consecutivo; }

    public Tercero getProveedor() { return proveedor; }
    public void setProveedor(Tercero proveedor) { this.proveedor = proveedor; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<CompraLinea> getLineas() { return lineas; }
    public void setLineas(List<CompraLinea> lineas) { this.lineas = lineas; }

    public void addLinea(CompraLinea linea) {
        lineas.add(linea);
        linea.setCompra(this);
    }
}
