# Documento: Informe de Implementación Funcional del Sistema

## Metadatos
* **Nombre:** 01_informe_implementacion_funcional.md
* **Descripción:** Informe técnico y funcional de la implementación completa del sistema web de inventario para ferretería: módulos desarrollados, persistencia, reglas de negocio y seguridad RBAC.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Equipo Senior de Ingeniería de Software)
* **Versión:** 1.0
* **Estado:** Aprobado

---

## 1. Resumen Ejecutivo del Sistema Implementado
Se ha completado el desarrollo funcional integral del sistema de inventario para una ferretería de sede única, cubriendo el 100% de los requerimientos aprobados en las Etapas 1, 2 y 3.

El aplicativo opera bajo una arquitectura desacoplada en dos repositorios independientes:
* **Backend:** Java 21 LTS con Spring Boot 3.3.4, Spring Security (autenticación por sesiones web mediante cookies `JSESSIONID`), Spring Data JPA, Hibernate y MySQL 8.0.
* **Frontend:** Single Page Application (SPA) en React 18 con Vite, Material UI (MUI v5), Formik, Yup y Axios con interceptores CSRF.

---

## 2. Funcionalidades Implementadas por Módulo

### 2.1 Módulo: Autenticación, Usuarios y Permisos (RBAC)
* **Inicio y Cierre de Sesión:** Implementado en `AuthService` y `AuthController` (`POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `GET /api/v1/auth/me`).
* **Seguridad de Sesiones:** Sesión en servidor con cookie `JSESSIONID` (`HttpOnly`, `SameSite=Lax`), protección CSRF con `CookieCsrfTokenRepository.withHttpOnlyFalse()` e inyección en React.
* **Roles Autorizados:** `ROLE_ADMINISTRADOR` y `ROLE_USUARIO_OPERATIVO` aplicados con `@PreAuthorize` en controladores y validación en `ProtectedRoute` del frontend.
* **Aprovisionamiento Inicial Seguro:** `AdminInitializerRunner` inicializa al usuario `admin_ferreteria` con contraseña cifrada con BCrypt (coste 10) si la base de datos está limpia.

### 2.2 Módulo: Catálogo de Artículos y Referencias Comerciales
* **Normalización del Catálogo:** Artículos definidos mediante la combinación única de `Producto`, `Marca`, `Presentación` y `Unidad de Medida`.
* **Código de Barras Único:** Clave candidata única (`uk_ref_codigo_barras`) y búsqueda optimizada.
* **Borrado Lógico (RN-02):** `CatalogService.desactivarReferencia` valida la presencia de transacciones históricas en Kárdex. Si existen, cambia el estado a `INACTIVO` preservando la integridad referencial.
* **Auditoría de Precios (RN-09):** Endpoint `PATCH /api/v1/catalog/references/{id}/price` actualiza el precio vigente y genera un registro inmutable en `auditoria_log`.

### 2.3 Módulo: Terceros (Clientes y Proveedores Unificados - RN-10)
* **Identificación Fiscal Colombiana:** Soporte estricto para `CC`, `NIT`, `CE` y `PASAPORTE`.
* **Doble Rol Comercial:** La entidad `Tercero` dispone de banderas booleanas `es_cliente` y `es_proveedor`, permitiendo que una misma persona o empresa actúe en ambos roles comerciales sin duplicidad de datos.

### 2.4 Módulo: Núcleo de Inventario y Control de Concurrencia (RN-01, RN-04)
* **Saldo Atómico y Kárdex Inmutable:** Separación entre `inventario_saldo` (stock disponible en tiempo real) e `inventario_movimiento` (historial cronológico de solo inserción).
* **Bloqueo Pesimista (Anti-Stock Negativo):** Método `descontarStockConBloqueo` ejecuta `SELECT ... FOR UPDATE` en base de datos. Si `cantidadDisponible < cantidadSolicitada`, arroja `InsufficientStockException` y detiene la transacción, impidiendo saldos negativos ante ventas simultáneas.
* **Inventario Inicial:** Endpoint `POST /api/v1/inventory/initial` reservado a Administradores para registrar saldo y CPP inicial sin simular compras ficticias.
* **Ajustes Manuales Autorizados (RN-08):** `POST /api/v1/inventory/adjustments` exclusivo para Administradores, exigiendo motivo documentado obligatorio y auditoría.

### 2.5 Módulo: Compras Completas y Costo Promedio Ponderado (RN-03, RN-04)
* **Recepción Total en 1 Paso:** `PurchaseService.registrarCompra` recibe la totalidad de las líneas de producto en una única transacción atómica.
* **Cálculo Exacto de CPP:** Se actualiza automáticamente el costo promedio tras cada compra aplicando la fórmula:
  $$CPP_{nuevo} = \frac{(Stock_{actual} \times CPP_{actual}) + (Cantidad_{comprada} \times CostoUnitario_{compra})}{Stock_{actual} + Cantidad_{comprada}}$$
* **Cancelación Controlada:** Bloquea la anulación si las existencias actuales de cualquiera de los productos son inferiores a las unidades compradas (por haber sido consumidas posteriormente).

### 2.6 Módulo: Ventas de Contado y Cobro en Mostrador (RN-05, RN-06)
* **Cliente Obligatorio:** Requiere selección de un cliente activo (RN-05).
* **Pago en Efectivo:** Valida que el efectivo recibido cubra el total y calcula el cambio/vueltas (RN-06).
* **Costo Histórico Congelado:** Cada línea de venta persiste en la columna `costo_historico_unitario` el CPP exacto que tenía la referencia al momento del despacho, protegiendo los balances financieros ante compras futuras.

### 2.7 Módulo: Devoluciones de Clientes (RN-07)
* **Devolución Vinculada:** Comprueba la venta previa, valida que no se devuelvan más unidades de las vendidas y reincorpora el stock al costo histórico de la venta original.
* **Devolución No Vinculada:** Reingresa la mercancía al inventario disponible al Costo Promedio Ponderado vigente.

### 2.8 Módulo: Documentos, Consecutivos y Comprobantes Imprimibles (RN-12)
* **Consecutivos Correlativos:** `DocumentSequenceService` mantiene secuencias atómicas independientes con prefijos estándar (`VTA-`, `CMP-`, `DEV-`, `AJU-`).
* **Vista de Impresión Web:** Pantalla dedicada (`VoucherPrintPage.jsx`) optimizada con estilos `@media print` para emitir recibos y comprobantes de mostrador sin integración fiscal externa con la DIAN.

### 2.9 Módulo: Bitácora de Auditoría Básica (RN-11)
* **Registro Inmutable:** Captura obligatoria de: Usuario responsable, Fecha/hora exacta, Módulo, Acción y Descripción. Visible exclusivamente para el Administrador.
