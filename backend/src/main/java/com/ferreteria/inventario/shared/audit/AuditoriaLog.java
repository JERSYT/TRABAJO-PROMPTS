package com.ferreteria.inventario.shared.audit;

import com.ferreteria.inventario.user.entity.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad AuditoriaLog: registro inmutable de auditoría básica (RN-11).
 * Almacena exclusivamente: usuario responsable, fecha/hora y descripción.
 */
@Entity
@Table(
    name = "auditoria_log",
    indexes = {
        @Index(name = "idx_auditoria_fecha", columnList = "fecha_hora")
    }
)
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "modulo", nullable = false, length = 40)
    private String modulo;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    public AuditoriaLog() {}

    public AuditoriaLog(Usuario usuario, String modulo, String accion, String descripcion) {
        this.usuario = usuario;
        this.modulo = modulo;
        this.accion = accion;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
