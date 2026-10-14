package com.ferreteria.inventario.user.controller;

import com.ferreteria.inventario.user.dto.request.CrearUsuarioRequest;
import com.ferreteria.inventario.user.dto.response.UsuarioResponse;
import com.ferreteria.inventario.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> listarUsuarios(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.listarUsuarios(pageable));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(
            @Valid @RequestBody CrearUsuarioRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        UsuarioResponse creado = userService.crearUsuario(request, admin.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Void> alternarEstado(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        userService.alternarEstado(id, admin.getUsername());
        return ResponseEntity.noContent().build();
    }
}
