package com.ferreteria.inventario.customer.dto.response;

public class TerceroResponse {
    private Long id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String razonSocial;
    private String tipoPersona;
    private String telefono;
    private String email;
    private String direccion;
    private boolean esCliente;
    private boolean esProveedor;
    private boolean activo;

    public TerceroResponse() {}

    public TerceroResponse(Long id, String tipoDocumento, String numeroDocumento, String razonSocial,
                           String tipoPersona, String telefono, String email, String direccion,
                           boolean esCliente, boolean esProveedor, boolean activo) {
        this.id = id;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.razonSocial = razonSocial;
        this.tipoPersona = tipoPersona;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.esCliente = esCliente;
        this.esProveedor = esProveedor;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getTipoPersona() { return tipoPersona; }
    public void setTipoPersona(String tipoPersona) { this.tipoPersona = tipoPersona; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public boolean isEsCliente() { return esCliente; }
    public void setEsCliente(boolean esCliente) { this.esCliente = esCliente; }
    public boolean isEsProveedor() { return esProveedor; }
    public void setEsProveedor(boolean esProveedor) { this.esProveedor = esProveedor; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
