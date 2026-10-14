# Documento: Plan de Pruebas y Matriz de Trazabilidad QA

## Metadatos
* **Nombre:** 01_plan_pruebas_y_trazabilidad.md
* **Descripción:** Plan maestro de testing integral, estrategia de aseguramiento de calidad y matriz de trazabilidad bidireccional entre requisitos aprobados, riesgos, suites de prueba y evidencias.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Equipo de QA y Automatización de Pruebas)
* **Versión:** 1.0
* **Estado:** Aprobado

---

## 1. Alcance y Objetivos del Testing Integral (Etapa 5)
El objetivo de esta etapa es someter al sistema de inventario a una batería exhaustiva de validaciones automatizadas para garantizar su estabilidad, seguridad transaccional, consistencia matemática y robustez ante concurrencia antes de preparar el despliegue.

### 1.1 Tipos de Pruebas Incluidas
1. **Pruebas Unitarias de Lógica Crítica:** Verificación de cálculos de Costo Promedio Ponderado (CPP), subtotales, cambio en efectivo y validación de entidades.
2. **Pruebas de Integración y Concurrencia:** Simulación multihilo con `ExecutorService` y `CountDownLatch` para validar el bloqueo pesimista contra saldos negativos (RN-01).
3. **Pruebas de Seguridad y Autorización (RBAC):** Verificación de denegación de acceso (401 Unauthorized, 403 Forbidden) en endpoints administrativos invocados por el Usuario Operativo.
4. **Pruebas de Reglas de Negocio en Devoluciones y Cancelaciones:** Restricción de exceso de cantidad devuelta y bloqueo de anulación de ventas con devoluciones asociadas.
5. **Pruebas End-to-End (E2E) con Playwright:** Flujos críticos en el navegador web (login, mostrador, inventario, emisión de comprobante).

---

## 2. Matriz de Trazabilidad Integral QA

| Requisito Aprobado | Riesgo Técnico Asociado | Caso de Prueba | Tipo de Prueba | Entorno / Herramienta | Resultado Esperado | Estado | Evidencia |
| :--- | :--- | :--- | :--- | :--- | :--- | :---: | :--- |
| **RF-INV-004 / RN-01** | Ventas simultáneas que dejen stock negativo. | `testConcurrenciaVentasStockNegativo` | Concurrencia / Transaccional | JUnit 5 + Spring Boot | Exactamente el stock disponible se descuenta; peticiones excedentes son rechazadas sin saldos < 0. | **Aprobado** | `ConcurrencyInventoryTest.java` |
| **RF-COM-002 / RN-04** | Error de redondeo o cálculo erróneo en CPP. | `testRecalculoCostoPromedioPonderado` | Unitaria / Matemática | JUnit 5 | Nuevo CPP = $2.500,0000 COP exacto tras compra de 10 unidades a $3.000 con base 10 a $2.000. | **Aprobado** | `InventoryServiceTest.java` |
| **RF-SEG-002 / RN-08** | Usuario operativo ejecutando ajustes de stock. | `operativoNoPuedeEjecutarAjustes` | Seguridad / RBAC | MockMvc + Spring Security | Retorna HTTP 403 Forbidden ante llamada POST a `/api/v1/inventory/adjustments`. | **Aprobado** | `SecurityRbacTest.java` |
| **RF-SEG-002 / RN-08** | Usuario operativo gestionando cuentas de usuario. | `operativoNoPuedeGestionarUsuarios` | Seguridad / RBAC | MockMvc + Spring Security | Retorna HTTP 403 Forbidden ante llamada GET/POST a `/api/v1/users`. | **Aprobado** | `SecurityRbacTest.java` |
| **RF-SEG-001** | Acceso a endpoints protegidos sin sesión activa. | `accesoSinSesionRetorna401` | Seguridad / Autenticación | MockMvc | Retorna HTTP 401 Unauthorized sin exponer trazas de error internas. | **Aprobado** | `SecurityRbacTest.java` |
| **RF-DEV-001 / RN-07** | Devolución de cantidad superior a la vendida. | `devolucionNoPuedeExcederCantidadVendida` | Negocio / Validación | JUnit 5 + Spring Boot | Lanza `BusinessException` y bloquea la transacción. | **Aprobado** | `ReturnsAndAdjustmentsTest.java` |
| **RF-VEN-002** | Cancelar venta con devoluciones previas registradas. | `cancelarVentaConDevolucionBloqueado` | Integridad / Consistencia | JUnit 5 + Spring Boot | Lanza `BusinessException` impidiendo la doble reincorporación de existencias. | **Aprobado** | `ReturnsAndAdjustmentsTest.java` |
| **RF-DOC-001 / RN-12** | Consecutivos correlativos sin saltos ni duplicados. | `generarConsecutivosCorrelativos` | Transaccional / Secuencias | JUnit 5 | Genera secuencia ininterrumpida (`VTA-000101`, `VTA-000102`, etc.). | **Aprobado** | `ReturnsAndAdjustmentsTest.java` |
| **RF-VEN-001 / RN-05** | Venta sin cliente registrado o pago insuficiente. | `ventaSinClienteFallaValidacion` | Validación REST (Bean Validation) | MockMvc | Retorna HTTP 400 Bad Request o HTTP 409 Conflict con ProblemDetail (RFC 9457). | **Aprobado** | `SaleControllerTest.java` |
| **E2E Flujo General** | Incompatibilidad frontend con API REST de sesión. | `flujoCompletoLoginYVenta` | End-to-End (E2E) | Playwright + Chromium | Inicio de sesión, navegación al POS, selección de cliente, cobro y visualización de recibo. | **Aprobado** | `ferreteria.spec.js` |
