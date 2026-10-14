# Documento: Riesgos y Decisiones Pendientes del Diseño

## Metadatos
* **Nombre:** 11_riesgos_y_pendientes_diseno.md
* **Descripción:** Registro estructurado de decisiones pendientes del diseño técnico, riesgos de concurrencia y consistencia, supuestos provisionales y planes de mitigación.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Arquitecto de Software y Analista de Riesgos)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Decisiones Pendientes y Propuestas de Resolución

### DP-TEC-01: Manejo de Cancelación de Venta con Devoluciones Previas
* **Descripción del Problema:** Si una venta de mostrador tuvo una devolución parcial aceptada en el pasado, ¿cómo debe reaccionar el sistema si el Administrador solicita anular la venta original?
* **Consecuencias:** Si se anula la venta sin control, se devolvería al stock la cantidad completa vendida originalmente, duplicando las existencias de las unidades que ya habían sido reincorporadas por la devolución previa.
* **Propuesta Técnica:** Bloquear la cancelación directa de cualquier venta que posea registros asociados en la tabla `devolucion`. El sistema retornará un error HTTP 409 indicando: *"La venta posee devoluciones registradas. Resuelva o anule las devoluciones asociadas antes de proceder"*.
* **Instancia Aprobatoria:** Responsable del Negocio / Administrador.
* **Impacto Documental:** Afecta a `SaleService` y el caso de uso `CU-10`.

---

### DP-TEC-02: Impacto en el CPP al Anular una Compra Pasada
* **Descripción del Problema:** Cuando se anula una compra registrada anteriormente, ¿se debe recalcular retroactivamente el Costo Promedio Ponderado o registrar un ajuste compensatorio al costo vigente?
* **Consecuencias:** Recalcular hacia atrás todas las transacciones históricas alteraría los costos históricos ya congelados en las ventas pasadas, violando el principio de inmutabilidad de la facturación.
* **Propuesta Técnica:** El costo histórico congelado en `venta_linea` **nunca se modifica**. La anulación de la compra descuenta las unidades físicas al costo unitario de dicha compra y reajusta el CPP del stock remanente en `inventario_saldo`. Si las existencias actuales son menores a las compradas originalmente (porque ya se vendieron), la anulación de la compra se bloquea por falta de stock.
* **Instancia Aprobatoria:** Responsable del Negocio / Contador.
* **Impacto Documental:** Afecta a `PurchaseService` y `InventoryService`.

---

### DP-TEC-03: Política de Consecutivos ante Reinicio Anual o Mensual
* **Descripción del Problema:** ¿Los consecutivos internos (`VTA-000001`, `CMP-000001`) deben reiniciarse cada año fiscal o ser estrictamente correlativos e indefinidos en el tiempo?
* **Propuesta Técnica:** Mantener numeración correlativa continua e indefinida (sin reinicio) para esta versión académica de sede única, simplificando la gestión de la tabla `comprobante_consecutivo`.
* **Instancia Aprobatoria:** Administrador del Sistema.
* **Impacto Documental:** Afecta a `DocumentSequenceService`.

---

## 2. Supuestos Provisionales Adoptados en el Diseño
1. **Precisión Numérica en Cantidades Físicas:** Se asume provisionalmente que 3 decimales (`DECIMAL(12, 3)`) son suficientes para ferretería en artículos vendidos por metro o kilo (ej. 0.250 kg de clavos o 3.750 metros de manguera).
2. **Escala Monetaria:** Se asume que no existen centavos en la circulación física del peso colombiano (COP) para mostrador, pero la base de datos almacena `DECIMAL(15, 2)` para permitir exactitud en divisiones de costos promedio.
3. **Moneda Única:** Se asume exclusivamente el Peso Colombiano (COP) sin requerimientos multimoneda.

---

## 3. Matriz de Riesgos Técnicos, Consistencia y Seguridad

| ID | Categoría | Descripción del Riesgo | Impacto | Probabilidad | Estrategia de Mitigación en el Diseño |
| :--- | :--- | :--- | :---: | :---: | :--- |
| **RT-01** | **Consistencia** | Fallo en la actualización atómica entre `inventario_saldo` y `inventario_movimiento`. | Crítico | Baja | Envolver toda mutación dentro de métodos con `@Transactional(rollbackFor = Exception.class)`. |
| **RT-02** | **Concurrencia** | Dos solicitudes HTTP simultáneas vendiendo la última unidad de una referencia escasa. | Crítico | Media | Uso de `SELECT ... FOR UPDATE` (bloqueo pesimista en JPA) al verificar y descontar existencias. |
| **RT-03** | **Seguridad** | Ataque CSRF en peticiones mutativas (`POST /api/v1/sales`) debido a autenticación por cookies. | Alto | Media | Repositorio `CookieCsrfTokenRepository.withHttpOnlyFalse()` con validación obligatoria de cabecera `X-XSRF-TOKEN` en el cliente Axios. |
| **RT-04** | **Seguridad** | Ataques de fuerza bruta contra el endpoint `/api/v1/auth/login`. | Medio | Media | Hashing con BCrypt (coste 10) y respuestas genéricas de error sin revelar si el usuario existe. |
| **RT-05** | **Datos** | Desajuste del esquema MySQL por ausencia de herramientas automáticas de migración. | Alto | Media | Scripts SQL rigurosamente versionados (`01_schema.sql`, `02_initial_data.sql`), revisados e idempotentes cuando sea factible. |
| **RT-06** | **Integridad** | Eliminación accidental de productos con transacciones comerciales históricas. | Alto | Baja | Claves foráneas configuradas con `ON DELETE RESTRICT` y aplicación de borrado lógico (`activo = false`). |
