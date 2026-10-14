package com.ferreteria.inventario.shared.audit;

import java.time.LocalDateTime;

public class AuditoriaLogResponse {
    private Long id;
    private String usuarioNombre;
    private LocalDateTime fechaHora;
    private String modulo;
    private String accion;
    private String descripcion;

    public AuditoriaLogResponse() {}

    public AuditoriaLogResponse(Long id, String usuarioNombre, LocalDateTime fechaHora, String modulo, String accion, String descripcion) {
        this.id = id;
        this.usuarioNombre = usuarioNombre;
        this.fechaHora = fechaHora;
        this.modulo = modulo;
        this.accion = accion;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
