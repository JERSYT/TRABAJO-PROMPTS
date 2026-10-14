# Documento: Procedimiento de Base de Datos y Scripts SQL

## Metadatos
* **Nombre:** 03_procedimiento_base_datos.md
* **Descripción:** Procedimiento operativo estandarizado para la creación, inicialización manual y mantenimiento del esquema de base de datos MySQL 8.0 sin Flyway ni Liquibase.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Administrador de Base de Datos y Backend)
* **Versión:** 1.0
* **Estado:** Aprobado

---

## 1. Política de Gestión de Base de Datos (Decisión Aprobada)
* **Sin herramientas automáticas:** Queda estrictamente excluido el uso de herramientas como Flyway o Liquibase para evitar dependencias innecesarias en una demostración académica.
* **Prohibido `ddl-auto=update`:** En entornos con datos persistentes se prohíbe delegar en Hibernate la alteración automática del esquema (`ddl-auto: validate` en desarrollo y `none` en producción).
* **Gestión manual y versionada:** Todo cambio al modelo relacional debe plasmarse en un script SQL descriptivo, versionado y aplicado de forma explícita por el operador.

---

## 2. Catálogo de Scripts SQL Oficiales
Los scripts residen en `backend/src/main/resources/db/`:
1. `01_schema.sql`: Script DDL para creación de tablas, llaves primarias, foráneas, restricciones de unicidad (`UNIQUE`), restricciones de dominio (`CHECK`), índices e integridad referencial (`ON DELETE RESTRICT`).
2. `02_initial_data.sql`: Script DML que contiene los roles fundamentales (`ROLE_ADMINISTRADOR`, `ROLE_USUARIO_OPERATIVO`), usuario administrador inicial con hash BCrypt, unidades de medida estándar (`UND`, `MTR`, `KGM`, `GLN`, `CJA`), marcas, presentaciones, referencias base y clientes/proveedores sintéticos de demostración.

---

## 3. Procedimiento de Instalación Limpia en MySQL 8.0

### Paso 1: Crear la Base de Datos con el Juego de Caracteres Aprobado
Conectarse al gestor MySQL mediante consola o herramienta administrativa (Workbench/DBeaver) y ejecutar:
```sql
CREATE DATABASE IF NOT EXISTS ferreteria_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ferreteria_db;
```

### Paso 2: Aplicar el Script DDL de Creación del Esquema
Desde la terminal del sistema operativo:
```bash
mysql -u root -p ferreteria_db < "backend/src/main/resources/db/01_schema.sql"
```
* **Verificación de éxito:** Comprobar que se han creado las 16 tablas relacionales (`SHOW TABLES;`).

### Paso 3: Aplicar el Script DML de Datos Iniciales y Sintéticos
```bash
mysql -u root -p ferreteria_db < "backend/src/main/resources/db/02_initial_data.sql"
```
* **Verificación de éxito:** Consultar que la tabla `usuario` contenga los usuarios `admin_ferreteria` y `operador_mostrador`, y que `inventario_saldo` registre los saldos de arranque.

---

## 4. Parámetros de Conexión en Spring Boot
En `backend/src/main/resources/application-dev.yml` o mediante variables de entorno:

| Variable | Valor Típico | Descripción |
| :--- | :--- | :--- |
| `DB_HOST` | `localhost` | Dirección IP o host del servidor MySQL. |
| `DB_PORT` | `3306` | Puerto de conexión MySQL. |
| `DB_NAME` | `ferreteria_db` | Nombre de la base de datos. |
| `DB_USER` | `root` | Usuario autorizado. |
| `DB_PASSWORD`| *(secreto local)* | Contraseña del usuario. |

---

## 5. Procedimientos de Respaldo y Restauración (Backup & Restore)

### 5.1 Generación de Copia de Seguridad Lógica
```bash
mysqldump -u root -p --single-transaction --routines --triggers --databases ferreteria_db > backup_ferreteria_$(date +%Y%m%d_%H%M%S).sql
```
*La opción `--single-transaction` garantiza consistencia transaccional sin bloquear lecturas de mostrador en tablas InnoDB.*

### 5.2 Restauración de una Copia en Entorno Aislado
```bash
mysql -u root -p ferreteria_db_test < backup_ferreteria_YYYYMMDD_HHMMSS.sql
```
