# Documento: Configuración del Entorno Desplegado

## Metadatos
* **Nombre:** 03_configuracion_entorno_desplegado.md
* **Descripción:** Especificación técnica de parámetros de configuración, variables de entorno protegidas, directivas CORS, políticas de cookies y protección CSRF para el entorno de producción desplegado.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Ingeniero DevOps y Seguridad Web)
* **Versión:** 1.0
* **Estado:** Aprobado

---

## 1. Variables de Entorno del Backend (`backend`)

Las variables deben configurarse en el panel de control del servicio de alojamiento (o en un archivo `.env` local no versionado para Docker):

| Variable de Entorno | Valor Ejemplo / Producción | Obligatoria | Propósito y Seguridad |
| :--- | :--- | :---: | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` | SÍ | Activa `application-prod.yml` con `ddl-auto: none`. |
| `PORT` | `8080` | SÍ | Puerto de escucha HTTP del contenedor backend. |
| `DB_HOST` | `mysql-ferreteria.aivencloud.com` | SÍ | Host o IP del servidor MySQL de demostración. |
| `DB_PORT` | `3306` | SÍ | Puerto de red de la base de datos MySQL. |
| `DB_NAME` | `ferreteria_db` | SÍ | Nombre de la base de datos aislada. |
| `DB_USER` | `ferreteria_app` | SÍ | Usuario de aplicación sin privilegios de superadministrador. |
| `DB_PASSWORD` | *(secreto inyectado en PaaS)* | SÍ | Contraseña robusta no versionada en Git. |
| `CORS_ALLOWED_ORIGINS` | `https://ferreteria-inventario.vercel.app` | SÍ | Origen único HTTPS autorizado para solicitudes web cruzadas. |
| `APP_ADMIN_INITIAL_USERNAME`| `admin_ferreteria` | NO | Nombre de usuario del administrador inicial. |
| `APP_ADMIN_INITIAL_PASSWORD`| *(secreto inyectado)* | NO | Contraseña de alta para el primer administrador. |

---

## 2. Variables de Entorno del Frontend (`frontend`)

| Variable | Valor en Despliegue | Propósito |
| :--- | :--- | :--- |
| `VITE_API_BASE_URL` | `https://ferreteria-backend.onrender.com/api/v1` | URL base pública del backend para el cliente Axios. |

---

## 3. Políticas de Seguridad de Transporte, Cookies y CSRF

### 3.1 Política de Cookies de Sesión (`JSESSIONID`)
En el perfil `prod` (`application-prod.yml`):
* `server.servlet.session.cookie.name=JSESSIONID`
* `server.servlet.session.cookie.http-only=true` (Impide acceso desde JavaScript malicioso / protección contra XSS).
* `server.servlet.session.cookie.secure=true` (Garantiza que la cookie solo viaje sobre conexiones cifradas HTTPS).
* `server.servlet.session.cookie.same-site=None` (Necesario cuando frontend y backend residen en subdominios diferentes con HTTPS, permitiendo `withCredentials: true`).

### 3.2 Política de Protección CSRF
* Repositorio: `CookieCsrfTokenRepository.withHttpOnlyFalse()`.
* La cookie `XSRF-TOKEN` es emitida por el backend y consumida por el interceptor de Axios en el frontend, inyectándola en la cabecera HTTP `X-XSRF-TOKEN` para cada solicitud `POST`, `PUT`, `PATCH` y `DELETE`.
* Solicitudes mutativas sin esta cabecera son rechazadas automáticamente por Spring Security con **HTTP 403 Forbidden**.

### 3.3 Política de CORS (Cross-Origin Resource Sharing)
* Orígenes autorizados estrictamente limitados a la URL del frontend desplegado.
* Métodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
* `allowCredentials(true)` obligatorio para habilitar el intercambio bidireccional de cookies de sesión.
* Cabeceras expuestas: `X-XSRF-TOKEN`.
