package com.ferreteria.inventario.auth.service;

import com.ferreteria.inventario.auth.dto.request.LoginRequest;
import com.ferreteria.inventario.auth.dto.response.AuthResponse;
import com.ferreteria.inventario.shared.audit.AuditService;
import com.ferreteria.inventario.shared.exception.ResourceNotFoundException;
import com.ferreteria.inventario.user.entity.Rol;
import com.ferreteria.inventario.user.entity.Usuario;
import com.ferreteria.inventario.user.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!usuario.isActivo()) {
            throw new BadCredentialsException("Cuenta de usuario inactiva");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .toList();

        var authorities = usuario.getRoles().stream()
                .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r.getNombre()))
                .toList();

        Authentication auth = new UsernamePasswordAuthenticationToken(usuario.getUsername(), null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        auditService.registrarEvento(usuario, "SEGURIDAD", "INICIO_SESION", "Inicio de sesión exitoso del usuario " + usuario.getUsername());

        return new AuthResponse(usuario.getId(), usuario.getUsername(), usuario.getNombreCompleto(), usuario.getEmail(), roles);
    }

    @Transactional(readOnly = true)
    public AuthResponse getActiveUser(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .toList();

        return new AuthResponse(usuario.getId(), usuario.getUsername(), usuario.getNombreCompleto(), usuario.getEmail(), roles);
    }

    public void logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}
