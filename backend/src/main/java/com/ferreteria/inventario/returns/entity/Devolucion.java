package com.ferreteria.inventario.returns.entity;

import com.ferreteria.inventario.customer.entity.Tercero;
import com.ferreteria.inventario.sale.entity.Venta;
import com.ferreteria.inventario.user.entity.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devolucion")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consecutivo", nullable = false, unique = true, length = 25)
    private String consecutivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta; // Opcional si es no vinculada

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Tercero cliente;

    @Column(name = "tipo_devolucion", nullable = false, length = 20)
    private String tipoDevolucion; // VINCULADA, NO_VINCULADA

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "APROBADA";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "devolucion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DevolucionLinea> lineas = new ArrayList<>();

    public Devolucion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConsecutivo() { return consecutivo; }
    public void setConsecutivo(String consecutivo) { this.consecutivo = consecutivo; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public Tercero getCliente() { return cliente; }
    public void setCliente(Tercero cliente) { this.cliente = cliente; }

    public String getTipoDevolucion() { return tipoDevolucion; }
    public void setTipoDevolucion(String tipoDevolucion) { this.tipoDevolucion = tipoDevolucion; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<DevolucionLinea> getLineas() { return lineas; }
    public void setLineas(List<DevolucionLinea> lineas) { this.lineas = lineas; }

    public void addLinea(DevolucionLinea linea) {
        lineas.add(linea);
        linea.setDevolucion(this);
    }
}
