package com.ferreteria.inventario.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Configuración de infraestructura de Spring Security para SPA.
 * - Autenticación basada en sesiones de servidor (HttpSession / JSESSIONID)
 * - Protección CSRF compatible con SPA (CsrfTokenRequestAttributeHandler sin XOR masking + CsrfCookieFilter)
 * - Control de acceso RBAC
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // Desactiva el enmascaramiento XOR para que el token de la cookie coincida directamente con el header X-XSRF-TOKEN
        requestHandler.setCsrfRequestAttributeName(null);

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> {
                HttpSessionCsrfTokenRepository csrfRepository = new HttpSessionCsrfTokenRepository();
                csrfRepository.setHeaderName("X-XSRF-TOKEN");
                csrf.csrfTokenRepository(csrfRepository)
                    .csrfTokenRequestHandler(requestHandler)
                    .ignoringRequestMatchers("/api/v1/health", "/actuator/health", "/api/v1/auth/login");
            })
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(2)
            )
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos técnicos y de documentación
                .requestMatchers(
                    "/api/v1/health",
                    "/actuator/health",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Endpoint público de autenticación y CSRF inicial
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/csrf").permitAll()
                // Endpoints administrativos protegidos por rol
                .requestMatchers("/api/v1/admin/**", "/api/v1/inventory/adjustments/**", "/api/v1/audit/**", "/api/v1/users/**")
                    .hasRole("ADMINISTRADOR")
                // El resto de peticiones requieren autenticación
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Filtro para asegurar que el token CSRF siempre sea evaluado y enviado como cookie en cada petición.
     */
    public static class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                // Forzar la resolución del token diferido para que se envíe la cookie Set-Cookie: XSRF-TOKEN
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }
}
