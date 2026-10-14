package com.ferreteria.inventario.document.service;

import com.ferreteria.inventario.document.dto.response.DocumentoComprobanteResponse;
import com.ferreteria.inventario.purchase.entity.Compra;
import com.ferreteria.inventario.purchase.repository.CompraRepository;
import com.ferreteria.inventario.returns.entity.Devolucion;
import com.ferreteria.inventario.returns.repository.DevolucionRepository;
import com.ferreteria.inventario.sale.entity.Venta;
import com.ferreteria.inventario.sale.repository.VentaRepository;
import com.ferreteria.inventario.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de generar las representaciones estructuradas
 * de comprobantes imprimibles para ventas, compras y devoluciones (RN-12).
 */
@Service
public class DocumentService {

    private final VentaRepository ventaRepository;
    private final CompraRepository compraRepository;
    private final DevolucionRepository devolucionRepository;

    public DocumentService(VentaRepository ventaRepository,
                           CompraRepository compraRepository,
                           DevolucionRepository devolucionRepository) {
        this.ventaRepository = ventaRepository;
        this.compraRepository = compraRepository;
        this.devolucionRepository = devolucionRepository;
    }

    @Transactional(readOnly = true)
    public DocumentoComprobanteResponse obtenerComprobanteVenta(String consecutivo) {
        Venta v = ventaRepository.findByConsecutivo(consecutivo)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante de venta no encontrado con consecutivo: " + consecutivo));

        DocumentoComprobanteResponse doc = new DocumentoComprobanteResponse();
        doc.setTipoDocumento("VENTA");
        doc.setConsecutivo(v.getConsecutivo());
        doc.setFechaHora(v.getFechaHora());
        doc.setTerceroNombre(v.getCliente().getRazonSocial());
        doc.setTerceroDocumento(v.getCliente().getTipoDocumento() + " " + v.getCliente().getNumeroDocumento());
        doc.setTerceroDireccion(v.getCliente().getDireccion());
        doc.setTerceroTelefono(v.getCliente().getTelefono());
        doc.setSubtotal(v.getSubtotal());
        doc.setTotal(v.getTotal());
        doc.setMontoPagado(v.getMontoPagadoEfectivo());
        doc.setCambio(v.getCambioDevuelto());
        doc.setEstado(v.getEstado());
        doc.setUsuarioResponsable(v.getUsuario() != null ? v.getUsuario().getNombreCompleto() : "SISTEMA");
        doc.setMotivo(v.getMotivoCancelacion());

        var lineas = v.getLineas().stream().map(l -> new DocumentoComprobanteResponse.LineaComprobante(
                l.getReferencia().getCodigoBarras(),
                l.getReferencia().getNombreCompleto(),
                l.getCantidad(),
                l.getReferencia().getUnidadMedida().getCodigo(),
                l.getPrecioUnitario(),
                l.getSubtotal()
        )).toList();
        doc.setLineas(lineas);

        return doc;
    }

    @Transactional(readOnly = true)
    public DocumentoComprobanteResponse obtenerComprobanteCompra(String consecutivo) {
        Compra c = compraRepository.findByConsecutivo(consecutivo)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante de compra no encontrado con consecutivo: " + consecutivo));

        DocumentoComprobanteResponse doc = new DocumentoComprobanteResponse();
        doc.setTipoDocumento("COMPRA");
        doc.setConsecutivo(c.getConsecutivo());
        doc.setFechaHora(c.getFechaHora());
        doc.setTerceroNombre(c.getProveedor().getRazonSocial());
        doc.setTerceroDocumento(c.getProveedor().getTipoDocumento() + " " + c.getProveedor().getNumeroDocumento());
        doc.setTerceroDireccion(c.getProveedor().getDireccion());
        doc.setTerceroTelefono(c.getProveedor().getTelefono());
        doc.setSubtotal(c.getTotal());
        doc.setTotal(c.getTotal());
        doc.setEstado(c.getEstado());
        doc.setUsuarioResponsable(c.getUsuario() != null ? c.getUsuario().getNombreCompleto() : "SISTEMA");
        doc.setMotivo(c.getMotivoCancelacion());

        var lineas = c.getLineas().stream().map(l -> new DocumentoComprobanteResponse.LineaComprobante(
                l.getReferencia().getCodigoBarras(),
                l.getReferencia().getNombreCompleto(),
                l.getCantidad(),
                l.getReferencia().getUnidadMedida().getCodigo(),
                l.getCostoUnitario(),
                l.getSubtotal()
        )).toList();
        doc.setLineas(lineas);

        return doc;
    }

    @Transactional(readOnly = true)
    public DocumentoComprobanteResponse obtenerComprobanteDevolucion(String consecutivo) {
        Devolucion d = devolucionRepository.findByConsecutivo(consecutivo)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante de devolución no encontrado con consecutivo: " + consecutivo));

        DocumentoComprobanteResponse doc = new DocumentoComprobanteResponse();
        doc.setTipoDocumento("DEVOLUCION");
        doc.setConsecutivo(d.getConsecutivo());
        doc.setFechaHora(d.getFechaHora());
        if (d.getCliente() != null) {
            doc.setTerceroNombre(d.getCliente().getRazonSocial());
            doc.setTerceroDocumento(d.getCliente().getTipoDocumento() + " " + d.getCliente().getNumeroDocumento());
            doc.setTerceroDireccion(d.getCliente().getDireccion());
            doc.setTerceroTelefono(d.getCliente().getTelefono());
        }
        doc.setEstado(d.getEstado());
        doc.setUsuarioResponsable(d.getUsuario() != null ? d.getUsuario().getNombreCompleto() : "SISTEMA");
        doc.setMotivo(d.getMotivo());

        var lineas = d.getLineas().stream().map(l -> new DocumentoComprobanteResponse.LineaComprobante(
                l.getReferencia().getCodigoBarras(),
                l.getReferencia().getNombreCompleto(),
                l.getCantidad(),
                l.getReferencia().getUnidadMedida().getCodigo(),
                l.getCostoUnitarioAplicado(),
                l.getCantidad().multiply(l.getCostoUnitarioAplicado())
        )).toList();
        doc.setLineas(lineas);

        return doc;
    }
}
