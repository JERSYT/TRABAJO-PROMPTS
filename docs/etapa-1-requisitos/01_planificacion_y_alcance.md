# Documento: Planificación y Alcance del Producto

## Metadatos
* **Nombre:** 01_planificacion_y_alcance.md
* **Descripción:** Documento de definición del problema, contexto de negocio, objetivos, alcance, exclusiones, restricciones y riesgos iniciales para el Sistema Web de Gestión de Inventario para Ferretería.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Analista de Requisitos y Sistemas)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Declaración del Problema y Necesidad del Negocio
### 1.1 Problema que se busca resolver
Las micro y pequeñas ferreterías en el contexto colombiano operan habitualmente con registros manuales, hojas de cálculo dispersas o controles informales de mostrador. Esta modalidad genera discrepancias críticas entre las existencias físicas y los saldos registrados, ventas de mercancía sin stock disponible, incertidumbre en la valoración real del inventario (por falta de cálculo dinámico del costo promedio) y pérdida de trazabilidad en las compras, ventas, devoluciones y ajustes operativos.

### 1.2 Situación actual vs. Hechos confirmados y Supuestos
* **Hechos confirmados:**
  * El negocio opera en una **única sede física** con un **único almacén/inventario centralizado**.
  * Todas las transacciones comerciales se realizan en pesos colombianos (**COP**) con clientes y proveedores identificados formalmente (cédula de ciudadanía CC o NIT).
  * La modalidad de venta autorizada para esta versión es exclusivamente **de contado en efectivo** (no se admite crédito).
  * Las compras se registran y reciben en su totalidad en una única operación (no hay entregas parciales de proveedores).
  * Se requiere simular comprobantes internos con numeración consecutiva sin conexión real con la DIAN.
* **Supuestos identificados:**
  * La ferretería cuenta con conectividad de red local o acceso a Internet estable para acceder a la aplicación web a través de un navegador moderno.
  * El personal operativo posee conocimientos informáticos básicos a nivel de usuario de oficina.

---

## 2. Objetivos del Producto
### 2.1 Objetivo General
Desarrollar e implementar un sistema web profesional, seguro y mantenible para centralizar la administración del inventario, compras, ventas, catálogo, clientes, proveedores, devoluciones y ajustes de una ferretería de sede única, asegurando la integridad transaccional y el control de costos mediante el método de costo promedio ponderado.

### 2.2 Objetivos Específicos
1. Centralizar el catálogo de artículos basado en combinaciones de **Producto, Marca y Presentación**, asociando unidades base y códigos de barras únicos.
2. Garantizar la consistencia del saldo disponible mediante control de concurrencia y bloqueo de ventas cuando las existencias solicitadas superen el inventario físico real.
3. Valorar con precisión financiera el inventario mediante el cálculo automático del **Costo Promedio Ponderado (CPP)** tras cada compra registrada.
4. Preservar la trazabilidad inmutable de cada transacción a través de un historial persistente de movimientos de inventario (`Kárdex`) y un log básico de auditoría de usuarios.
5. Gestionar el control de acceso estricto mediante dos roles autorizados: **Administrador** y **Usuario Operativo**.
6. Generar comprobantes comerciales internos con numeración consecutiva e interfaz apta para impresión.

---

## 3. Beneficios Esperados
* **Operativos:** Eliminación del desabastecimiento oculto y de ventas en negativo; agilidad en el despacho de mostrador y registro de compras.
* **Financieros:** Margen de ganancia calculado sobre costos históricos reales y saldos valorados conforme al costo promedio ponderado.
* **Control y Auditoría:** Responsabilidad identificada en cada ajuste manual, anulación y modificación de precios de venta.

---

## 4. Alcance del Producto (Módulos Confirmados)
El sistema comprenderá 12 módulos funcionales interconectados:
1. **Módulo de Catálogo y Productos:** Registro, actualización, desactivación lógica y consulta de artículos parametrizados por producto, marca y presentación comercial.
2. **Módulo de Gestión de Inventario:** Consulta de saldos en tiempo real, registro de movimientos (entradas, salidas, ajustes) y control estricto de existencias disponibles.
3. **Módulo de Inventario Inicial:** Proceso controlado para asentar el inventario de arranque con cantidades y costo unitario de referencia (sin simular compras ficticias).
4. **Módulo de Compras:** Registro de órdenes de compra con múltiples líneas, recepción total en un único paso y recálculo del costo promedio ponderado.
5. **Módulo de Valoración y Costos:** Motor de cálculo del costo promedio ponderado por referencia de inventario.
6. **Módulo de Ventas de Contado:** Facturación en mostrador obligatoriamente asociada a un cliente registrado, cobro en efectivo y descuento transaccional atómico de stock.
7. **Módulo de Terceros (Clientes y Proveedores):** Administración unificada de personas naturales y jurídicas, permitiendo que un mismo tercero opere como cliente y proveedor sin duplicidad.
8. **Módulo de Pagos:** Registro de pagos en efectivo vinculados a comprobantes de venta.
9. **Módulo de Devoluciones de Clientes:** Registro de devoluciones autorizadas con reincorporación inmediata de existencias al inventario; distinción de costo histórico para devoluciones vinculadas vs. costo promedio para no vinculadas.
10. **Módulo de Ajustes de Inventario:** Operaciones de corrección de stock reservadas exclusivamente para el Administrador, con motivo documentado y auditoría.
11. **Módulo de Documentos y Comprobantes:** Generación de consecutivos internos (Ventas, Compras, Devoluciones, Ajustes), simulación de comprobante fiscal y vista de impresión.
12. **Módulo de Seguridad y Auditoría:** Autenticación por sesiones Spring Security, control de acceso por roles (RBAC) y bitácora de auditoría básica (usuario, fecha, hora, descripción de acción).

