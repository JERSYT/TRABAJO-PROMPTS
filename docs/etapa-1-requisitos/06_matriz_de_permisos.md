# Documento: Matriz Preliminar de Permisos y Control de Acceso

## Metadatos
* **Nombre:** 06_matriz_de_permisos.md
* **Descripción:** Matriz de control de acceso basada en roles (RBAC) para el Administrador y el Usuario Operativo, especificando operaciones permitidas, denegadas y pendientes de confirmación.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Analista de Seguridad y Requisitos)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Principio Fundamental de Seguridad
Ocultar o deshabilitar un botón en la interfaz gráfica del frontend **no constituye por sí solo un control de seguridad**. 

El backend Spring Boot es la **única autoridad definitiva** para verificar la autenticación y la autorización. Cualquier petición HTTP enviada directamente a un endpoint restringido por parte de un usuario sin el rol o permiso adecuado debe ser rechazada inexorablemente con un código de respuesta **HTTP 403 Forbidden** (o **HTTP 401 Unauthorized** si no hay sesión activa).

---

## 2. Definición de Roles Autorizados
1. **`ROLE_ADMINISTRADOR`:** Rol con control total del sistema, responsable de la supervisión global, ajustes extraordinarios de inventario, cancelaciones, altas de usuarios y auditoría.
2. **`ROLE_USUARIO_OPERATIVO`:** Rol para el personal de mostrador y bodega, enfocado en el registro ágil de operaciones diarias (ventas de contado, compras completas, clientes, proveedores y devoluciones).

---

## 3. Matriz Detallada de Permisos por Módulo y Operación

| Módulo | Operación Concreta | Rol: Administrador | Rol: Usuario Operativo | Estado de Decisión | Riesgo de Seguridad / Justificación |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **Autenticación** | Iniciar sesión / Cerrar sesión | **Permitido** | **Permitido** | Confirmado | Acceso estándar con sesión segura. |
| **Usuarios** | Crear, editar y desactivar usuarios | **Permitido** | **Denegado** | Confirmado | Riesgo de escalamiento de privilegios si el operativo gestiona usuarios. |
| **Catálogo** | Consultar productos y referencias | **Permitido** | **Permitido** | Confirmado | Consulta indispensable para venta y mostrador. |
| **Catálogo** | Crear nuevos productos en catálogo | **Permitido** | **Permitido (Condicionado)** | Pendiente de Validación | Si el operativo crea productos sin supervisión, puede duplicar referencias en catálogo. |
| **Catálogo** | Desactivar / Borrar referencias | **Permitido** | **Denegado** | Confirmado | Evita borrado accidental de referencias activas en venta. |
| **Precios** | Modificar precios de venta | **Permitido** | **Permitido (Si autorizado)** | Confirmado (RN-09) | Requiere auditoría obligatoria de quién modificó el precio de venta. |
| **Inventario** | Consultar saldos y existencias | **Permitido** | **Permitido** | Confirmado | Operativo necesita conocer disponibilidad para vender. |
| **Inventario** | Consultar Kárdex y Costo Promedio | **Permitido** | **Denegado** | Pendiente de Validación | El personal de ventas no siempre debe conocer el margen y costo de compra exacto del producto. |
| **Inventario** | Registrar inventario inicial | **Permitido** | **Denegado** | Confirmado (RN-08) | Operación administrativa de arranque que afecta balances de stock y costos. |
| **Ajustes** | Ejecutar ajustes manuales de stock | **Permitido** | **Denegado** | Confirmado (RN-08) | **Riesgo crítico de fraude/hurto:** Si el operativo ajusta stock, puede ocultar pérdidas de mercancía. |
| **Compras** | Registrar y recibir compra completa | **Permitido** | **Permitido** | Confirmado (RN-03) | Recepción de mercancía en bodega/mostrador. |
| **Compras** | Cancelar compra confirmada | **Permitido** | **Denegado** | Confirmado (RN-08) | Anular compras afecta el Kárdex y el Costo Promedio Ponderado. |
| **Ventas** | Registrar venta de contado | **Permitido** | **Permitido** | Confirmado (RN-05/06)| Flujo principal de mostrador. |
| **Ventas** | Cancelar venta confirmada | **Permitido** | **Denegado** | Confirmado (RN-08) | Previene anulación fraudulenta de ventas con cobro en efectivo recibido. |
| **Terceros** | Registrar y editar Clientes/Proveedores| **Permitido** | **Permitido** | Confirmado (RN-10) | Agilidad para crear clientes nuevos durante la venta. |
| **Devoluciones** | Registrar devolución de cliente | **Permitido** | **Permitido** | Confirmado (RN-07) | Atención al cliente en mostrador con reincorporación a stock. |
| **Comprobantes** | Generar e imprimir comprobantes | **Permitido** | **Permitido** | Confirmado (RN-12) | Entrega de recibo impreso al cliente. |
| **Auditoría** | Consultar bitácora de auditoría | **Permitido** | **Denegado** | Confirmado (RN-11) | Confidencialidad y control interno de eventos. |

---

## 4. Análisis de Permisos Ambiguos y Riesgos Asociados

1. **Visibilidad del Costo Unitario de Compra y CPP para el Usuario Operativo:**
   * *Riesgo:* Exponer el costo de compra a todos los empleados de mostrador puede filtrar márgenes comerciales confidenciales de la ferretería.
   * *Propuesta:* Ocultar el campo de costo unitario y costo promedio ponderado en las vistas operativas de catálogo e inventario para el rol `USUARIO_OPERATIVO`, mostrándolo únicamente al `ADMINISTRADOR`.
2. **Creación de nuevos productos por parte del Usuario Operativo:**
   * *Riesgo:* Desorden en la catalogación (nombres mal escritos, unidades erróneas, duplicidad de referencias vendibles).
   * *Propuesta:* Habilitar la creación rápida de productos en mostrador bajo un flag de usuario o restringirla exclusivamente al Administrador para mantener orden estricto del catálogo.
