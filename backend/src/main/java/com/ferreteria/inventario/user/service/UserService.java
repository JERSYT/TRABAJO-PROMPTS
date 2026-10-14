package com.ferreteria.inventario.user.service;

import com.ferreteria.inventario.shared.audit.AuditService;
import com.ferreteria.inventario.shared.exception.BusinessException;
import com.ferreteria.inventario.shared.exception.ResourceNotFoundException;
import com.ferreteria.inventario.user.dto.request.CrearUsuarioRequest;
import com.ferreteria.inventario.user.dto.response.UsuarioResponse;
import com.ferreteria.inventario.user.entity.Rol;
import com.ferreteria.inventario.user.entity.Usuario;
import com.ferreteria.inventario.user.repository.RolRepository;
import com.ferreteria.inventario.user.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                       PasswordEncoder passwordEncoder, AuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public UsuarioResponse crearUsuario(CrearUsuarioRequest request, String adminUsername) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El nombre de usuario ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getEmail());
        usuario.setActivo(true);

        Set<Rol> roles = new HashSet<>();
        for (String rolNombre : request.getRoles()) {
            Rol rol = rolRepository.findByNombre(rolNombre)
                    .orElseGet(() -> rolRepository.save(new Rol(null, rolNombre, "Rol " + rolNombre)));
            roles.add(rol);
        }
        usuario.setRoles(roles);

        Usuario guardado = usuarioRepository.save(usuario);

        Usuario admin = usuarioRepository.findByUsername(adminUsername).orElse(null);
        auditService.registrarEvento(admin, "USUARIOS", "CREAR_USUARIO", "Creación de usuario " + guardado.getUsername());

        return mapToResponse(guardado);
    }

    @Transactional
    public void alternarEstado(Long id, String adminUsername) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(!usuario.isActivo());
        usuarioRepository.save(usuario);

        Usuario admin = usuarioRepository.findByUsername(adminUsername).orElse(null);
        auditService.registrarEvento(admin, "USUARIOS", "ESTADO_USUARIO",
                "Cambio de estado a " + (usuario.isActivo() ? "ACTIVO" : "INACTIVO") + " para usuario " + usuario.getUsername());
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        Set<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toSet());
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.isActivo(),
                roles,
                usuario.getFechaCreacion()
        );
    }
}
