# Documento: Plan Técnico de Despliegue

## Metadatos
* **Nombre:** 02_plan_tecnico_despliegue.md
* **Descripción:** Procedimiento técnico paso a paso para el aprovisionamiento, configuración de entornos, despliegue y puesta en marcha controlada del sistema de inventario con costo $0 COP.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Ingeniero DevOps y Despliegue)
* **Versión:** 1.0
* **Estado:** Aprobado

---

## 1. Fases del Procedimiento de Despliegue

```
[Fase 1: Preparación de MySQL Aislada]
                 │
                 ▼
[Fase 2: Aplicación Controlada de Scripts DDL y DML]
                 │
                 ▼
[Fase 3: Despliegue y Configuración del Backend]
                 │
                 ▼
[Fase 4: Despliegue y Configuración del Frontend]
                 │
                 ▼
[Fase 5: Validación Técnica y Smoke Testing]
```

---

## 2. Ejecución Paso a Paso

### Fase 1: Aprovisionamiento de la Base de Datos MySQL
1. Crear una instancia de base de datos MySQL 8.0 en la plataforma administrada gratuita seleccionada (ej. Aiven / Alwaysdata / contenedor Docker dedicado).
2. Crear la base de datos `ferreteria_db` configurada con cotejamiento `utf8mb4_unicode_ci`.
3. Crear un usuario de aplicación con privilegios limitados (`SELECT`, `INSERT`, `UPDATE`, `DELETE`, `CREATE`, `INDEX`, `ALTER`, `LOCK TABLES`) restringiendo permisos de administración global.
4. Obtener la cadena de conexión JDBC segura con TLS habilitado:
   `jdbc:mysql://<HOST>:<PORT>/ferreteria_db?useSSL=true&serverTimezone=America/Bogota`

### Fase 2: Aplicación Controlada de Scripts SQL
1. Conectarse a la base de datos y ejecutar manualmente en orden estricto:
   * `backend/src/main/resources/db/01_schema.sql` (Creación de tablas, claves primarias, foráneas, índices y restricciones).
   * `backend/src/main/resources/db/02_initial_data.sql` (Carga de roles, catálogos base, consecutivos y usuario administrador con hash BCrypt).
2. Verificar la integridad mediante consultas de validación:
   * `SELECT COUNT(*) FROM rol;` (Debe retornar 2).
   * `SELECT username, nombre_completo FROM usuario;` (Debe listar `admin_ferreteria` y `operador_mostrador`).
   * `SELECT COUNT(*) FROM referencia;` (Debe listar las 5 referencias sintéticas).

### Fase 3: Despliegue del Backend Spring Boot (Render / Docker)
1. Conectar el repositorio backend en el servicio PaaS.
2. Configurar el runtime para compilar vía Dockerfile o Maven con Java 21.
3. Inyectar las variables de entorno en el panel de secretos del proveedor:
   * `SPRING_PROFILES_ACTIVE`: `prod`
   * `DB_HOST`: Host asignado por el proveedor MySQL.
   * `DB_PORT`: Puerto (típicamente `3306`).
   * `DB_NAME`: `ferreteria_db`
   * `DB_USER`: Usuario de aplicación.
   * `DB_PASSWORD`: Secreto de base de datos.
   * `CORS_ALLOWED_ORIGINS`: URL HTTPS pública del frontend (ej. `https://ferreteria-frontend.vercel.app`).
   * `PORT`: `8080`
4. Iniciar la construcción y verificar en los logs que el arranque finalice sin errores (`Started InventarioApplication in X seconds`).
5. Verificar el endpoint de salud: `curl -I https://<BACKEND_URL>/api/v1/health` (debe responder HTTP 200 OK).

### Fase 4: Despliegue del Frontend React (Vercel / Render Static)
1. Conectar el repositorio frontend.
2. Configurar la variable de entorno de construcción:
   * `VITE_API_BASE_URL`: `https://<BACKEND_URL>/api/v1`
3. Comando de build: `npm run build`
4. Directorio de salida: `dist`
5. Configurar regla de reescritura SPA para redirigir todas las rutas a `index.html`.
6. Publicar y verificar que la URL HTTPS cargue la pantalla de inicio de sesión.

### Fase 5: Validación Técnica de Integración y Smoke Testing
1. Cargar la URL del frontend en el navegador.
2. Autenticarse con `admin_ferreteria` / `Admin123*`.
3. Comprobar que el navegador almacene la cookie de sesión `JSESSIONID` con flags `HttpOnly` y `Secure`.
4. Navegar a Catálogo, Inventario y Mostrador (POS).
5. Registrar una venta de prueba de 1 unidad de martillo a cliente Juan Pérez con cobro en efectivo.
6. Verificar que el Kárdex descuente la unidad y que se emita el comprobante imprimible con consecutivo `VTA-000101`.
7. Probar cierre de sesión y verificar que la cookie se invalide.
