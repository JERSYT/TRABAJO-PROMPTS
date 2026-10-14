package com.ferreteria.inventario.customer.service;

import com.ferreteria.inventario.customer.dto.request.CrearTerceroRequest;
import com.ferreteria.inventario.customer.dto.response.TerceroResponse;
import com.ferreteria.inventario.customer.entity.Tercero;
import com.ferreteria.inventario.customer.repository.TerceroRepository;
import com.ferreteria.inventario.shared.exception.BusinessException;
import com.ferreteria.inventario.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThirdPartyService {

    private final TerceroRepository terceroRepository;

    public ThirdPartyService(TerceroRepository terceroRepository) {
        this.terceroRepository = terceroRepository;
    }

    @Transactional(readOnly = true)
    public Page<TerceroResponse> listarTerceros(String search, String tipo, Pageable pageable) {
        boolean soloClientes = "CLIENTE".equalsIgnoreCase(tipo);
        boolean soloProveedores = "PROVEEDOR".equalsIgnoreCase(tipo);
        return terceroRepository.searchTerceros(search, soloClientes, soloProveedores, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public TerceroResponse obtenerPorId(Long id) {
        Tercero tercero = terceroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tercero no encontrado con ID: " + id));
        return mapToResponse(tercero);
    }

    @Transactional
    public TerceroResponse crearTercero(CrearTerceroRequest request) {
        if (terceroRepository.existsByNumeroDocumento(request.getNumeroDocumento())) {
            throw new BusinessException("Ya existe un tercero registrado con el documento: " + request.getNumeroDocumento());
        }

        Tercero tercero = new Tercero();
        tercero.setTipoDocumento(request.getTipoDocumento());
        tercero.setNumeroDocumento(request.getNumeroDocumento().trim());
        tercero.setRazonSocial(request.getRazonSocial().trim());
        tercero.setTipoPersona(request.getTipoPersona());
        tercero.setTelefono(request.getTelefono());
        tercero.setEmail(request.getEmail());
        tercero.setDireccion(request.getDireccion());
        tercero.setEsCliente(request.getEsCliente());
        tercero.setEsProveedor(request.getEsProveedor());
        tercero.setActivo(true);

        return mapToResponse(terceroRepository.save(tercero));
    }

    @Transactional
    public TerceroResponse actualizarTercero(Long id, CrearTerceroRequest request) {
        Tercero tercero = terceroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tercero no encontrado con ID: " + id));

        if (!tercero.getNumeroDocumento().equalsIgnoreCase(request.getNumeroDocumento().trim())
                && terceroRepository.existsByNumeroDocumento(request.getNumeroDocumento())) {
            throw new BusinessException("Ya existe otro tercero con el documento: " + request.getNumeroDocumento());
        }

        tercero.setTipoDocumento(request.getTipoDocumento());
        tercero.setNumeroDocumento(request.getNumeroDocumento().trim());
        tercero.setRazonSocial(request.getRazonSocial().trim());
        tercero.setTipoPersona(request.getTipoPersona());
        tercero.setTelefono(request.getTelefono());
        tercero.setEmail(request.getEmail());
        tercero.setDireccion(request.getDireccion());
        tercero.setEsCliente(request.getEsCliente());
        tercero.setEsProveedor(request.getEsProveedor());

        return mapToResponse(terceroRepository.save(tercero));
    }

    public Tercero obtenerEntidadPorId(Long id) {
        return terceroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tercero no encontrado con ID: " + id));
    }

    private TerceroResponse mapToResponse(Tercero t) {
        return new TerceroResponse(
                t.getId(),
                t.getTipoDocumento(),
                t.getNumeroDocumento(),
                t.getRazonSocial(),
                t.getTipoPersona(),
                t.getTelefono(),
                t.getEmail(),
                t.getDireccion(),
                t.isEsCliente(),
                t.isEsProveedor(),
                t.isActivo()
        );
    }
}
