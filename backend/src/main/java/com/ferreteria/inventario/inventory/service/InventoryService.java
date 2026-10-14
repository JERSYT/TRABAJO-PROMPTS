package com.ferreteria.inventario.inventory.service;

import com.ferreteria.inventario.catalog.entity.Referencia;
import com.ferreteria.inventario.catalog.repository.ReferenciaRepository;
import com.ferreteria.inventario.inventory.dto.request.InventarioInicialRequest;
import com.ferreteria.inventario.inventory.dto.response.InventarioMovimientoResponse;
import com.ferreteria.inventario.inventory.dto.response.InventarioSaldoResponse;
import com.ferreteria.inventario.inventory.entity.InventarioMovimiento;
import com.ferreteria.inventario.inventory.entity.InventarioSaldo;
import com.ferreteria.inventario.inventory.repository.InventarioMovimientoRepository;
import com.ferreteria.inventario.inventory.repository.InventarioSaldoRepository;
import com.ferreteria.inventario.shared.audit.AuditService;
import com.ferreteria.inventario.shared.exception.BusinessException;
import com.ferreteria.inventario.shared.exception.InsufficientStockException;
import com.ferreteria.inventario.shared.exception.ResourceNotFoundException;
import com.ferreteria.inventario.user.entity.Usuario;
import com.ferreteria.inventario.user.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Núcleo de Gestión de Inventario:
 * Implementa el control transaccional de existencias, bloqueo pesimista contra stock negativo (RN-01),
 * cálculo del Costo Promedio Ponderado (RN-04) y registro inmutable en Kárdex.
 */
@Service
public class InventoryService {

    private final InventarioSaldoRepository saldoRepository;
    private final InventarioMovimientoRepository movimientoRepository;
    private final ReferenciaRepository referenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;

