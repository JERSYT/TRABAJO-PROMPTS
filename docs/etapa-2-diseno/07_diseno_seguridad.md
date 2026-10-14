# Documento: Diseño de Seguridad del Sistema

## Metadatos
* **Nombre:** 07_diseno_seguridad.md
* **Descripción:** Especificación técnica del subsistema de seguridad con Spring Security, autenticación por sesión, protección CSRF y CORS, hashing BCrypt, RBAC y aprovisionamiento seguro del primer administrador.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Especialista en Seguridad de Aplicaciones)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Principios de Seguridad y Decisiones Obligatorias
* **Autenticación Basada en Sesiones:** Se utiliza la gestión nativa de sesiones de Spring Security respaldada por la cookie `JSESSIONID`. Queda terminantemente prohibido el uso de tokens JWT.
* **Autorización en Profundidad:** El backend es la autoridad absoluta. La ocultación de elementos visuales en el frontend es puramente ergonómica. Toda solicitud a endpoints protegidos es interceptada y verificada mediante filtros y anotaciones `@PreAuthorize`.
* **Zero Hardcoded Secrets:** Ninguna contraseña, llave secreta o credencial administrativa debe residir en el código fuente versionado en Git.

---

## 2. Flujo de Autenticación y Manejo de Sesión

### 2.1 Cadena de Filtros de Spring Security (`SecurityFilterChain`)
El backend define la siguiente política de acceso:
* **Endpoints Públicos:**
  * `POST /api/v1/auth/login` (Recepción de credenciales de usuario).
  * `GET /api/v1/health` o `/actuator/health` (Comprobación técnica de disponibilidad sin detalles internos).
  * `GET /api/v1/auth/csrf` (Obtención voluntaria del token CSRF si se requiere).
* **Endpoints Protegidos por Rol `ROLE_ADMINISTRADOR` (Exclusivos):**
  * `POST /api/v1/inventory/adjustments` (Ajustes manuales de inventario - RN-08).
  * `POST /api/v1/inventory/initial` (Carga de inventario inicial).
  * `POST /api/v1/sales/{id}/cancel` y `POST /api/v1/purchases/{id}/cancel` (Cancelaciones comerciales).
  * `GET /api/v1/audit/**` (Bitácora de auditoría).
  * `CRUD /api/v1/users/**` (Administración de cuentas y asignación de roles).
* **Endpoints Protegidos para Cualquier Usuario Autenticado (`ROLE_ADMINISTRADOR`, `ROLE_USUARIO_OPERATIVO`):**
  * `POST /api/v1/auth/logout`, `GET /api/v1/auth/me`.
  * `GET /api/v1/catalog/**`, `POST /api/v1/sales`, `POST /api/v1/purchases`, `POST /api/v1/returns`, `CRUD /api/v1/third-parties/**`.

### 2.2 Política de Sesión e Invalidación
* **Creación de Sesión:** `SessionCreationPolicy.IF_REQUIRED`.
* **Protección contra Fijación de Sesión:** `sessionManagement().sessionFixation().migrateSession()`. Al autenticarse exitosamente, Spring Security invalida el ID de sesión anónimo anterior y genera un identificador `JSESSIONID` criptográficamente seguro.
* **Cierre de Sesión (`Logout`):** Invalida explícitamente el `HttpSession`, elimina el contexto de seguridad en memoria (`SecurityContextHolder.clearContext()`) y marca la cookie `JSESSIONID` con tiempo de expiración cero en la respuesta.
* **Tiempo de Inactividad (Timeout):** Configurado a 30 minutos de inactividad (`server.servlet.session.timeout=30m`).

---

## 3. Configuración de Cookies, CORS y CSRF

### 3.1 Atributos de la Cookie de Sesión
```properties
server.servlet.session.cookie.name=JSESSIONID
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false # En producción HTTPS pasa a true
server.servlet.session.cookie.same-site=Lax
server.servlet.session.cookie.path=/
```

