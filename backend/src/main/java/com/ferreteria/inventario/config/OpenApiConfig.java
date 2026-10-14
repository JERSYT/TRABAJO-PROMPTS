package com.ferreteria.inventario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración transversal de OpenAPI / Swagger 3 para la documentación de la API REST.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestión de Inventario para Ferretería")
                        .version("1.0.0")
                        .description("Especificación técnica de contratos REST para operaciones comerciales, inventario, catálogo y auditoría.")
                        .contact(new Contact()
                                .name("Equipo de Ingeniería de Software")
                                .email("soporte@ferreteria.local"))
                        .license(new License()
                                .name("Uso Académico Demostrativo")
                                .url("https://ferreteria.local/licencia")));
    }
}
