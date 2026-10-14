# Documento: Diseño de Arquitectura del Frontend

## Metadatos
* **Nombre:** 06_diseno_frontend.md
* **Descripción:** Especificación técnica del frontend React, arquitectura modular por características, integración con Material UI, cliente HTTP centralizado, gestión de formularios con Formik y Yup, y control de sesiones sin JWT.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Arquitecto Frontend)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Tecnologías y Principios de Diseño
* **Framework:** React 18+ (SPA) con Vite.
* **Biblioteca de Componentes UI:** Material UI (MUI v5/v6) con un tema personalizado y coherente.
* **Enrutamiento:** React Router (v6+).
* **Gestión de Formularios y Validación:** Formik y Yup como estándar unificado.
* **Comunicación HTTP:** Axios configurado con `withCredentials: true` para envío automático de la cookie de sesión `JSESSIONID` y cabeceras CSRF.
* **Autenticación:** Basada estrictamente en la sesión del backend. **Prohibido el uso de JWT o almacenamiento de tokens en localStorage**.
* **Estado Global:** Context API nativa de React para la sesión del usuario (`AuthContext`) y alertas globales (`NotificationContext`). Sin frameworks complejos de estado global (no Redux, no Zustand) por ser innecesarios para el alcance.

---

## 2. Estructura de Directorios Obligatoria

```
frontend/
├── package.json
├── index.html
├── public/
└── src/
    ├── main.jsx
    ├── App.jsx
    ├── app/
    │   ├── router/          # Configuración de rutas públicas y privadas
    │   ├── providers/       # AuthProvider, ThemeProvider, NotificationProvider
    │   └── theme/           # Paleta de colores, tipografía, overrides de MUI
    ├── assets/              # Iconos, logo de la ferretería, estilos base
    ├── components/
    │   ├── layout/          # MainLayout, Navbar, Sidebar, Footer
    │   ├── feedback/        # LoadingSpinner, ErrorAlert, ConfirmDialog
    │   ├── forms/           # FormikTextField, FormikSelect, CurrencyField
    │   ├── tables/          # DataTable, TablePaginationActions
    │   └── common/          # StatusBadge, PrintContainer, PageHeader
    ├── features/
    │   ├── auth/            # LoginPage, authService, authValidation
    │   ├── users/           # UserListPage, UserFormDialog, userService
    │   ├── catalog/         # ReferenceListPage, ReferenceFormDialog, catalogService
    │   ├── customers/       # CustomerListPage, CustomerFormDialog, customerService
    │   ├── suppliers/       # SupplierListPage, SupplierFormDialog, supplierService
    │   ├── inventory/       # StockOverviewPage, KardexDialog, AdjustmentDialog
    │   ├── purchases/       # PurchaseCreatePage, PurchaseHistoryPage, purchaseService
    │   ├── sales/           # POSSalePage, SaleHistoryPage, saleService
    │   ├── returns/         # ReturnCreatePage, ReturnHistoryPage, returnService
    │   ├── documents/       # VoucherPrintPage, documentService
    │   └── reports/         # StockValuationReportPage, reportService
    ├── services/
    │   └── api/             # apiClient.js, endpoints.js, csrfHelper.js
    ├── hooks/               # useAuth, useDebounce, useNotification
    ├── utils/               # formatters.js (moneda COP, fechas ISO, cantidades)
    └── tests/               # Pruebas de integración frontend y Playwright
```

---

## 3. Catálogo de Páginas y Navegación

