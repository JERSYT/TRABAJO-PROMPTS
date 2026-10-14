package com.ferreteria.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferreteria.inventario.inventory.dto.request.AjusteInventarioRequest;
import com.ferreteria.inventario.user.dto.request.CrearUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de seguridad, autorización basada en roles (RBAC) y protección CSRF.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityRbacTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Petición anónima a /api/v1/inventory/stocks debe ser rechazada con 401 Unauthorized")
    void peticionAnonimaRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/stocks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Usuario Operativo intentando ejecutar Ajuste Manual de Inventario debe recibir 403 Forbidden (RN-08)")
    @WithMockUser(username = "operador_mostrador", roles = {"USUARIO_OPERATIVO"})
    void usuarioOperativoNoPuedeEjecutarAjustes() throws Exception {
        AjusteInventarioRequest req = new AjusteInventarioRequest();
        req.setReferenciaId(1L);
        req.setTipoAjuste("INCREMENTO");
        req.setCantidad(new BigDecimal("5.000"));
        req.setMotivo("Intento no autorizado de ajuste de inventario");

        mockMvc.perform(post("/api/v1/inventory/adjustments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario Operativo intentando consultar o crear usuarios debe recibir 403 Forbidden")
    @WithMockUser(username = "operador_mostrador", roles = {"USUARIO_OPERATIVO"})
    void usuarioOperativoNoPuedeGestionarUsuarios() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());

        CrearUsuarioRequest req = new CrearUsuarioRequest();
        req.setUsername("nuevo_hacker");
        req.setPassword("123456");
        req.setNombreCompleto("Usuario No Permitido");
        req.setRoles(Set.of("ROLE_ADMINISTRADOR"));

        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario Operativo intentando consultar la bitácora de auditoría debe recibir 403 Forbidden (RN-11)")
    @WithMockUser(username = "operador_mostrador", roles = {"USUARIO_OPERATIVO"})
    void usuarioOperativoNoPuedeConsultarAuditoria() throws Exception {
        mockMvc.perform(get("/api/v1/audit/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Petición mutativa POST sin token CSRF debe ser rechazada con 403 Forbidden")
    @WithMockUser(username = "admin_ferreteria", roles = {"ADMINISTRADOR"})
    void peticionPostSinCsrfRetorna403() throws Exception {
        AjusteInventarioRequest req = new AjusteInventarioRequest();
        req.setReferenciaId(1L);
        req.setTipoAjuste("INCREMENTO");
        req.setCantidad(new BigDecimal("1.000"));
        req.setMotivo("Ajuste sin token CSRF");

        mockMvc.perform(post("/api/v1/inventory/adjustments")
                // Sin .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario Administrador autenticado con CSRF puede consultar usuarios exitosamente (200 OK)")
    @WithMockUser(username = "admin_ferreteria", roles = {"ADMINISTRADOR"})
    void administradorPuedeConsultarUsuarios() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
