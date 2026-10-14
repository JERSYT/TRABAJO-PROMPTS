package com.ferreteria.inventario.auth.dto.response;

import java.util.List;

public class AuthResponse {
    private Long id;
    private String username;
    private String nombreCompleto;
    private String email;
    private List<String> roles;

    public AuthResponse() {}

    public AuthResponse(Long id, String username, String nombreCompleto, String email, List<String> roles) {
        this.id = id;
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