| Ruta | Componente / Página | Rol Requerido | Descripción Funcional |
| :--- | :--- | :--- | :--- |
| `/login` | `LoginPage` | Público | Autenticación de credenciales; si ya hay sesión redirige al inicio. |
| `/` | `DashboardRedirect` | Autenticado | Redirecciona a Ventas (Operativo) o a Resumen de Stock (Admin). |
| `/ventas/nueva` | `POSSalePage` | Todos | Punto de venta en mostrador (búsqueda de cliente, escaneo de productos, cobro en efectivo y emisión de recibo). |
| `/ventas` | `SaleHistoryPage` | Todos | Historial de comprobantes de venta con opción de reimpresión y anulación (anulación solo visible y habilitada para Admin). |
| `/compras/nueva` | `PurchaseCreatePage` | Todos | Registro de compra completa a proveedor con líneas y costos. |
| `/compras` | `PurchaseHistoryPage`| Todos | Historial de compras con detalle de recepción y cálculo de CPP. |
| `/inventario` | `StockOverviewPage` | Todos | Tabla de existencias en tiempo real; botón de Ajuste de Stock solo para Admin. |
| `/catalogo` | `ReferenceListPage` | Todos | Listado de artículos del catálogo comercial (creación y desactivación restringida a Admin u Operativo autorizado). |
| `/terceros` | `ThirdPartyListPage`| Todos | Administración unificada de clientes y proveedores. |
| `/devoluciones` | `ReturnCreatePage` | Todos | Registro de devoluciones vinculadas y no vinculadas. |
| `/comprobantes/:tipo/:consecutivo` | `VoucherPrintPage` | Todos | Vista de comprobante lista para impresión por navegador. |
| `/auditoria` | `AuditLogPage` | `ADMINISTRADOR` | Bitácora inmutable de eventos del sistema. |
| `/usuarios` | `UserManagementPage`| `ADMINISTRADOR` | Alta, edición y bloqueo de cuentas de usuario. |

---

## 4. Comunicación con la API y Manejo de Sesiones

### 4.1 Configuración del Cliente HTTP (`src/services/api/apiClient.js`)
* Instancia de Axios con `baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1'`.
* `withCredentials: true` obligatorio en todas las peticiones para viajar con la cookie `JSESSIONID`.
* **Interceptors:**
  * **Request Interceptor:** Lee la cookie `XSRF-TOKEN` proporcionada por Spring Security y la inyecta automáticamente en la cabecera `X-XSRF-TOKEN` para peticiones mutativas (`POST`, `PUT`, `PATCH`, `DELETE`).
  * **Response Interceptor:** 
    * Si la respuesta es HTTP `401 Unauthorized`, limpia el `AuthContext` y redirige a `/login` preservando un aviso amigable ("Su sesión ha expirado o requiere autenticación").
    * Si la respuesta es HTTP `403 Forbidden`, despliega una notificación de advertencia ("No tiene permisos para ejecutar esta acción").
    * Si la respuesta es HTTP `409 Conflict` (ej. stock insuficiente), extrae el campo `detail` del `ProblemDetail` (RFC 9457) y lo presenta en un diálogo de error sin romper la navegación.

---

## 5. Formularios y Validaciones (Formik + Yup)
* **Regla:** Ningún formulario enviará cálculos de negocio autoritativos al backend. 
  * En la venta, el formulario envía únicamente `{ clienteId, lineas: [{ referenciaId, cantidad }], montoEfectivoRecibido }`.
  * Los precios de venta, subtotales, totales, costos e impuestos simulados son calculados y certificados por el backend.
* **Esquemas Yup representativos:**
  * `crearVentaSchema`: Exige `clienteId` numérico positivo, lista de `lineas` con al menos 1 ítem, cada línea con `cantidad` > 0 y `montoEfectivoRecibido` >= total proyectado.
  * `ajusteInventarioSchema`: Exige `referenciaId`, `tipoAjuste` ('INCREMENTO' o 'DISMINUCION'), `cantidad` > 0 y `motivo` con longitud mínima de 5 caracteres.

---

## 6. Experiencia de Usuario, Estados y Formateo
* **Formateadores estándar (`src/utils/formatters.js`):**
  * Moneda COP: Formateo con símbolo `$`, separador de miles `.` y sin decimales visuales o con 2 decimales según contexto (ej. `$ 45.000` o `$ 45.000,00`).
  * Fechas: Formato local colombiano `DD/MM/YYYY hh:mm a`.
  * Cantidades: Separador decimal `,` con hasta 3 dígitos para fracciones (ej. `2,500 m`).
* **Manejo de Estados:**
  * Cada tabla y vista funcional dispone de tres estados canónicos:
    1. **Loading:** Componente `Skeleton` o `CircularProgress` centrado.
    2. **Empty:** Componente `EmptyState` que informa claramente la ausencia de datos con botón para crear el primer registro.
    3. **Error:** Componente `ErrorAlert` que permite reintentar la carga con un clic.