    public InventoryService(InventarioSaldoRepository saldoRepository,
                            InventarioMovimientoRepository movimientoRepository,
                            ReferenciaRepository referenciaRepository,
                            UsuarioRepository usuarioRepository,
                            AuditService auditService) {
        this.saldoRepository = saldoRepository;
        this.movimientoRepository = movimientoRepository;
        this.referenciaRepository = referenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<InventarioSaldoResponse> listarSaldos(String search, boolean bajoStock, Pageable pageable) {
        return saldoRepository.searchSaldos(search, bajoStock, pageable).map(this::mapSaldoToResponse);
    }

    @Transactional(readOnly = true)
    public Page<InventarioMovimientoResponse> listarMovimientosPorReferencia(Long referenciaId, Pageable pageable) {
        return movimientoRepository.findByReferenciaIdOrderByFechaHoraDesc(referenciaId, pageable)
                .map(this::mapMovimientoToResponse);
    }

    /**
     * Carga de inventario inicial (RN-04, RN-08).
     * Solo para referencias con saldo en cero y sin movimientos previos.
     */
    @Transactional
    public InventarioSaldoResponse inicializarInventario(InventarioInicialRequest request, String username) {
        Referencia ref = referenciaRepository.findById(request.getReferenciaId())
                .orElseThrow(() -> new ResourceNotFoundException("Referencia no encontrada con ID: " + request.getReferenciaId()));

        if (movimientoRepository.existsByReferenciaId(ref.getId())) {
            throw new BusinessException("La referencia ya registra movimientos en el sistema. Utilice un ajuste o compra.");
        }

        InventarioSaldo saldo = saldoRepository.findByReferenciaIdForUpdate(ref.getId())
                .orElseGet(() -> new InventarioSaldo(ref, BigDecimal.ZERO, BigDecimal.ZERO));

        saldo.setCantidadDisponible(request.getCantidadInicial());
        saldo.setCostoPromedioPonderado(request.getCostoUnitario().setScale(4, RoundingMode.HALF_UP));
        InventarioSaldo saldoGuardado = saldoRepository.save(saldo);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        // Registrar movimiento Kárdex inicial
        registrarMovimientoInterno(
                ref,
                "INICIALIZACION",
                request.getCantidadInicial(),
                request.getCostoUnitario(),
                saldo.getCantidadDisponible(),
                "AJUSTE",
                0L,
                "Inventario inicial de arranque",
                usuario
        );

        auditService.registrarEvento(usuario, "INVENTARIO", "INVENTARIO_INICIAL",
                "Carga inicial de " + request.getCantidadInicial() + " unidades a costo $" + request.getCostoUnitario() + " para " + ref.getCodigoBarras());

        return mapSaldoToResponse(saldoGuardado);
    }

    /**
     * Descuento atómico de existencias con Bloqueo Pesimista (RN-01).
     * Retorna el costo histórico unitario que tenía el producto al momento del descuento.
     */
    @Transactional
    public BigDecimal descontarStockConBloqueo(Long referenciaId, BigDecimal cantidad, String docTipo, Long docId, String motivo, Usuario usuario) {
        InventarioSaldo saldo = saldoRepository.findByReferenciaIdForUpdate(referenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo no encontrado para la referencia ID: " + referenciaId));

        if (saldo.getCantidadDisponible().compareTo(cantidad) < 0) {
            throw new InsufficientStockException("Stock insuficiente para " + saldo.getReferencia().getCodigoBarras() +
                    " (" + saldo.getReferencia().getNombreCompleto() + "). Disponibles: " +
                    saldo.getCantidadDisponible() + ", Solicitadas: " + cantidad);
        }

        BigDecimal nuevoSaldo = saldo.getCantidadDisponible().subtract(cantidad);
        saldo.setCantidadDisponible(nuevoSaldo);
        saldoRepository.save(saldo);

        BigDecimal costoHistorico = saldo.getCostoPromedioPonderado();

        registrarMovimientoInterno(
                saldo.getReferencia(),
                "VENTA",
                cantidad,
                costoHistorico,
                nuevoSaldo,
                docTipo,
                docId,
                motivo,
                usuario
        );

        return costoHistorico;
    }

    /**
     * Incremento de existencias por compra y recálculo del Costo Promedio Ponderado (RN-03, RN-04).
     */
    @Transactional
    public void procesarEntradaCompra(Long referenciaId, BigDecimal cantidad, BigDecimal costoUnitario, Long compraId, Usuario usuario) {
        InventarioSaldo saldo = saldoRepository.findByReferenciaIdForUpdate(referenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo no encontrado para la referencia ID: " + referenciaId));

        BigDecimal stockActual = saldo.getCantidadDisponible();
        BigDecimal cppActual = saldo.getCostoPromedioPonderado();

        // Fórmula de Costo Promedio Ponderado:
        // Nuevo CPP = [(StockActual * CPPActual) + (CantidadComprada * CostoUnitarioCompra)] / (StockActual + CantidadComprada)
        BigDecimal valorInventarioActual = stockActual.multiply(cppActual);
        BigDecimal valorCompraNueva = cantidad.multiply(costoUnitario);
        BigDecimal nuevoStock = stockActual.add(cantidad);

        BigDecimal nuevoCpp;
        if (nuevoStock.compareTo(BigDecimal.ZERO) > 0) {
            nuevoCpp = valorInventarioActual.add(valorCompraNueva).divide(nuevoStock, 4, RoundingMode.HALF_UP);
        } else {
            nuevoCpp = costoUnitario.setScale(4, RoundingMode.HALF_UP);
        }

        saldo.setCantidadDisponible(nuevoStock);
        saldo.setCostoPromedioPonderado(nuevoCpp);
        saldoRepository.save(saldo);

        registrarMovimientoInterno(
                saldo.getReferencia(),
                "COMPRA",
                cantidad,
                costoUnitario,
                nuevoStock,
                "COMPRA",
                compraId,
                "Recepción de compra a proveedor",
                usuario
        );
    }

    /**
     * Reintegro de existencias por devolución o anulación de venta (RN-07).
     */
    @Transactional
    public void reintegrarStock(Long referenciaId, BigDecimal cantidad, BigDecimal costoAplicado, String docTipo, Long docId, String motivo, Usuario usuario) {
        InventarioSaldo saldo = saldoRepository.findByReferenciaIdForUpdate(referenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo no encontrado para la referencia ID: " + referenciaId));

        BigDecimal nuevoStock = saldo.getCantidadDisponible().add(cantidad);
        saldo.setCantidadDisponible(nuevoStock);
        saldoRepository.save(saldo);

        registrarMovimientoInterno(
                saldo.getReferencia(),
                "DEVOLUCION_CLIENTE",
                cantidad,
                costoAplicado,
                nuevoStock,
                docTipo,
                docId,
                motivo,
                usuario
        );
    }

    /**
     * Compensación de salida de existencias por anulación de compra.
     */
    @Transactional
    public void compensarSalidaCompra(Long referenciaId, BigDecimal cantidad, BigDecimal costoUnitario, Long compraId, String motivo, Usuario usuario) {
        InventarioSaldo saldo = saldoRepository.findByReferenciaIdForUpdate(referenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo no encontrado para la referencia ID: " + referenciaId));

        if (saldo.getCantidadDisponible().compareTo(cantidad) < 0) {
            throw new InsufficientStockException("No se puede anular la compra. Parte del stock ya fue vendido o consumido. Stock actual: " +
                    saldo.getCantidadDisponible() + ", Requerido para reversión: " + cantidad);
        }

        BigDecimal nuevoStock = saldo.getCantidadDisponible().subtract(cantidad);
        saldo.setCantidadDisponible(nuevoStock);
        saldoRepository.save(saldo);

        registrarMovimientoInterno(
                saldo.getReferencia(),
                "CANCELACION_COMPRA",
                cantidad,
                costoUnitario,
                nuevoStock,
                "COMPRA",
                compraId,
                motivo,
                usuario
        );
    }

    /**
     * Compensación de entrada de existencias por anulación de venta.
     */
    @Transactional
    public void compensarEntradaVenta(Long referenciaId, BigDecimal cantidad, BigDecimal costoHistorico, Long ventaId, String motivo, Usuario usuario) {
        InventarioSaldo saldo = saldoRepository.findByReferenciaIdForUpdate(referenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo no encontrado para la referencia ID: " + referenciaId));

        BigDecimal nuevoStock = saldo.getCantidadDisponible().add(cantidad);
        saldo.setCantidadDisponible(nuevoStock);
        saldoRepository.save(saldo);

        registrarMovimientoInterno(
                saldo.getReferencia(),
                "CANCELACION_VENTA",
                cantidad,
                costoHistorico,
                nuevoStock,
                "VENTA",
                ventaId,
                motivo,
                usuario
        );
    }

    /**
     * Ajuste manual de inventario autorizado (RN-08).
     */
    @Transactional
    public void procesarAjusteManual(Long referenciaId, String tipoAjuste, BigDecimal cantidad, String motivo, Long ajusteDocId, Usuario usuario) {
        InventarioSaldo saldo = saldoRepository.findByReferenciaIdForUpdate(referenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo no encontrado para la referencia ID: " + referenciaId));

        BigDecimal nuevoStock;
        String tipoMov;

        if ("INCREMENTO".equalsIgnoreCase(tipoAjuste)) {
            nuevoStock = saldo.getCantidadDisponible().add(cantidad);
            tipoMov = "AJUSTE_ENTRADA";
        } else {
            if (saldo.getCantidadDisponible().compareTo(cantidad) < 0) {
                throw new InsufficientStockException("El ajuste de disminución supera el saldo actual disponible (" + saldo.getCantidadDisponible() + ")");
            }
            nuevoStock = saldo.getCantidadDisponible().subtract(cantidad);
            tipoMov = "AJUSTE_SALIDA";
        }

        saldo.setCantidadDisponible(nuevoStock);
        saldoRepository.save(saldo);

        registrarMovimientoInterno(
                saldo.getReferencia(),
                tipoMov,
                cantidad,
                saldo.getCostoPromedioPonderado(),
                nuevoStock,
                "AJUSTE",
                ajusteDocId,
                motivo,
                usuario
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerCostoPromedioVigente(Long referenciaId) {
        return saldoRepository.findByReferenciaId(referenciaId)
                .map(InventarioSaldo::getCostoPromedioPonderado)
                .orElse(BigDecimal.ZERO);
    }

    private void registrarMovimientoInterno(Referencia ref, String tipoMov, BigDecimal cant, BigDecimal costo,
                                           BigDecimal saldoRes, String docTipo, Long docId, String motivo, Usuario usuario) {
        InventarioMovimiento mov = new InventarioMovimiento();
        mov.setReferencia(ref);
        mov.setTipoMovimiento(tipoMov);
        mov.setCantidad(cant);
        mov.setCostoUnitarioMovimiento(costo.setScale(4, RoundingMode.HALF_UP));
        mov.setSaldoResultante(saldoRes);
        mov.setDocumentoTipo(docTipo);
        mov.setDocumentoId(docId);
        mov.setMotivo(motivo);
        mov.setFechaHora(LocalDateTime.now());
        mov.setUsuario(usuario);

        movimientoRepository.save(mov);
    }

    private InventarioSaldoResponse mapSaldoToResponse(InventarioSaldo s) {
        InventarioSaldoResponse res = new InventarioSaldoResponse();
        res.setId(s.getId());
        res.setReferenciaId(s.getReferencia().getId());
        res.setCodigoBarras(s.getReferencia().getCodigoBarras());
        res.setProductoNombre(s.getReferencia().getProducto().getNombre());
        res.setMarcaNombre(s.getReferencia().getMarca().getNombre());
        res.setPresentacionNombre(s.getReferencia().getPresentacion().getNombre());
        res.setUnidadMedida(s.getReferencia().getUnidadMedida().getCodigo());
        res.setCantidadDisponible(s.getCantidadDisponible());
        res.setCostoPromedioPonderado(s.getCostoPromedioPonderado());
        res.setFechaActualizacion(s.getFechaActualizacion());
        return res;
    }

    private InventarioMovimientoResponse mapMovimientoToResponse(InventarioMovimiento m) {
        InventarioMovimientoResponse res = new InventarioMovimientoResponse();
        res.setId(m.getId());
        res.setReferenciaId(m.getReferencia().getId());
        res.setProductoNombre(m.getReferencia().getNombreCompleto());
        res.setTipoMovimiento(m.getTipoMovimiento());
        res.setCantidad(m.getCantidad());
        res.setCostoUnitarioMovimiento(m.getCostoUnitarioMovimiento());
        res.setSaldoResultante(m.getSaldoResultante());
        res.setDocumentoTipo(m.getDocumentoTipo());
        res.setDocumentoId(m.getDocumentoId());
        res.setMotivo(m.getMotivo());
        res.setFechaHora(m.getFechaHora());
        res.setUsuarioNombre(m.getUsuario() != null ? m.getUsuario().getNombreCompleto() : "SISTEMA");
        return res;
    }
}
