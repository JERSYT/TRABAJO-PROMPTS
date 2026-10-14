# Documento: Diseño de Contratos de la API REST

## Metadatos
* **Nombre:** 04_diseno_api_rest.md
* **Descripción:** Catálogo completo de endpoints REST, DTOs de entrada y salida, validaciones, códigos HTTP, roles requeridos, efectos sobre el inventario y manejo de errores mediante ProblemDetail (RFC 9457).
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Arquitecto de API REST y Backend)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Convenciones REST y Estándares de Comunicación
* **URL Base:** `/api/v1`
* **Formato de Carga Útil:** JSON (`application/json;charset=UTF-8`).
* **Manejo de Errores:** Estándar **RFC 9457** (`application/problem+json`) mediante `org.springframework.http.ProblemDetail`.
* **Convención de Envoltorios:** **No se utilizará un envoltorio universal obligatorio** (como `ApiResponse<T>`). Las respuestas exitosas devuelven directamente el recurso, arreglo o estructura de paginación de Spring (`Page<T>`), apoyándose en los códigos de estado HTTP nativos.
* **Formatos de Fecha:** ISO-8601 (`YYYY-MM-DDTHH:mm:ss.sssZ`).
* **Paginación y Filtros:** Parámetros estándar: `page` (0-indexed, default 0), `size` (default 20, max 100), `sort` (campo,asc|desc).

---

## 2. Formato de Errores ProblemDetail (RFC 9457)
Toda respuesta de error (4xx y 5xx) sigue la siguiente estructura JSON canónica:

```json
{
  "type": "https://ferreteria.local/errors/stock-insuficiente",
  "title": "Stock Insuficiente",
  "status": 409,
  "detail": "La cantidad solicitada (5.000) excede el saldo disponible actual (2.000) para el producto Tornillo 2 pulg",
  "instance": "/api/v1/sales",
  "timestamp": "2026-10-13T21:30:00Z",
  "invalidParams": [
    {
      "field": "lineas[0].cantidad",
      "reason": "Existencias insuficientes para completar la salida"
    }
  ]
}
```

---

## 3. Catálogo Completo de Endpoints REST por Módulo

### 3.1 Módulo: Autenticación y Sesión (`/api/v1/auth`)

#### **POST /api/v1/auth/login**
* **Descripción:** Autentica credenciales de usuario y crea una sesión web en el servidor respaldada por cookie `JSESSIONID`.
* **Rol requerido:** Público (anónimo).
* **DTO Solicitud (`LoginRequest`):**
  * `username` (`String`, obligatorio, no vacío).
  * `password` (`String`, obligatorio, no vacío).
* **DTO Respuesta (`AuthResponse`):**
  * `id` (`Long`), `username` (`String`), `nombreCompleto` (`String`), `roles` (`List<String>`).
* **Códigos HTTP:** 200 OK (éxito), 400 Bad Request (validación), 401 Unauthorized (credenciales incorrectas).
* **Efectos:** Genera cookie de sesión `JSESSIONID` con atributos `HttpOnly`, `Path=/`, `SameSite=Lax`.

#### **POST /api/v1/auth/logout**
* **Descripción:** Cierra la sesión activa en el servidor e invalida la cookie.
* **Rol requerido:** Autenticado (`ADMINISTRADOR`, `USUARIO_OPERATIVO`).
* **Códigos HTTP:** 204 No Content.
* **Efectos:** Invalida `HttpSession` en el servidor y expira la cookie.

#### **GET /api/v1/auth/me**
* **Descripción:** Obtiene los datos del usuario en la sesión activa actual para verificar estado de autenticación al recargar la interfaz.
* **Rol requerido:** Autenticado.
* **DTO Respuesta:** `AuthResponse`.
* **Códigos HTTP:** 200 OK, 401 Unauthorized.

---

### 3.2 Módulo: Catálogo de Productos (`/api/v1/catalog`)

#### **GET /api/v1/catalog/references**
* **Descripción:** Consulta paginada y filtrable de referencias de productos.
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **Parámetros de Consulta:** `search` (código de barras, nombre), `activo` (booleano, default true), `page`, `size`, `sort`.
* **DTO Respuesta (`Page<ReferenciaResponse>`):**
  * `id`, `codigoBarras`, `productoNombre`, `marcaNombre`, `presentacionNombre`, `unidadMedida`, `precioVenta`, `activo`, `stockDisponible`.