---

## 5. Exclusiones Explícitas (Fuera de Alcance Inicial)
Conforme a las directrices aprobadas, quedan estrictamente excluidas de esta versión:
1. Soporte para múltiples sucursales, almacenes o bodegas físicas.
2. Recepciones parciales o fraccionadas de compras.
3. Venta a crédito, cuentas por cobrar o cuentas por pagar diferidas.
4. Integración web real con los servicios de la DIAN para facturación electrónica oficial.
5. Importación masiva inicial de productos mediante archivos Excel o CSV.
6. Funcionamiento en modo offline / sin conexión a Internet.
7. Drivers para comunicación por hardware con lectores físicos de códigos de barras (se acepta entrada de teclado estándar con escáner USB/emulador de teclado).
8. Control de números de serie, lotes industriales, fechas de caducidad o pólizas de garantía de productos.
9. Devoluciones a proveedores externos.
10. Reserva anticipada o apartado de productos en stock.
11. Cierre contable y fiscal formal de períodos.
12. Integraciones con sistemas ERP, pasarelas bancarias o plataformas de pago externas.

---

## 6. Restricciones Técnicas y Arquitectónicas Confirmadas
* **Arquitectura de Software:** Arquitectura tradicional por capas (`Controller` → `Service` → `Repository`). No se utilizarán arquitecturas distribuidas, microservicios, Event Sourcing, CQRS ni Arquitectura Hexagonal.
* **Separación de Repositorios:** Dos repositorios de código independientes (`backend` y `frontend`). Prohibido estructurarlo como monorepo.
* **Stack Backend:** Java 21 LTS, Spring Boot 3, Spring MVC, Spring Data JPA / Hibernate, Spring Security, Maven.
* **Stack Frontend:** React, Material UI (MUI), Formik, Yup, React Router.
* **Persistencia:** Base de datos relacional MySQL 8.0. Sin herramientas automáticas de migración tipo Flyway o Liquibase; creación del esquema mediante scripts SQL versionados y revisados.
* **Mecanismo de Autenticación:** Sesiones web gestionadas por Spring Security con cookies `HttpOnly` y protección contra `CSRF` y `CORS`. Prohibido el uso de tokens JWT.
* **Moneda e Identificación:** Transacciones en pesos colombianos (COP) utilizando enteros o decimales con escala definida sin usar tipos `FLOAT` o `DOUBLE`. Documentos bajo estándar colombiano (CC, NIT, CE, Pasaporte).
* **Presupuesto:** COP $0 en costos de licencias o infraestructura en la nube.

---

## 7. Riesgos Iniciales y Dependencias
| ID | Riesgo Identificado | Probabilidad | Impacto | Estrategia de Mitigación |
| :--- | :--- | :--- | :--- | :--- |
| **RSK-01** | Errores de concurrencia al realizar ventas simultáneas del mismo producto con stock escaso. | Media | Crítico | Implementar bloqueo pesimista o atómico en la consulta y descuento del stock dentro de transacciones aisladas `@Transactional`. |
| **RSK-02** | Inconsistencia en la valoración del inventario tras devoluciones o cancelaciones. | Media | Alto | Definir matemáticamente el impacto en el CPP para cada tipo de movimiento y registrar el costo histórico en cada renglón de venta. |
| **RSK-03** | Fuga de sesión o fallos de CORS/CSRF al operar frontend y backend en orígenes cruzados. | Media | Alto | Configuración estricta de políticas de cookies (`SameSite`, `HttpOnly`), cabeceras CSRF (`X-XSRF-TOKEN`) y credenciales habilitadas (`withCredentials: true`). |
| **RSK-04** | Discrepancia entre esquemas de desarrollo y pruebas por ausencia de herramienta de migración. | Alta | Medio | Establecer un catálogo estricto de scripts SQL versionados (`01_schema.sql`, `02_data.sql`, etc.) ejecutados de forma controlada. |
