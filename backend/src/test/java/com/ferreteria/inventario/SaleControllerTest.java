package com.ferreteria.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferreteria.inventario.customer.entity.Tercero;
import com.ferreteria.inventario.customer.repository.TerceroRepository;
import com.ferreteria.inventario.sale.dto.request.CrearVentaRequest;
import com.ferreteria.inventario.sale.dto.request.VentaLineaRequest;
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
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TerceroRepository terceroRepository;

    @Test
    @DisplayName("GET /api/v1/sales sin autenticación debe retornar 401 Unauthorized")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/sales"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/sales con usuario autenticado debe retornar 200 OK")
    @WithMockUser(username = "operador_mostrador", roles = {"USUARIO_OPERATIVO"})
    void shouldReturn200WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/sales"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/sales con cliente y líneas válidas debe registrar venta 201 Created (RN-05, RN-06)")
    @WithMockUser(username = "admin_ferreteria", roles = {"ADMINISTRADOR"})
    void shouldCreateSaleSuccessfully() throws Exception {
        Tercero cliente = terceroRepository.findByNumeroDocumento("1020304050")
                .orElseGet(() -> {
                    Tercero t = new Tercero();
                    t.setTipoDocumento("CC");
                    t.setNumeroDocumento("1020304050");
                    t.setRazonSocial("Juan Perez Constructor");
                    t.setTipoPersona("NATURAL");
                    t.setEsCliente(true);
                    t.setEsProveedor(false);
                    return terceroRepository.save(t);
                });

        CrearVentaRequest req = new CrearVentaRequest();
        req.setClienteId(cliente.getId());
        req.setMontoPagadoEfectivo(new BigDecimal("100000.00"));
        req.setLineas(List.of(
                new VentaLineaRequest(1L, new BigDecimal("1.000"))
        ));

        mockMvc.perform(post("/api/v1/sales")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consecutivo").exists())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.lineas").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/sales con pago insuficiente debe retornar 409 Conflict (ProblemDetail)")
    @WithMockUser(username = "admin_ferreteria", roles = {"ADMINISTRADOR"})
    void shouldFailWhenPaymentIsInsufficient() throws Exception {
        Tercero cliente = terceroRepository.findByNumeroDocumento("1020304050").orElseThrow();

        CrearVentaRequest req = new CrearVentaRequest();
        req.setClienteId(cliente.getId());
        req.setMontoPagadoEfectivo(new BigDecimal("100.00")); // Pago ridículo frente al precio
        req.setLineas(List.of(
                new VentaLineaRequest(1L, new BigDecimal("1.000"))
        ));

        mockMvc.perform(post("/api/v1/sales")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de Regla de Negocio"));
    }
}
