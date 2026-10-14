package com.ferreteria.inventario.catalog.service;

import com.ferreteria.inventario.catalog.dto.request.ActualizarPrecioRequest;
import com.ferreteria.inventario.catalog.dto.request.CrearReferenciaRequest;
import com.ferreteria.inventario.catalog.dto.response.ReferenciaResponse;
import com.ferreteria.inventario.catalog.entity.Marca;
import com.ferreteria.inventario.catalog.entity.Presentacion;
import com.ferreteria.inventario.catalog.entity.Producto;
import com.ferreteria.inventario.catalog.entity.Referencia;
import com.ferreteria.inventario.catalog.entity.UnidadMedida;
import com.ferreteria.inventario.catalog.repository.MarcaRepository;
import com.ferreteria.inventario.catalog.repository.PresentacionRepository;
import com.ferreteria.inventario.catalog.repository.ProductoRepository;
import com.ferreteria.inventario.catalog.repository.ReferenciaRepository;
import com.ferreteria.inventario.catalog.repository.UnidadMedidaRepository;
import com.ferreteria.inventario.inventory.entity.InventarioSaldo;
import com.ferreteria.inventario.inventory.repository.InventarioMovimientoRepository;
import com.ferreteria.inventario.inventory.repository.InventarioSaldoRepository;
import com.ferreteria.inventario.shared.audit.AuditService;
import com.ferreteria.inventario.shared.exception.BusinessException;
import com.ferreteria.inventario.shared.exception.ResourceNotFoundException;
import com.ferreteria.inventario.user.entity.Usuario;
import com.ferreteria.inventario.user.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CatalogService {

    private final ReferenciaRepository referenciaRepository;
    private final ProductoRepository productoRepository;
    private final MarcaRepository marcaRepository;
    private final PresentacionRepository presentacionRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;
    private final InventarioSaldoRepository saldoRepository;
    private final InventarioMovimientoRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;

    public CatalogService(ReferenciaRepository referenciaRepository,
                          ProductoRepository productoRepository,
                          MarcaRepository marcaRepository,
                          PresentacionRepository presentacionRepository,
                          UnidadMedidaRepository unidadMedidaRepository,
                          InventarioSaldoRepository saldoRepository,
                          InventarioMovimientoRepository movimientoRepository,
                          UsuarioRepository usuarioRepository,
                          AuditService auditService) {
        this.referenciaRepository = referenciaRepository;
        this.productoRepository = productoRepository;
        this.marcaRepository = marcaRepository;
        this.presentacionRepository = presentacionRepository;
        this.unidadMedidaRepository = unidadMedidaRepository;
        this.saldoRepository = saldoRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<ReferenciaResponse> listarReferencias(String search, Boolean activo, Pageable pageable) {
        return referenciaRepository.searchReferencias(search, activo, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ReferenciaResponse obtenerPorId(Long id) {
        Referencia ref = referenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con ID: " + id));
        return mapToResponse(ref);
    }

    @Transactional(readOnly = true)
    public ReferenciaResponse obtenerPorCodigoBarras(String codigoBarras) {
        Referencia ref = referenciaRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con código de barras: " + codigoBarras));
        return mapToResponse(ref);
    }

    @Transactional
    public ReferenciaResponse crearReferencia(CrearReferenciaRequest request, String username) {
        if (referenciaRepository.existsByCodigoBarras(request.getCodigoBarras())) {
            throw new BusinessException("Ya existe una referencia con el código de barras: " + request.getCodigoBarras());
        }

        if (referenciaRepository.existsByProductoIdAndMarcaIdAndPresentacionId(
                request.getProductoId(), request.getMarcaId(), request.getPresentacionId())) {
            throw new BusinessException("Ya existe una referencia con la misma combinación de Producto, Marca y Presentación");
        }

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + request.getProductoId()));
        Marca marca = marcaRepository.findById(request.getMarcaId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con ID: " + request.getMarcaId()));
        Presentacion presentacion = presentacionRepository.findById(request.getPresentacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Presentación no encontrada con ID: " + request.getPresentacionId()));
        UnidadMedida unidad = unidadMedidaRepository.findById(request.getUnidadMedidaId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada con ID: " + request.getUnidadMedidaId()));

        Referencia ref = new Referencia();
        ref.setProducto(producto);
        ref.setMarca(marca);
        ref.setPresentacion(presentacion);
        ref.setUnidadMedida(unidad);
        ref.setCodigoBarras(request.getCodigoBarras().trim());
        ref.setPrecioVenta(request.getPrecioVenta());
        ref.setActivo(true);

        Referencia guardada = referenciaRepository.save(ref);

        // Inicializar saldo en 0.000 y CPP en 0.0000
        InventarioSaldo saldo = new InventarioSaldo(guardada, BigDecimal.ZERO, BigDecimal.ZERO);
        saldoRepository.save(saldo);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        auditService.registrarEvento(usuario, "CATALOGO", "CREAR_REFERENCIA",
                "Creación de referencia " + guardada.getCodigoBarras() + " - " + guardada.getNombreCompleto());

        return mapToResponse(guardada);
    }

    @Transactional
    public ReferenciaResponse actualizarPrecio(Long id, ActualizarPrecioRequest request, String username) {
        Referencia ref = referenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con ID: " + id));

        BigDecimal precioAnterior = ref.getPrecioVenta();
        ref.setPrecioVenta(request.getNuevoPrecio());
        referenciaRepository.save(ref);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        auditService.registrarEvento(usuario, "CATALOGO", "MODIFICAR_PRECIO",
                "Cambio de precio para " + ref.getCodigoBarras() + ": $" + precioAnterior + " -> $" + request.getNuevoPrecio() +
                        (request.getMotivo() != null ? ". Motivo: " + request.getMotivo() : ""));

        return mapToResponse(ref);
    }

    @Transactional
    public void desactivarReferencia(Long id, String username) {
        Referencia ref = referenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con ID: " + id));

        // Regla RN-02: si tiene movimientos o transacciones, no se elimina físicamente; se desactiva
        boolean tieneHistorial = movimientoRepository.existsByReferenciaId(id);

        if (tieneHistorial) {
            ref.setActivo(false);
            referenciaRepository.save(ref);
        } else {
            // Si no tiene ningún historial se puede remover el saldo y la referencia
            saldoRepository.findByReferenciaId(id).ifPresent(saldoRepository::delete);
            referenciaRepository.delete(ref);
        }

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        auditService.registrarEvento(usuario, "CATALOGO", "DESACTIVAR_REFERENCIA",
                "Desactivación lógica de referencia " + ref.getCodigoBarras());
    }

    public Referencia obtenerEntidadPorId(Long id) {
        return referenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con ID: " + id));
    }

    private ReferenciaResponse mapToResponse(Referencia r) {
        ReferenciaResponse res = new ReferenciaResponse();
        res.setId(r.getId());
        res.setProductoId(r.getProducto().getId());
        res.setProductoNombre(r.getProducto().getNombre());
        res.setMarcaId(r.getMarca().getId());
        res.setMarcaNombre(r.getMarca().getNombre());
        res.setPresentacionId(r.getPresentacion().getId());
        res.setPresentacionNombre(r.getPresentacion().getNombre());
        res.setUnidadMedidaId(r.getUnidadMedida().getId());
        res.setUnidadMedidaCodigo(r.getUnidadMedida().getCodigo());
        res.setCodigoBarras(r.getCodigoBarras());
        res.setPrecioVenta(r.getPrecioVenta());
        res.setActivo(r.isActivo());

        saldoRepository.findByReferenciaId(r.getId()).ifPresent(saldo -> {
            res.setStockDisponible(saldo.getCantidadDisponible());
            res.setCostoPromedio(saldo.getCostoPromedioPonderado());
        });

        return res;
    }
}
