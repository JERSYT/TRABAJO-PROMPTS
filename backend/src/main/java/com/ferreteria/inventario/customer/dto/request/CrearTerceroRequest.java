package com.ferreteria.inventario.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CrearTerceroRequest {

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Pattern(regexp = "^(CC|NIT|CE|PASAPORTE)$", message = "Tipo de documento inválido (CC, NIT, CE, PASAPORTE)")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 4, max = 30, message = "El número de documento debe tener entre 4 y 30 caracteres")
    @Pattern(regexp = "^[0-9]+(-[0-9kK])?$", message = "El número de documento debe contener únicamente dígitos numéricos (o guión para dígito de verificación en NIT)")
    private String numeroDocumento;

    @NotBlank(message = "La razón social o nombre completo es obligatoria")
    @Size(min = 2, max = 150, message = "La razón social debe tener entre 2 y 150 caracteres")
    private String razonSocial;

    @NotBlank(message = "El tipo de persona es obligatorio")
    @Pattern(regexp = "^(NATURAL|JURIDICA)$", message = "Tipo de persona inválido (NATURAL o JURIDICA)")
    private String tipoPersona;

    @Pattern(regexp = "^$|^[0-9+\\s-]{7,25}$", message = "El teléfono debe contener únicamente dígitos numéricos (mínimo 7)")
    private String telefono;
    private String email;
    private String direccion;

    @NotNull(message = "Debe indicar si es cliente")
    private Boolean esCliente;

    @NotNull(message = "Debe indicar si es proveedor")
    private Boolean esProveedor;

    public CrearTerceroRequest() {}

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

    public Boolean getEsCliente() { return esCliente; }
    public void setEsCliente(Boolean esCliente) { this.esCliente = esCliente; }

    public Boolean getEsProveedor() { return esProveedor; }
    public void setEsProveedor(Boolean esProveedor) { this.esProveedor = esProveedor; }
}
