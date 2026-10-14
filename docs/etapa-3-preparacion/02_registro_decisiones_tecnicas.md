# Documento: Registro de Decisiones Técnicas de Preparación y Scaffolding

## Metadatos
* **Nombre:** 02_registro_decisiones_tecnicas.md
* **Descripción:** Registro de decisiones de ingeniería adoptadas para la preparación técnica de los repositorios backend (Spring Boot 3) y frontend (React + Vite).
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Ingeniero Senior & Arquitecto Técnico)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Decisiones Técnicas Adoptadas en el Backend (`backend/`)

### DT-01: Versión de Spring Boot y Compatibilidad Java 21
* **Decisión:** Configurar Spring Boot versión `3.3.4` con compatibilidad Java 21 LTS y Maven Compiler Plugin 3.13+.
* **Justificación:** Spring Boot 3.3.4 ofrece soporte nativo maduro para Jakarta EE 10, Java 21 (Virtual Threads si se requiriesen a futuro, Records para DTOs inmutables) y ProblemDetail (RFC 9457).
* **Dependencias Base Seleccionadas:**
  * `spring-boot-starter-web`: API REST y MVC.
  * `spring-boot-starter-data-jpa`: Persistencia con Hibernate 6.
  * `mysql-connector-j`: Conector oficial para MySQL 8.
  * `spring-boot-starter-security`: Autenticación por sesión y filtros RBAC.
  * `spring-boot-starter-validation`: Bean Validation para DTOs.
  * `spring-boot-starter-actuator`: Endpoint de comprobación de salud técnica `/actuator/health`.
  * `springdoc-openapi-starter-webmvc-ui`: Documentación OpenAPI/Swagger de la API REST.
  * `spring-boot-starter-test`: JUnit 5, Mockito, MockMvc.
  * `com.h2database:h2` (scope: test): Únicamente como base de apoyo para pruebas unitarias ultrarrápidas de contexto sin sustituir las pruebas con MySQL real.

### DT-02: Gestión de Perfiles y Configuración Externa
* **Decisión:** Implementar 4 archivos de configuración en `src/main/resources`:
  * `application.yml`: Configuración común transversal (nombres de cookies, prefijos de comprobantes, timeouts).
  * `application-dev.yml`: Configuración para entorno de desarrollo local con variables de entorno por defecto (`DB_HOST: localhost`, `DB_PORT: 3306`, `DB_NAME: ferreteria_db`, `DB_USER: root`).
  * `application-test.yml`: Configuración aislada para pruebas de ejecución rápida.
  * `application-prod.yml`: Configuración estricta para el despliegue de demostración con cookies `Secure` y orígenes CORS cerrados.

### DT-03: Endpoint Técnico de Salud Controlado
* **Decisión:** Exponer `/api/v1/health` mediante un controlador dedicado y habilitar únicamente `health` en Actuator (`management.endpoints.web.exposure.include=health`), ocultando métricas internas, beans y variables de entorno del servidor.

---

## 2. Decisiones Técnicas Adoptadas en el Frontend (`frontend/`)

### DT-04: Inicialización con Vite y React 18+
* **Decisión:** Utilizar **Vite** como empaquetador y servidor de desarrollo para React en lugar de Create React App (CRA obsoleto).
* **Justificación:** Proporciona tiempos de arranque casi instantáneos (HMR), builds optimizados con Rollup y compatibilidad nativa con ESM y Node 24.

### DT-05: Cliente HTTP Centralizado con Inyección Automática de CSRF
* **Decisión:** Implementar `src/services/api/apiClient.js` como una instancia preconfigurada de Axios que:
  * Activa `withCredentials: true` para todas las peticiones (viaja con la cookie `JSESSIONID`).
  * Posee un interceptor que extrae la cookie `XSRF-TOKEN` y la inyecta automáticamente en la cabecera `X-XSRF-TOKEN` para verbos `POST`, `PUT`, `PATCH` y `DELETE`.
  * Captura errores HTTP 401 para limpiar la sesión y redirigir limpiamente a la pantalla de login.

### DT-06: Tema Global Material UI y Pantalla Neutra de Arranque
* **Decisión:** Configurar el `ThemeProvider` de Material UI con la paleta de colores institucional (azul ferretero `#1976d2` y acento naranja de seguridad `#ed6c02`), fuente Roboto estándar y una pantalla inicial neutra (`HealthCheckPage.jsx`) que comprueba la comunicación con el backend antes de la implementación funcional.