### 3.2 Protección contra Falsificación de Peticiones en Sitios Cruzados (CSRF)
* Se activa el repositorio `CookieCsrfTokenRepository.withHttpOnlyFalse()`.
* En la primera solicitud GET, el backend deposita en el cliente una cookie `XSRF-TOKEN` (legible por JavaScript en el mismo dominio o por interceptor configurado).
* Para cualquier método que modifique estado (`POST`, `PUT`, `PATCH`, `DELETE`), el frontend React lee el valor y lo remite en la cabecera HTTP `X-XSRF-TOKEN`.
* Solicitudes mutativas sin la cabecera o con valor discrepante son rechazadas automáticamente con **HTTP 403 Forbidden**.

### 3.3 Configuración de CORS (Cross-Origin Resource Sharing)
* Se define un `CorsConfigurationSource` que autoriza estrictamente los orígenes del frontend:
  * Local: `http://localhost:5173`
  * Despliegue de Demostración: URL asignada por el proveedor (inyectada por variable de entorno `APP_CORS_ALLOWED_ORIGINS`).
* Métodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
* Cabeceras permitidas: `Content-Type`, `X-XSRF-TOKEN`, `Authorization`.
* `allowCredentials(true)` obligatorio para habilitar el intercambio bidireccional de cookies de sesión.

---

## 4. Almacenamiento Seguro de Contraseñas
* Se implementa `BCryptPasswordEncoder` con un factor de costo de trabajo (strength) de 10.
* El algoritmo genera automáticamente una sal (*salt*) criptográfica única por cada contraseña y utiliza un coste computacional adaptativo contra ataques de fuerza bruta.

---

## 5. Procedimiento Seguro de Aprovisionamiento del Primer Administrador

Para evitar el uso de credenciales predeterminadas quemadas en el código fuente (ej. `admin/admin` en repositorios públicos), se diseña el siguiente mecanismo seguro:

1. **Detección Automática de Base Limpia:** Al iniciar la aplicación, un componente `AdminInitializerRunner` consulta si la tabla `usuario` tiene 0 registros.
2. **Lectura de Variables de Entorno:**
   * Si la base de datos está vacía, el componente lee las variables de entorno del sistema:
     * `APP_ADMIN_INITIAL_USERNAME` (si no existe, toma `admin_ferreteria`).
     * `APP_ADMIN_INITIAL_PASSWORD` (debe ser suministrada al arrancar el servidor).
3. **Generación Segura si no se proporciona contraseña:**
   * Si `APP_ADMIN_INITIAL_PASSWORD` no está definida en el entorno, el componente genera una contraseña pseudoaleatoria criptográfica de 16 caracteres, crea el usuario con rol `ROLE_ADMINISTRADOR` y la imprime **una única vez en los logs de la consola del servidor durante el arranque inicial**, advirtiendo la necesidad de guardarla.
4. **Idempotencia:** Si ya existe al menos un usuario en la tabla, el proceso no realiza ninguna acción, garantizando que nunca se sobreescriban credenciales en reinicios posteriores.

---

## 6. Prevención de Escalamiento de Privilegios y Vulnerabilidades
* **Prevención de Escalamiento de Privilegios:** El endpoint de creación y edición de usuarios solo permite ser invocado por administradores. Además, un usuario nunca puede modificar sus propios roles a través de peticiones enviadas desde la interfaz.
* **Inyección SQL:** Mitigada al 100% mediante el uso exclusivo de consultas parametrizadas de Spring Data JPA y Hibernate ORM.
* **Exposición de Información Sensible:**
  * Las entidades JPA de usuario nunca son retornadas directamente en las respuestas REST (se utiliza `UserResponseDTO` excluyendo el campo `password_hash`).
  * Los mensajes de error de autenticación son genéricos ("Credenciales inválidas") para evitar enumeración de nombres de usuario.
