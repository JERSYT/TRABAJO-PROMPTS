package com.ferreteria.inventario.user.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public class UsuarioResponse {
    private Long id;
    private String username;
    private String nombreCompleto;
    private String email;
    private boolean activo;
    private Set<String> roles;
    private LocalDateTime fechaCreacion;

    public UsuarioResponse() {}

    public UsuarioResponse(Long id, String username, String nombreCompleto, String email, boolean activo, Set<String> roles, LocalDateTime fechaCreacion) {
        this.id = id;
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.activo = activo;
        this.roles = roles;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
