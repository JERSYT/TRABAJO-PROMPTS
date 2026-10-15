package com.ferreteria.inventario.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración centralizada de Cross-Origin Resource Sharing (CORS)
 * para autorizar la comunicación con el frontend React separado.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        List<String> origins = new java.util.ArrayList<>(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toList());
        
        if (!origins.contains("https://*.vercel.app")) origins.add("https://*.vercel.app");
        if (!origins.contains("https://trabajo-prompts.vercel.app")) origins.add("https://trabajo-prompts.vercel.app");
        if (!origins.contains("https://*.onrender.com")) origins.add("https://*.onrender.com");
        if (!origins.contains("http://localhost:*")) origins.add("http://localhost:*");

        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
