# Documento: Matriz de Trazabilidad del Diseño Técnico

## Metadatos
* **Nombre:** 10_matriz_trazabilidad_diseno.md
* **Descripción:** Matriz integral de trazabilidad que conecta los requisitos aprobados de la Etapa 1 con las entidades, atributos, servicios, endpoints REST, pantallas y diagramas de diseño definidos en la Etapa 2.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Arquitecto de Software y Requisitos)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Matriz de Trazabilidad Integral: Requisitos a Artefactos de Diseño

| ID Requisito | Entidad JPA y Tablas | Atributos Críticos | Servicio Backend | Endpoint REST | Pantalla / Componente Frontend | Diagrama de Secuencia | Criterio de Aceptación |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **RF-CAT-001** | `Referencia`, `Producto`, `Marca`, `Presentacion` | `codigo_barras (UK)`, `precio_venta`, `activo` | `CatalogService` | `POST /api/v1/catalog/references` | `ReferenceListPage`, `ReferenceFormDialog` | N/A (CRUD Estándar) | **CA-SC-006** |
| **RF-CAT-002** | `Referencia` | `activo (boolean)` | `CatalogService` | `DELETE /api/v1/catalog/references/{id}` | `ReferenceListPage` (Botón Desactivar) | N/A (Soft-Delete) | **CA-SC-006** |
| **RF-CAT-003** | `Referencia`, `AuditoriaLog` | `precio_venta`, `auditoria_log.descripcion` | `CatalogService` | `PATCH /api/v1/catalog/references/{id}/price` | `PriceEditDialog` | N/A (Auditoría) | **CA-SC-009** |
| **RF-INV-001** | `InventarioSaldo` | `cantidad_disponible`, `costo_promedio_ponderado` | `InventoryService` | `GET /api/v1/inventory/stocks` | `StockOverviewPage` | N/A (Lectura) | **CA-SC-001** |
| **RF-INV-002** | `InventarioMovimiento` | `tipo_movimiento`, `cantidad`, `costo_unitario_movimiento` | `InventoryService` | `GET /api/v1/inventory/references/{id}/movements` | `KardexModal` | Diagramas 2, 3, 4 | **CA-SC-002** |
| **RF-INV-003** | `InventarioSaldo`, `InventarioMovimiento` | `cantidad_disponible`, `costo_promedio_ponderado` | `InventoryService` | `POST /api/v1/inventory/initial` | `InitialStockDialog` | N/A (Admin) | **CA-SC-005** |
| **RF-INV-004** | `InventarioSaldo` | `cantidad_disponible >= 0 (CHECK)` | `InventoryService` | `POST /api/v1/sales` (Validación) | `POSSalePage` | **Diagrama 2: Venta** | **CA-SC-001** |
| **RF-COM-001** | `Compra`, `CompraLinea` | `estado = 'RECIBIDA'`, `consecutivo`, `total` | `PurchaseService` | `POST /api/v1/purchases` | `PurchaseCreatePage` | **Diagrama 3: Compra** | **CA-SC-004** |
| **RF-COM-002** | `InventarioSaldo` | `costo_promedio_ponderado` | `InventoryService` | Invocado por `PurchaseService` | `PurchaseHistoryPage` | **Diagrama 3: Compra** | **CA-SC-005** |
| **RF-COM-003** | `Compra`, `InventarioMovimiento` | `estado = 'CANCELADA'`, `motivo_cancelacion` | `PurchaseService` | `POST /api/v1/purchases/{id}/cancel` | `PurchaseHistoryPage` | **Diagrama 6: Cancelación** | **CA-SC-004** |
| **RF-VEN-001** | `Venta`, `VentaLinea` | `consecutivo`, `costo_historico_unitario`, `total` | `SaleService` | `POST /api/v1/sales` | `POSSalePage` | **Diagrama 2: Venta** | **CA-SC-002** |
| **RF-VEN-002** | `Venta`, `InventarioMovimiento` | `estado = 'CANCELADA'`, `motivo_cancelacion` | `SaleService` | `POST /api/v1/sales/{id}/cancel` | `SaleHistoryPage` | **Diagrama 6: Cancelación** | **CA-SC-008** |
| **RF-DEV-001** | `Devolucion`, `DevolucionLinea` | `tipo = 'VINCULADA'`, `costo_unitario_aplicado` | `ReturnService` | `POST /api/v1/returns` | `ReturnCreatePage` | **Diagrama 4: Devolución** | **CA-SC-007** |
| **RF-DEV-002** | `Devolucion`, `DevolucionLinea` | `tipo = 'NO_VINCULADA'`, `costo_unitario_aplicado` | `ReturnService` | `POST /api/v1/returns` | `ReturnCreatePage` | **Diagrama 4: Devolución** | **CA-SC-007** |
| **RF-AJU-001** | `AjusteInventario`, `AuditoriaLog` | `tipo_ajuste`, `cantidad`, `motivo` | `InventoryAdjustmentService` | `POST /api/v1/inventory/adjustments` | `AdjustmentDialog` | **Diagrama 5: Ajuste** | **CA-SC-008** |
| **RF-TER-001** | `Tercero` | `numero_documento (UK)`, `es_cliente`, `es_proveedor` | `ThirdPartyService` | `POST /api/v1/third-parties` | `ThirdPartyListPage`, `ThirdPartyDialog` | N/A | **CA-SC-002** |
| **RF-DOC-001** | `ComprobanteConsecutivo` | `tipo_documento (UK)`, `ultimo_numero`, `prefijo` | `DocumentSequenceService` | Invocado internamente | Mostrado en cabeceras de vistas | N/A | **CA-SC-010** |
| **RF-DOC-002** | N/A (Presentación DTO) | `DocumentoComprobanteResponse` | `DocumentService` | `GET /api/v1/documents/{tipo}/{id}` | `VoucherPrintPage` | N/A | **CA-SC-010** |
| **RF-SEG-001** | `Usuario`, `Rol` | `username (UK)`, `password_hash (BCrypt)` | `AuthService` | `POST /api/v1/auth/login` | `LoginPage` | **Diagrama 1: Login** | **CA-SC-003** |
| **RF-SEG-002** | `UsuarioRol`, `Rol` | `nombre = ROLE_ADMINISTRADOR / ROLE_USUARIO_OPERATIVO` | `SecurityFilterChain` | Todos los endpoints (Filtros RBAC) | Navegación condicionada por rol | Diagramas 1 y 5 | **CA-SC-003** |
| **RF-SEG-003** | `AuditoriaLog` | `usuario_id`, `fecha_hora`, `modulo`, `accion`, `descripcion` | `AuditService` | `GET /api/v1/audit/logs` | `AuditLogPage` | N/A | **CA-SC-009** |

---

## 2. Cobertura del Diseño
* Todos los requisitos de la Etapa 1 tienen asignación directa a entidades, servicios, controladores, rutas frontend y criterios de verificación.
* Las reglas de negocio críticas (`RN-01`, `RN-02`, `RN-03`, `RN-04`, `RN-08`) se materializan en restricciones a nivel de base de datos (`CHECK`, `UK`, `RESTRICT`) y en validaciones transaccionales en la capa `@Service`.
