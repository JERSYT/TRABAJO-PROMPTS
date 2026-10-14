# Sistema Web de Gestión de Inventario para Ferretería (Sede Única)

[![CI/CD Pipeline](https://github.com/JERSYT/TRABAJO-PROMPTS/actions/workflows/ci.yml/badge.svg)](https://github.com/JERSYT/TRABAJO-PROMPTS/actions/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://adoptium.net/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React 18](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Material UI](https://img.shields.io/badge/MUI-v5-007FFF.svg)](https://mui.com/)
[![MySQL 8.0](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/Uso-Acad%C3%A9mico-lightgrey.svg)]()

Sistema web profesional de inventario para una ferretería en Colombia (moneda **COP**, identificación con **CC/NIT**, sede física única y pago en efectivo), desarrollado de acuerdo con la metodología de **7 Prompts Especializados** de ciclo de vida de software (`PROMPTS.pdf`).

---

## 🚀 Inicio Rápido: ¿Cómo Correr el Proyecto?

### Opción 1: Ejecución Local Directa (Recomendada para Desarrollo)

#### Prerrequisitos
* **Java 21 LTS** y **Maven 3.9+** instalados y en el PATH.
* **Node.js v18+** y **npm** instalados.

#### Paso 1: Levantar el Backend (Puerto 8080)
Abre una terminal y ejecuta:
```bash
cd backend
mvn spring-boot:run
```
> El backend arrancará con perfil `local` (base de datos en memoria con emulación MySQL y datos sintéticos precargados de inmediato).
> Verifícalo en el navegador: [http://localhost:8080/api/v1/health](http://localhost:8080/api/v1/health) *(responde `{"status":"UP"}`)*.

#### Paso 2: Levantar el Frontend (Puerto 5173)
Abre otra terminal y ejecuta:
```bash
cd frontend
npm install
npm run dev
```
> Abre tu navegador en: **[http://localhost:5173](http://localhost:5173)**

---

### Opción 2: Ejecución Hermética con Docker Compose ($0 COP)

Si tienes Docker instalado en tu máquina o servidor, puedes levantar el stack completo (MySQL 8.0 + Backend + Frontend servido con Nginx) con un solo comando:
```bash
docker compose up -d
```
* **Frontend:** [http://localhost](http://localhost) (Puerto 80)
* **Backend:** [http://localhost:8080](http://localhost:8080) (Puerto 8080)
* **MySQL:** Puerto 3306 con volumen persistente `ferreteria_mysql_data`.

---

## 🔑 Credenciales de Acceso al Sistema

Apenas arranca la aplicación, el inicializador automático (`AdminInitializerRunner`) aprovisiona dos usuarios con contraseñas cifradas en **BCrypt**:

| Rol | Usuario | Contraseña | Privilegios y Alcance |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin_ferreteria` | `Admin123*` | Control total, catálogo, Kárdex, ajustes manuales de stock, cancelaciones, auditoría y observabilidad. |
| **Usuario Operativo** | `operador_mostrador` | `Operador123*` | Punto de venta en mostrador, registro de compras, clientes/proveedores y devoluciones. |

---

## 📦 Arquitectura del Sistema

El sistema implementa una **arquitectura tradicional por capas** y desacoplamiento estricto en dos repositorios:

```
[Cliente Navegador]
       │
       ▼  (Peticiones HTTP con Cookies JSESSIONID + Cabecera X-XSRF-TOKEN)
[Frontend React 18 + MUI] (Puerto 5173 / 80)
       │
       ▼  (/api/v1/...)
[Backend Spring Boot 3 / Java 21] (Puerto 8080)
   ├── Controller (Endpoints REST declarativos con validación)
   ├── Service (@Transactional, Bloqueo Pesimista, Cálculo CPP)
   └── Repository (Spring Data JPA)
       │
       ▼
[Base de Datos Relacional]
   └── MySQL 8.0 / InnoDB (Scripts manuales 01_schema.sql y 02_initial_data.sql)
```

---

## ⚙️ Reglas de Negocio Implementadas (RN-01 a RN-12)

1. **RN-01 (Integridad y Prevención de Stock Negativo):** Bloqueo pesimista `SELECT ... FOR UPDATE` sobre la tabla de saldos durante la venta para evitar que operaciones concurrentes dejen existencias menores a cero.
2. **RN-02 (Borrado Lógico):** Productos con historial en compras o ventas nunca se eliminan físicamente; se desactivan (`activo = false`) preservando la trazabilidad.
3. **RN-03 (Compras Completas):** Recepción total en una única transacción atómica; sin entregas parciales.
4. **RN-04 (Costo Promedio Ponderado - CPP):** Actualización automática del CPP tras cada compra según la fórmula:
   $$CPP_{nuevo} = \frac{(Stock_{actual} \times CPP_{actual}) + (Cant_{comprada} \times CostoUnit_{compra})}{Stock_{actual} + Cant_{comprada}}$$
   Las líneas de venta congelan el costo histórico vigente en el momento exacto de la transacción.
5. **RN-05 (Cliente Obligatorio):** Toda venta exige estar vinculada a un cliente previamente registrado en el sistema.
6. **RN-06 (Pago en Efectivo):** Cobro exclusivo de contado en efectivo con cálculo automático de cambio/vueltas.
7. **RN-07 (Devoluciones de Clientes):** Reingreso automático de unidades al inventario (a costo histórico si es vinculada a una venta, o a CPP vigente si es libre).
8. **RN-08 (Ajustes de Inventario Exclusivos):** Operación manual autorizada únicamente para el Administrador, exigiendo motivo documentado y auditoría.
9. **RN-09 (Modificación de Precios):** Cambio de precio auditado que no altera los valores de ventas pasadas.
10. **RN-10 (Terceros Unificados):** Un mismo registro de persona/empresa (NIT o CC) puede operar como cliente y proveedor sin duplicar datos.
11. **RN-11 (Auditoría Básica):** Bitácora inmutable que almacena: usuario responsable, fecha/hora exacta y descripción de la acción.
12. **RN-12 (Comprobantes y Consecutivos):** Numeración correlativa continua (`VTA-`, `CMP-`, `DEV-`, `AJU-`) y vista de impresión de tickets sin integración fiscal real con la DIAN.

---

## 🧪 Pruebas Automatizadas (20/20 Aprobadas)

Para ejecutar la batería completa de pruebas automatizadas del backend:
```bash
cd backend
mvn clean test
```
* **`ConcurrencyInventoryTest`:** Simulación multihilo con 10 hilos simultáneos sobre stock escaso (0 existencias negativas garantizadas).
* **`SecurityRbacTest`:** Verificación de protección 401 sin sesión, 403 ante llamadas no autorizadas del operador y bloqueo sin token CSRF.
* **`ReturnsAndAdjustmentsTest`:** Verificación de restricciones de devolución y anulación de ventas.
* **`InventoryServiceTest`:** Comprobación matemática exacta de la fórmula del Costo Promedio Ponderado.
* **`SaleControllerTest`:** Integración de la API de ventas y validaciones de caja.

Para construir y verificar el bundle del frontend:
```bash
cd frontend
npm run build
```

---

## 🌐 Módulos y Pantallas del Frontend

* `/login` : Inicio de sesión seguro con cookies `HttpOnly` y protección CSRF.
* `/dashboard` : Panel con métricas de existencias, productos críticos y accesos directos.
* `/ventas/nueva` : Punto de venta de mostrador (POS) con selector de clientes, cálculo de totales y liquidación de efectivo.
* `/ventas` : Historial de comprobantes de venta, desglose con costo histórico y anulación administrativa.
* `/compras/nueva` : Recepción completa de mercancía a proveedores y actualización de costos.
* `/inventario` : Tabla de saldos en tiempo real, filtro de bajo stock, Kárdex por artículo y modal de ajustes.
* `/catalogo` : Referencias comerciales (Producto + Marca + Presentación), códigos de barra y edición de precios.
* `/terceros` : Directorio unificado de clientes y proveedores con identificación fiscal (CC/NIT).
* `/devoluciones` : Registro de devoluciones vinculadas y libres con reingreso automático al stock.
* `/comprobantes/:tipo/:consecutivo` : Vista estructurada para impresión de tickets/facturas de mostrador.
* `/observabilidad` : Dashboard técnico para Administrador con métricas de latencia, Actuator, salud del backend y seguimiento de CI/CD.
* `/auditoria` : Bitácora inmutable de eventos.
* `/usuarios` : Gestión de cuentas y asignación de roles.

---

## 📚 Documentación Técnica Detallada del Proyecto

Toda la documentación generada durante las 7 etapas se encuentra organizada en la carpeta `docs/`:
* `docs/etapa-1-requisitos/`: Especificación formal de requisitos (ISO 29148 / 25010) y reglas de negocio.
* `docs/etapa-2-diseno/`: Arquitectura por capas, modelo entidad-relación (Mermaid), contratos REST y diagramas de secuencia.
* `docs/etapa-3-preparacion/`: Plan de implementación, decisiones técnicas y evidencias de scaffolding.
* `docs/etapa-4-implementacion/`: Informe funcional, contratos API finales y procedimientos de base de datos.
* `docs/etapa-5-testing/`: Plan de pruebas, matriz de trazabilidad y reporte de testing y concurrencia.
* `docs/etapa-6-despliegue/`: Evaluación de PaaS gratuitos ($0 COP), manuales de operación, copias de seguridad y Docker.
* `docs/etapa-7-hardening/`: Matriz global de aceptación (criterios E7-01 a E7-25) e informe de cierre.
* **`RESUMEN_EJECUTIVO_PROYECTO.pdf`**: Documento ejecutivo completo consolidado listo para presentación o entrega académica.
