# Backend - Sistema de Gestión de Inventario para Ferretería

## Descripción General
Servicio backend REST desarrollado con **Java 21**, **Spring Boot 3.3.4**, **Spring Security 6**, **Spring Data JPA** y **MySQL 8.0**. Implementa una arquitectura tradicional por capas (`Controller` → `Service` → `Repository`), control de acceso basado en roles (RBAC), autenticación mediante sesiones de servidor (`HttpSession` / Cookies `JSESSIONID`), protección CSRF y manejo estandarizado de errores según RFC 9457 (`ProblemDetail`).

---

## 1. Requisitos de Entorno
* **Java Development Kit (JDK):** Versión 21 LTS (Eclipse Temurin / OpenJDK).
* **Apache Maven:** Versión 3.9+ instalada y en el PATH del sistema.
* **Base de Datos:** MySQL Server 8.0+.

---

## 2. Configuración y Variables de Entorno
El backend utiliza perfiles de Spring (`dev`, `test`, `prod`). Las variables de entorno configurables son:

| Variable de Entorno | Valor Predeterminado | Descripción |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `dev` | Perfil activo de ejecución (`dev`, `test`, `prod`). |
| `PORT` | `8080` | Puerto HTTP del servidor web embebido. |
| `DB_HOST` | `localhost` | Host del servidor MySQL. |
| `DB_PORT` | `3306` | Puerto de conexión MySQL. |
| `DB_NAME` | `ferreteria_db` | Nombre de la base de datos de la ferretería. |
| `DB_USER` | `root` | Usuario con permisos en MySQL. |
| `DB_PASSWORD` | *(vacío)* | Contraseña del usuario MySQL. |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Orígenes web autorizados para interactuar con la API. |

---

## 3. Compilación y Ejecución

### 3.1 Compilar el Proyecto y Ejecutar Pruebas Automatizadas
```bash
mvn clean test
```

### 3.2 Empaquetar el Artefacto Ejecutable (.jar)
```bash
mvn clean package -DskipTests
```
El archivo ejecutable se generará en `target/inventario-1.0.0.jar`.

### 3.3 Arrancar la Aplicación
* En entorno de desarrollo local:
```bash
mvn spring-boot:run
```
* O ejecutando el archivo empaquetado:
```bash
java -jar target/inventario-1.0.0.jar
```

---

## 4. Endpoints Técnicos de Verificación
* **Endpoint de Salud Técnico:**
  `GET http://localhost:8080/api/v1/health`
  * Respuesta esperada:
    ```json
    {
      "status": "UP",
      "service": "ferreteria-inventario-backend",
      "environment": "ready"
    }
    ```
* **Spring Boot Actuator Health:**
  `GET http://localhost:8080/actuator/health`
* **Documentación Interactiva Swagger / OpenAPI 3:**
  `http://localhost:8080/swagger-ui.html`