* **Códigos HTTP:** 200 OK.

#### **POST /api/v1/catalog/references**
* **Descripción:** Registra una nueva referencia comercial en el catálogo.
* **Rol requerido:** `ADMINISTRADOR`.
* **DTO Solicitud (`CrearReferenciaRequest`):**
  * `productoId` (`Long`, obligatorio).
  * `marcaId` (`Long`, obligatorio).
  * `presentacionId` (`Long`, obligatorio).
  * `unidadMedidaId` (`Long`, obligatorio).
  * `codigoBarras` (`String`, obligatorio, longitud 3-50, sin espacios).
  * `precioVenta` (`BigDecimal`, obligatorio, min 0.01).
* **DTO Respuesta:** `ReferenciaResponse`.
* **Códigos HTTP:** 201 Created, 400 Bad Request, 409 Conflict (código de barras o tupla duplicada).
* **Efectos:** Crea referencia con `activo=true` y crea automáticamente registro en `inventario_saldo` con saldo 0.000 y CPP 0.0000.

#### **PATCH /api/v1/catalog/references/{id}/price**
* **Descripción:** Actualiza el precio de venta de una referencia y asienta auditoría obligatoria.
* **Rol requerido:** `ADMINISTRADOR` o `USUARIO_OPERATIVO` (con permiso de inventario).
* **DTO Solicitud (`ActualizarPrecioRequest`):**
  * `nuevoPrecio` (`BigDecimal`, obligatorio, min 0.01).
  * `motivo` (`String`, opcional).
* **DTO Respuesta:** `ReferenciaResponse`.
* **Códigos HTTP:** 200 OK, 400 Bad Request, 404 Not Found.
* **Efectos:** Actualiza precio en catálogo y registra evento en `auditoria_log` (RN-09, RN-11).

#### **DELETE /api/v1/catalog/references/{id}**
* **Descripción:** Desactiva lógicamente una referencia (o borra físicamente solo si carece totalmente de historial).
* **Rol requerido:** `ADMINISTRADOR`.
* **Códigos HTTP:** 204 No Content, 404 Not Found, 409 Conflict.
* **Efectos:** Si tiene historial pasa `activo = false` (RN-02).

---

### 3.3 Módulo: Terceros - Clientes y Proveedores (`/api/v1/third-parties`)

#### **GET /api/v1/third-parties**
* **Descripción:** Consulta paginada y filtrable de terceros.
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **Parámetros:** `tipo` (`CLIENTE`, `PROVEEDOR`, `TODOS`), `search` (documento o nombre), `page`, `size`.
* **DTO Respuesta:** `Page<TerceroResponse>`.
* **Códigos HTTP:** 200 OK.

#### **POST /api/v1/third-parties**
* **Descripción:** Registra un nuevo cliente o proveedor unificado (RN-10).
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **DTO Solicitud (`CrearTerceroRequest`):**
  * `tipoDocumento` (`String`, CC, NIT, CE, etc.).
  * `numeroDocumento` (`String`, obligatorio, longitud 4-30).
  * `razonSocial` (`String`, obligatorio, longitud 2-150).
  * `tipoPersona` (`String`, `NATURAL` | `JURIDICA`).
  * `telefono` (`String`, opcional), `email` (`String`, opcional), `direccion` (`String`, opcional).
  * `esCliente` (`Boolean`, obligatorio), `esProveedor` (`Boolean`, obligatorio).
* **DTO Respuesta:** `TerceroResponse`.
* **Códigos HTTP:** 201 Created, 400 Bad Request, 409 Conflict (documento ya registrado).

---

### 3.4 Módulo: Inventario y Movimientos (`/api/v1/inventory`)

