# Documento: Informe de Evidencias, Pruebas y Cierre de Etapa 3

## Metadatos
* **Nombre:** 04_informe_evidencias_pruebas.md
* **Descripción:** Informe formal de evidencias de ejecución técnica, resultados de compilación, ejecución de suites de pruebas y verificación de scaffolding para backend y frontend.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Ingeniero Senior & Responsable Técnico)
* **Versión:** 1.0
* **Estado:** Aprobado

---

## 1. Estado General de la Etapa 3
* **Estado:** **Completada con verificaciones externas pendientes** (la conexión con MySQL en producción queda preparada y verificada localmente mediante perfil `test` con H2; el servicio MySQL local se encuentra detenido y se pondrá en marcha con los scripts en la Etapa 4).
* **Confirmación de Alcance:** Se declara expresamente que **no se implementó ninguna funcionalidad de negocio** (ni CRUD de catálogo, ni compras, ni ventas, ni movimientos comerciales) durante esta etapa, respetando fielmente el alcance de preparación técnica y scaffolding.

---

## 2. Resumen de Cambios Técnicos Realizados

### 2.1 Repositorio Backend (`backend/`)
* Estructura completa de paquetes Java bajo `com.ferreteria.inventario` (`config`, `security`, `shared`, `auth`, `user`, `catalog`, `customer`, `supplier`, `inventory`, `purchase`, `sale`, `returns`, `document`, `reporting`).
* Archivo `pom.xml` configurado para **Java 21** y **Spring Boot 3.3.4** con dependencias aprobadas.
* Perfiles de configuración preparados: `application.yml`, `application-dev.yml`, `application-test.yml` y `application-prod.yml`.
* Configuración base de **Spring Security** con sesiones (`HttpSession`), cookies `JSESSIONID`, protección CSRF (`CookieCsrfTokenRepository.withHttpOnlyFalse()`) y codificación de contraseñas con BCrypt.
* Configuración de CORS transversal para orígenes frontend permitidos con soporte de credenciales.
* Manejador global de excepciones con soporte para **RFC 9457** (`ProblemDetail`).
* Controlador de comprobación técnica de salud en `/api/v1/health` y Actuator en `/actuator/health`.
* Archivo `backend/README.md` con manual completo de configuración y comandos.

### 2.2 Repositorio Frontend (`frontend/`)
* Proyecto inicializado con **Vite** y **React 18+**.
* Dependencias instaladas y fijadas: `@mui/material`, `@emotion/react`, `@emotion/styled`, `@mui/icons-material`, `axios`, `react-router-dom`, `formik`, `yup`.
* Estructura modular de directorios: `app/`, `components/`, `features/`, `services/api/`, `hooks/`, `utils/`, `tests/`.
* Cliente HTTP centralizado (`src/services/api/apiClient.js`) con envío de cookies y captura automática de cabeceras CSRF.
* Tema visual institucional personalizado de Material UI en `src/app/theme/theme.js`.
* Proveedor de autenticación (`AuthProvider.jsx`) conectado a la sesión del servidor (sin JWT).
* Pantalla neutra de arranque y diagnóstico técnico en `src/features/health/HealthStatusPage.jsx`.
* Archivo `frontend/README.md` con instrucciones de instalación, desarrollo y build.

---

## 3. Pruebas y Evidencias de Ejecución Real

### 3.1 Backend: Compilación y Pruebas Automatizadas
* **Comando ejecutado:** `mvn test` (en directorio `backend/`)
* **Salida del comando:**
  ```text
  [INFO] Running com.ferreteria.inventario.HealthControllerTest
  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.826 s -- in com.ferreteria.inventario.HealthControllerTest
  [INFO] Running com.ferreteria.inventario.InventarioApplicationTests
  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.792 s -- in com.ferreteria.inventario.InventarioApplicationTests
  [INFO] 
  [INFO] Results:
  [INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
  [INFO] BUILD SUCCESS
  [INFO] Total time: 18.444 s
  ```
* **Resultado:** **2/2 Pruebas Aprobadas (100% éxito)**. El contexto de Spring Boot arranca limpiamente y el endpoint de salud `/api/v1/health` responde con estado HTTP 200 OK y carga útil JSON.

### 3.2 Frontend: Instalación y Build de Producción
* **Comandos ejecutados:** `npm install` y `npm run build` (en directorio `frontend/`)
* **Salida del comando:**
  ```text
  added 164 packages, and audited 165 packages in 52s
  ✓ 976 modules transformed.
  dist/index.html                 0.74 kB │ gzip:   0.43 kB
  dist/assets/index-Cgf39La0.js 337.40 kB │ gzip: 112.57 kB
  ✓ built in 3.16s
  ```
* **Resultado:** **Aprobado**. Instalación reproducible de dependencias y generación de bundle de producción sin advertencias ni errores.

---

## 4. Pruebas No Ejecutadas / Bloqueos Externos
* **Conexión real contra MySQL Server 8.0 local:** El servicio `MySQL80` en Windows se encuentra detenido en la máquina anfitriona. Se programará su inicio y la aplicación de los scripts DDL en el primer incremento de la Etapa 4.

---

## 5. Cumplimiento de Criterios de Aceptación Globales de la Etapa 3
| Criterio | Estado | Evidencia |
| :--- | :---: | :--- |
| Plan detallado y verificable de implementación posterior | **Aprobado** | `docs/etapa-3-preparacion/01_plan_implementacion_detallado.md` |
| El backend compila y sus pruebas ejecutables pasan | **Aprobado** | `mvn test` exitoso (2/2 tests OK, BUILD SUCCESS) |
| El frontend instala dependencias y genera build sin errores | **Aprobado** | `npm run build` exitoso (dist/ generado en 3.16s) |
| Ambos proyectos tienen README con instrucciones técnicas | **Aprobado** | `backend/README.md` y `frontend/README.md` |
| Configuración de MySQL externa y configurable | **Aprobado** | `application-dev.yml` y `application-prod.yml` con variables de entorno |
| Seguridad base respeta el diseño (sesiones, cookies, CSRF, CORS) | **Aprobado** | `SecurityConfig.java` y `CorsConfig.java` |
| Comprobación técnica de salud controlada | **Aprobado** | `/api/v1/health` probado y funcionando |
| No se implementaron funcionalidades de negocio | **Aprobado** | Cero CRUD de negocio; solo scaffolding y pruebas técnicas |
| Registro de decisiones y riesgos actualizado | **Aprobado** | Documentos 02 y 03 en `docs/etapa-3-preparacion/` |
