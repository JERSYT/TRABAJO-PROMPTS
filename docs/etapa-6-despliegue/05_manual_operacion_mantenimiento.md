# Documento: Manual de Operación y Mantenimiento

## Metadatos
* **Nombre:** 05_manual_operacion_mantenimiento.md
* **Descripción:** Guía operativa y manual de procedimientos para la administración, diagnóstico de fallos, mantenimiento rutinario y respuesta ante incidentes en el entorno desplegado.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Ingeniero de Operaciones y DevOps)
* **Versión:** 1.0
* **Estado:** Aprobado

---

## 1. Arquitectura Desplegada y Puntos de Acceso

El sistema se compone de tres piezas interconectadas:
1. **Frontend (SPA):** React 18 + Vite servido a través de CDN HTTPS / Nginx en el puerto 80/443.
2. **Backend (API REST):** Java 21 / Spring Boot 3 ejecutándose en puerto 8080.
3. **Persistencia (RDBMS):** Motor MySQL 8.0 en puerto 3306 con volumen persistente.

### URLs de Acceso y Servicios:
* **Frontend Web (Mostrador / Admin):** `http://localhost` (o URL del proveedor PaaS gratuito).
* **Backend REST API:** `http://localhost:8080/api/v1` (o subdominio asignado).
* **Endpoint de Salud (Health Check):** `http://localhost:8080/api/v1/health`
* **Documentación Interactiva OpenAPI/Swagger:** `http://localhost:8080/swagger-ui.html`

---

## 2. Procedimientos de Inicio, Parada y Reinicio

### 2.1 En Entorno Docker Compose (Despliegue Local / Servidor Propio $0)
* **Iniciar todos los servicios en segundo plano:**
  ```bash
  docker compose up -d
  ```
* **Verificar el estado de los contenedores:**
  ```bash
  docker compose ps
  ```
  *(Comprobar que `ferreteria-mysql`, `ferreteria-backend` y `ferreteria-frontend` reporten estado `Up` o `healthy`).*
* **Detener los servicios preservando los datos:**
  ```bash
  docker compose down
  ```
  *(Nota crítica: no usar la bandera `-v` para evitar borrar el volumen persistente `ferreteria_mysql_data`).*
* **Reiniciar un servicio específico (ej. tras cambio de configuración):**
  ```bash
  docker compose restart backend
  ```

---

## 3. Diagnóstico de Problemas Habituales y Consulta de Logs

### 3.1 Consulta de Logs del Servidor
* **Logs del Backend en tiempo real:**
  ```bash
  docker compose logs -f backend
  ```
* **Logs de la Base de Datos MySQL:**
  ```bash
  docker compose logs -f db
  ```
* **Logs del Servidor Web Frontend (Nginx):**
  ```bash
  docker compose logs -f frontend
  ```

### 3.2 Diagnóstico de Errores de Conexión con MySQL
* **Síntoma:** El backend no inicia o arroja `CommunicationsException: Communications link failure`.
* **Pasos de Diagnóstico:**
  1. Comprobar que el servicio de base de datos responda ping:
     ```bash
     docker compose exec db mysqladmin ping -u root -p
     ```
  2. Verificar que las credenciales (`DB_USER`, `DB_PASSWORD`, `DB_NAME`) en el contenedor backend coincidan exactamente con las de la base de datos.
  3. Verificar que el puerto 3306 no esté bloqueado por un firewall o utilizado por otra instancia local de MySQL.

### 3.3 Diagnóstico de Errores 403 Forbidden en Formularios Web
* **Síntoma:** Al intentar registrar una venta o compra, el frontend muestra un error 403.
* **Causa Común:** Ausencia de la cabecera `X-XSRF-TOKEN` o sesión expirada.
* **Solución:**
  1. Verificar en las herramientas de desarrollo del navegador (pestaña Red / Network) si la cookie `XSRF-TOKEN` está presente.
  2. Si la sesión expiró (timeout de 30 minutos), cerrar sesión y volver a ingresar.

---

## 4. Gestión del Usuario Administrador
* En caso de pérdida de la contraseña del administrador en la base de datos de demostración, un operador con acceso a la consola de base de datos puede restablecerla mediante:
  ```sql
  -- Restablecer la contraseña a 'Admin123*' con hash BCrypt
  UPDATE usuario 
  SET password_hash = '$2a$10$X7m6Lz3yQpZ2WvU1B9rG9.b1bM9tA9b4s8V5x3k1z7q5W3v9x1b7y'
  WHERE username = 'admin_ferreteria';
  ```

---

## 5. Protocolo de Reversión de Publicación (Rollback)
Si una nueva versión del backend o del frontend presenta defectos críticos tras su publicación:
1. **Frontend:** Revertir el despliegue al commit anterior en el panel del proveedor estático (Vercel / Netlify / Render) con un clic en "Rollback to this deployment".
2. **Backend:** Revertir la imagen de contenedor o hacer checkout de la versión estable previa:
   ```bash
   git checkout <TAG_VERSION_ANTERIOR>
   docker compose build --no-cache backend
   docker compose up -d backend
   ```
3. **Verificación posterior:** Confirmar mediante `/api/v1/health` que la versión previa se encuentra operativa.
