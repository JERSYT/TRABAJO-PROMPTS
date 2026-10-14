package com.ferreteria.inventario.document.entity;

import jakarta.persistence.*;

/**
 * Entidad ComprobanteConsecutivo: administra la numeración secuencial
 * independiente y correlativa de comprobantes (RN-12).
 */
@Entity
@Table(name = "comprobante_consecutivo")
public class ComprobanteConsecutivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_documento", nullable = false, unique = true, length = 25)
    private String tipoDocumento; // VENTA, COMPRA, DEVOLUCION, AJUSTE

    @Column(name = "prefijo", nullable = false, length = 10)
    private String prefijo;

    @Column(name = "ultimo_numero", nullable = false)
    private Long ultimoNumero = 0L;

    public ComprobanteConsecutivo() {}

    public ComprobanteConsecutivo(String tipoDocumento, String prefijo, Long ultimoNumero) {
        this.tipoDocumento = tipoDocumento;
        this.prefijo = prefijo;
        this.ultimoNumero = ultimoNumero;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getPrefijo() { return prefijo; }
    public void setPrefijo(String prefijo) { this.prefijo = prefijo; }

    public Long getUltimoNumero() { return ultimoNumero; }
    public void setUltimoNumero(Long ultimoNumero) { this.ultimoNumero = ultimoNumero; }

    public String generarSiguienteConsecutivo() {
        this.ultimoNumero++;
        return String.format("%s%06d", prefijo, ultimoNumero);
    }
}
