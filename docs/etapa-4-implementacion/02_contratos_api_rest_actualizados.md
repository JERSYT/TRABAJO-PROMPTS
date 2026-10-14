# Documento: Catálogo Actualizado de Contratos de la API REST

## Metadatos
* **Nombre:** 02_contratos_api_rest_actualizados.md
* **Descripción:** Catálogo definitivo de endpoints REST, esquemas de entrada/salida (DTOs), reglas de validación, roles de seguridad y códigos de respuesta ProblemDetail (RFC 9457) implementados en el backend.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Arquitecto de API REST y Backend)
* **Versión:** 1.0
* **Estado:** Aprobado

---

## 1. Convenciones REST y Estándares de la API
* **Prefijo Base:** `/api/v1`
* **Protocolo y Formato:** HTTP/1.1 y HTTP/2 sobre JSON (`application/json;charset=UTF-8`).
* **Manejo Estandarizado de Errores:** Spring Framework `ProblemDetail` conforme a **RFC 9457** (`application/problem+json`).
* **Autenticación:** Sesiones web mediante cookie de servidor `JSESSIONID` (`HttpOnly`, `SameSite=Lax`).
* **Seguridad CSRF:** Protección obligatoria para verbos mutativos (`POST`, `PUT`, `PATCH`, `DELETE`) vía cabecera `X-XSRF-TOKEN`.

---

## 2. Catálogo Detallado de Endpoints Implementados

### 2.1 Autenticación y Sesiones (`/api/v1/auth`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Anónimo | `LoginRequest` (`username`, `password`) | `AuthResponse` | 200, 400, 401 |
| `POST` | `/api/v1/auth/logout` | Autenticado | N/A | N/A | 204 |
| `GET` | `/api/v1/auth/me` | Autenticado | N/A | `AuthResponse` | 200, 401 |

### 2.2 Gestión de Usuarios (`/api/v1/users`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/users` | `ADMINISTRADOR` | Query: `page`, `size` | `Page<UsuarioResponse>` | 200, 403 |
| `POST` | `/api/v1/users` | `ADMINISTRADOR` | `CrearUsuarioRequest` | `UsuarioResponse` | 201, 400, 409 |
| `PATCH`| `/api/v1/users/{id}/toggle-status` | `ADMINISTRADOR` | N/A | N/A | 204, 404 |

### 2.3 Terceros - Clientes y Proveedores (`/api/v1/third-parties`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/third-parties` | Autenticado | Query: `search`, `tipo`, `page`, `size` | `Page<TerceroResponse>` | 200 |
| `GET` | `/api/v1/third-parties/{id}` | Autenticado | N/A | `TerceroResponse` | 200, 404 |
| `POST` | `/api/v1/third-parties` | Autenticado | `CrearTerceroRequest` | `TerceroResponse` | 201, 400, 409 |
| `PUT` | `/api/v1/third-parties/{id}` | Autenticado | `CrearTerceroRequest` | `TerceroResponse` | 200, 400, 404 |

### 2.4 Catálogo de Artículos (`/api/v1/catalog`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/catalog/references` | Autenticado | Query: `search`, `activo`, `page`, `size` | `Page<ReferenciaResponse>` | 200 |
| `GET` | `/api/v1/catalog/references/{id}` | Autenticado | N/A | `ReferenciaResponse` | 200, 404 |
| `GET` | `/api/v1/catalog/references/barcode/{barcode}` | Autenticado | N/A | `ReferenciaResponse` | 200, 404 |
| `POST` | `/api/v1/catalog/references` | `ADMINISTRADOR` | `CrearReferenciaRequest` | `ReferenciaResponse` | 201, 400, 409 |
| `PATCH`| `/api/v1/catalog/references/{id}/price` | Autenticado | `ActualizarPrecioRequest` | `ReferenciaResponse` | 200, 400, 404 |
| `DELETE`| `/api/v1/catalog/references/{id}` | `ADMINISTRADOR` | N/A | N/A | 204, 404 |

### 2.5 Inventario y Kárdex (`/api/v1/inventory`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/inventory/stocks` | Autenticado | Query: `search`, `bajoStock`, `page`, `size` | `Page<InventarioSaldoResponse>` | 200 |
| `GET` | `/api/v1/inventory/references/{id}/movements` | `ADMINISTRADOR` | Query: `page`, `size` | `Page<InventarioMovimientoResponse>` | 200, 404 |
| `POST` | `/api/v1/inventory/initial` | `ADMINISTRADOR` | `InventarioInicialRequest` | `InventarioSaldoResponse` | 201, 400, 409 |
| `POST` | `/api/v1/inventory/adjustments` | `ADMINISTRADOR` | `AjusteInventarioRequest` | `AjusteInventario` | 201, 400, 409 |
| `GET` | `/api/v1/inventory/adjustments` | `ADMINISTRADOR` | Query: `page`, `size` | `Page<AjusteInventario>` | 200 |

### 2.6 Compras (`/api/v1/purchases`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/purchases` | Autenticado | Query: `estado`, `proveedorId`, `desde`, `hasta` | `Page<CompraResponse>` | 200 |
| `GET` | `/api/v1/purchases/{id}` | Autenticado | N/A | `CompraResponse` | 200, 404 |
| `POST` | `/api/v1/purchases` | Autenticado | `CrearCompraRequest` | `CompraResponse` | 201, 400, 404 |
| `POST` | `/api/v1/purchases/{id}/cancel` | `ADMINISTRADOR` | `CancelarDocumentoRequest` | `CompraResponse` | 200, 400, 409 |

### 2.7 Ventas de Contado (`/api/v1/sales`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/sales` | Autenticado | Query: `estado`, `clienteId`, `desde`, `hasta` | `Page<VentaResponse>` | 200 |
| `GET` | `/api/v1/sales/{id}` | Autenticado | N/A | `VentaResponse` | 200, 404 |
| `POST` | `/api/v1/sales` | Autenticado | `CrearVentaRequest` | `VentaResponse` | 201, 400, 409 |
| `POST` | `/api/v1/sales/{id}/cancel` | `ADMINISTRADOR` | `CancelarDocumentoRequest` | `VentaResponse` | 200, 400, 409 |

### 2.8 Devoluciones (`/api/v1/returns`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/returns` | Autenticado | Query: `page`, `size` | `Page<DevolucionResponse>` | 200 |
| `GET` | `/api/v1/returns/{id}` | Autenticado | N/A | `DevolucionResponse` | 200, 404 |
| `POST` | `/api/v1/returns` | Autenticado | `CrearDevolucionRequest` | `DevolucionResponse` | 201, 400, 409 |

### 2.9 Comprobantes Imprimibles (`/api/v1/documents`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/documents/{tipo}/{consecutivo}` | Autenticado | N/A | `DocumentoComprobanteResponse` | 200, 400, 404 |

### 2.10 Auditoría (`/api/v1/audit`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/audit/logs` | `ADMINISTRADOR` | Query: `modulo`, `usuarioId`, `desde`, `hasta` | `Page<AuditoriaLogResponse>` | 200, 403 |