#### **GET /api/v1/inventory/stocks**
* **Descripción:** Consulta del saldo actual disponible y costo promedio ponderado de todas las referencias.
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO` (el campo `costoPromedio` es nulo para operadores no autorizados).
* **Parámetros:** `search`, `bajoStock` (booleano), `page`, `size`.
* **DTO Respuesta:** `Page<InventarioSaldoResponse>`.
* **Códigos HTTP:** 200 OK.

#### **GET /api/v1/inventory/references/{id}/movements**
* **Descripción:** Consulta del Kárdex (movimientos cronológicos inmutables) de una referencia.
* **Rol requerido:** `ADMINISTRADOR`.
* **Parámetros:** `desde`, `hasta`, `page`, `size`.
* **DTO Respuesta:** `Page<InventarioMovimientoResponse>`.
* **Códigos HTTP:** 200 OK, 404 Not Found.

#### **POST /api/v1/inventory/initial**
* **Descripción:** Asienta el inventario inicial de una referencia con saldo cero (sin compras simuladas).
* **Rol requerido:** `ADMINISTRADOR` (exclusivo).
* **DTO Solicitud (`InventarioInicialRequest`):**
  * `referenciaId` (`Long`, obligatorio).
  * `cantidadInicial` (`BigDecimal`, obligatorio, min 0.001).
  * `costoUnitario` (`BigDecimal`, obligatorio, min 0.01).
* **DTO Respuesta:** `InventarioSaldoResponse`.
* **Códigos HTTP:** 201 Created, 400 Bad Request, 409 Conflict (la referencia ya tiene saldo o movimientos).
* **Efectos:** Fija saldo inicial, fija CPP inicial, genera movimiento `INICIALIZACION` y audita la acción.

#### **POST /api/v1/inventory/adjustments**
* **Descripción:** Ejecuta un ajuste manual de inventario (físico vs sistema) con motivo documentado.
* **Rol requerido:** `ADMINISTRADOR` (exclusivo - RN-08).
* **DTO Solicitud (`AjusteInventarioRequest`):**
  * `referenciaId` (`Long`, obligatorio).
  * `tipoAjuste` (`String`, `INCREMENTO` | `DISMINUCION`).
  * `cantidad` (`BigDecimal`, obligatorio, min 0.001).
  * `motivo` (`String`, obligatorio, longitud 5-255).
* **DTO Respuesta:** `AjusteInventarioResponse`.
* **Códigos HTTP:** 201 Created, 400 Bad Request, 403 Forbidden, 409 Conflict (disminución excede stock).
* **Efectos:** Actualiza saldo disponible, genera movimiento de Kárdex y registra entrada en auditoría.

---

### 3.5 Módulo: Compras (`/api/v1/purchases`)

#### **POST /api/v1/purchases**
* **Descripción:** Registra y recibe en un solo paso una compra completa a proveedor (RN-03).
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **DTO Solicitud (`CrearCompraRequest`):**
  * `proveedorId` (`Long`, obligatorio).
  * `lineas` (`List<CompraLineaRequest>`, min 1 elemento):
    * `referenciaId` (`Long`, obligatorio).
    * `cantidad` (`BigDecimal`, obligatorio, min 0.001).
    * `costoUnitario` (`BigDecimal`, obligatorio, min 0.01).
* **DTO Respuesta (`CompraResponse`):**
  * `id`, `consecutivo`, `proveedorId`, `fechaHora`, `total`, `estado`, `lineas` (con subtotales calculados).
* **Códigos HTTP:** 201 Created, 400 Bad Request, 404 Not Found (proveedor o producto inexistente).
* **Efectos Transaccionales:**
  * Crea cabecera `CMP-XXXXXX` y líneas.
  * Por cada línea: incrementa stock disponible, genera movimiento de Kárdex `COMPRA` y recalcula CPP (RN-04).

#### **POST /api/v1/purchases/{id}/cancel**
* **Descripción:** Cancela una compra con motivo obligatorio, reintegrando el inventario si las existencias actuales lo permiten.
* **Rol requerido:** `ADMINISTRADOR` (exclusivo).
* **DTO Solicitud (`CancelarDocumentoRequest`):**
  * `motivo` (`String`, obligatorio, min 5 caracteres).
* **DTO Respuesta:** `CompraResponse`.
* **Códigos HTTP:** 200 OK, 400 Bad Request, 403 Forbidden, 409 Conflict (stock insuficiente para revertir).

---

### 3.6 Módulo: Ventas de Contado (`/api/v1/sales`)

#### **POST /api/v1/sales**
* **Descripción:** Registra una venta de contado en efectivo obligatoriamente vinculada a un cliente (RN-05, RN-06).
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **DTO Solicitud (`CrearVentaRequest`):**
  * `clienteId` (`Long`, obligatorio).
  * `montoEfectivoRecibido` (`BigDecimal`, obligatorio).
  * `lineas` (`List<VentaLineaRequest>`, min 1 elemento):
    * `referenciaId` (`Long`, obligatorio).
    * `cantidad` (`BigDecimal`, obligatorio, min 0.001).
    * *(Nota de seguridad: el cliente NO envía precios, costos ni totales; el backend los calcula del catálogo y saldos).*
* **DTO Respuesta (`VentaResponse`):**
  * `id`, `consecutivo` (`VTA-XXXXXX`), `clienteNombre`, `fechaHora`, `subtotal`, `total`, `montoEfectivoRecibido`, `cambioDevuelto`, `estado`, `lineas`.
* **Códigos HTTP:** 201 Created, 400 Bad Request, 404 Not Found, 409 Conflict (stock insuficiente por RN-01 o concurrencia).
* **Efectos Transaccionales:**
  * Bloqueo atómico de las referencias afectadas.
  * Verificación de stock disponible >= cantidad solicitada.
  * Congelamiento del costo histórico vigente (`costo_historico_unitario`) en cada línea (RN-04).
  * Descuento de stock en `inventario_saldo`.
  * Generación de movimientos de Kárdex `VENTA`.
  * Emisión de consecutivo correlativo `VTA-XXXXXX`.

#### **POST /api/v1/sales/{id}/cancel**
* **Descripción:** Cancela una venta con motivo obligatorio, devolviendo las unidades al inventario.
* **Rol requerido:** `ADMINISTRADOR` (exclusivo).
* **DTO Solicitud:** `CancelarDocumentoRequest`.
* **Códigos HTTP:** 200 OK, 403 Forbidden, 409 Conflict (venta con devoluciones asociadas).

---

### 3.7 Módulo: Devoluciones (`/api/v1/returns`)

#### **POST /api/v1/returns**
* **Descripción:** Registra una devolución de cliente (vinculada a venta o libre) y reincorpora existencias a stock (RN-07).
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **DTO Solicitud (`CrearDevolucionRequest`):**
  * `ventaId` (`Long`, opcional).
  * `clienteId` (`Long`, opcional si viene venta).
  * `motivo` (`String`, obligatorio, longitud 5-255).
  * `lineas` (`List<DevolucionLineaRequest>`, min 1 elemento):
    * `referenciaId` (`Long`, obligatorio).
    * `cantidad` (`BigDecimal`, obligatorio, min 0.001).
* **DTO Respuesta (`DevolucionResponse`):**
  * `id`, `consecutivo` (`DEV-XXXXXX`), `tipoDevolucion`, `fechaHora`, `lineas`.
* **Códigos HTTP:** 201 Created, 400 Bad Request, 409 Conflict (cantidad a devolver excede lo vendido en la venta original).
* **Efectos:** Reingresa stock al inventario; si es vinculada usa el costo histórico de la venta original, si es libre usa el CPP vigente (RN-04, RN-07).

---

### 3.8 Módulo: Documentos y Comprobantes (`/api/v1/documents`)

#### **GET /api/v1/documents/{tipo}/{consecutivo}**
* **Descripción:** Obtiene los datos estructurados de un comprobante interno para visualización e impresión.
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **Parámetros de Ruta:** `tipo` (`sales`, `purchases`, `returns`, `adjustments`), `consecutivo` (ej. `VTA-000001`).
* **DTO Respuesta:** `DocumentoComprobanteResponse` (datos de ferretería, cliente/proveedor, líneas, importes en COP).
* **Códigos HTTP:** 200 OK, 404 Not Found.

---

### 3.9 Módulo: Auditoría (`/api/v1/audit`)

#### **GET /api/v1/audit/logs**
* **Descripción:** Consulta paginada y filtrable de la bitácora inmutable de auditoría (RN-11).
* **Rol requerido:** `ADMINISTRADOR` (exclusivo).
* **Parámetros:** `modulo`, `usuarioId`, `desde`, `hasta`, `page`, `size`.
* **DTO Respuesta:** `Page<AuditoriaLogResponse>`.
* **Códigos HTTP:** 200 OK, 403 Forbidden.
