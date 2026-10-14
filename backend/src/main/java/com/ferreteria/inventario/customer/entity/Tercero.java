package com.ferreteria.inventario.customer.entity;

import jakarta.persistence.*;

/**
 * Entidad Tercero: representa clientes y proveedores de forma unificada (RN-10).
 */
@Entity
@Table(name = "tercero")
public class Tercero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_documento", nullable = false, length = 10)
    private String tipoDocumento; // CC, NIT, CE, PASAPORTE

    @Column(name = "numero_documento", nullable = false, unique = true, length = 30)
    private String numeroDocumento;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "tipo_persona", nullable = false, length = 15)
    private String tipoPersona; // NATURAL, JURIDICA

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "direccion", length = 150)
    private String direccion;

    @Column(name = "es_cliente", nullable = false)
    private boolean esCliente = true;

    @Column(name = "es_proveedor", nullable = false)
    private boolean esProveedor = false;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public Tercero() {}

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
