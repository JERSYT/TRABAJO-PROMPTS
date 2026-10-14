package com.ferreteria.inventario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba base de verificación de arranque del contexto de Spring Boot con perfil 'test'.
 */
@SpringBootTest
@ActiveProfiles("test")
class InventarioApplicationTests {

    @Test
    @DisplayName("El contexto de Spring Boot debe cargar exitosamente sin errores de configuración")
    void contextLoads() {
        assertTrue(true, "El contexto de Spring Boot arrancó correctamente.");
    }
}
