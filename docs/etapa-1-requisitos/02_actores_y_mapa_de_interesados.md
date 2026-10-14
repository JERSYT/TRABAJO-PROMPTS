# Documento: Actores y Mapa de Interesados

## Metadatos
* **Nombre:** 02_actores_y_mapa_de_interesados.md
* **Descripción:** Identificación formal de interesados, usuarios directos del sistema y actores externos del negocio, especificando sus objetivos, responsabilidades y permisos preliminares de interacción.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Analista de Requisitos y Sistemas)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Clasificación General de Actores
De conformidad con las buenas prácticas de ingeniería de requisitos y las reglas de negocio confirmadas, se distingue claramente entre:
1. **Usuarios Directos:** Individuos con credenciales de acceso que interactúan directamente con la interfaz web y ejecutan peticiones hacia la API.
2. **Entidades Externas del Negocio:** Personas naturales o jurídicas cuyos datos y relaciones comerciales son registrados y administrados por los usuarios directos, sin contar con credenciales de acceso a la aplicación.

---

## 2. Mapa Detallado de Actores e Interesados

### 2.1 Propietario / Responsable del Negocio (Interesado Clave)
* **Naturaleza:** Interesado principal del proyecto.
* **Tipo de Interacción:** Indirecta / Directa (habitualmente asume el rol de Administrador en el sistema).
* **Objetivos:**
  * Maximizar la rentabilidad de la ferretería y evitar pérdidas por descuadres de mercancía.
  * Mantener información veraz y consolidada sobre costos, compras y ventas.
  * Disponer de trazabilidad y auditoría sobre operaciones sensibles como ajustes manuales de stock y cambios de precios.
* **Responsabilidades:**
  * Validar las políticas comerciales del negocio (márgenes de precio, políticas de devoluciones).
  * Autorizar ajustes de inventario extraordinarios y cancelaciones de operaciones.
* **Necesidades:**
  * Reportes fidedignos de existencias actuales y valoración financiera del inventario bajo costo promedio ponderado.

---

### 2.2 Administrador (Usuario Directo - Rol del Sistema: `ADMINISTRADOR`)
* **Naturaleza:** Usuario con privilegios de gestión total sobre la plataforma.
* **Tipo de Interacción:** Directa mediante credenciales autenticadas (sesión activa).
* **Objetivos:**
  * Garantizar la integridad operativa y la continuidad del sistema de inventario.
  * Supervisar las transacciones comerciales ejecutadas por los operadores.
* **Responsabilidades:**
  * Es el **único autorizado** para ejecutar y autorizar ajustes manuales de inventario (físico vs sistema) con motivo documentado.
  * Autorizar y ejecutar la cancelación de documentos comerciales (compras y ventas) cuando sea seguro revertir el inventario.
  * Gestionar el catálogo maestro de referencias vendibles, unidades y precios de venta.
  * Registrar la carga de inventario inicial.
  * Consultar los registros y bitácoras de auditoría de las operaciones del sistema.
  * Crear y gestionar las cuentas de usuarios del sistema (`ADMINISTRADOR` y `USUARIO_OPERATIVO`).
* **Relación con el Sistema:**
  * Acceso sin restricciones a todos los módulos funcionales, reportes y vistas de auditoría.

---

### 2.3 Usuario Operativo (Usuario Directo - Rol del Sistema: `USUARIO_OPERATIVO`)
* **Naturaleza:** Empleado de mostrador, cajero o encargado de bodega de la ferretería.
* **Tipo de Interacción:** Directa mediante credenciales autenticadas (sesión activa).
* **Objetivos:**
  * Registrar con rapidez y exactitud las operaciones comerciales cotidianas (ventas, compras, devoluciones).
  * Atender a los clientes en el mostrador sin generar retrasos o inconsistencias de stock.
* **Responsabilidades:**
  * Registrar ventas de contado en efectivo asociándolas obligatoriamente a un cliente registrado.
  * Registrar la recepción de compras de proveedores con sus cantidades y costos de compra.
  * Registrar y actualizar los datos de clientes y proveedores (terceros).
  * Registrar solicitudes de devolución de clientes para su reintegro al stock conforme a la política autorizada.
  * Modificar precios de venta de los productos únicamente si cuenta con autorización asignada para gestionar inventario.
* **Restricciones explícitas:**
  * **No puede** ejecutar ajustes manuales de inventario (reservados al Administrador).
  * **No puede** cancelar compras o ventas unilateralmente.
  * **No puede** alterar cuentas de usuario ni acceder a logs avanzados de auditoría del sistema.

---

### 2.4 Cliente (Entidad Externa)
* **Naturaleza:** Persona natural o jurídica que adquiere productos en la ferretería.
* **Tipo de Interacción:** Externa / Pasiva. Sus datos son capturados en el sistema por el personal de mostrador.
* **Objetivos:**
  * Recibir sus productos adquiridos y el comprobante de venta correspondiente con información clara del valor pagado en pesos colombianos (COP).
  * Realizar devoluciones justificadas de mercancía cuando aplique.
* **Responsabilidades:**
  * Proveer sus datos de identificación (tipo y número de documento: CC, NIT, etc., nombre/razón social, teléfono, dirección).
  * Pagar de contado en efectivo al momento de la venta.
* **Relación con el Sistema:**
  * Su registro es obligatorio para concretar cualquier venta (RN-05).
  * Puede desempeñar simultáneamente el rol de Proveedor sin necesidad de duplicar su registro como tercero (RN-10).

---

### 2.5 Proveedor (Entidad Externa)
* **Naturaleza:** Empresa distribuidora o persona que suministra artículos ferreteros para la venta.
* **Tipo de Interacción:** Externa / Pasiva.
* **Objetivos:**
  * Suministrar mercancía a la ferretería conforme a las órdenes pactadas.
* **Responsabilidades:**
  * Proporcionar documento de identificación fiscal (NIT o CC), razón comercial y datos de contacto.
  * Entregar el pedido completo en una sola entrega (conforme al alcance RN-03).
* **Relación con el Sistema:**
  * Vinculado de forma obligatoria a cada documento de compra registrado.
  * Sus costos suministrados actualizan el costo promedio ponderado de las referencias afectadas.

---

## 3. Matriz de Relación y Tipo de Interacción

| Actor | ¿Es Usuario del Sistema? | Tipo de Credencial | Nivel de Confianza | Módulos Principales de Interacción |
| :--- | :--- | :--- | :--- | :--- |
| **Propietario / Gerente** | Sí (como Admin) | Sesión Segura | Máximo | Auditoría, Costos, Reportes, Ajustes, Precios |
| **Administrador** | Sí | Sesión Segura | Alto | Gestión de Usuarios, Catálogo, Ajustes, Cancelaciones, Inventario Inicial |
| **Usuario Operativo** | Sí | Sesión Segura | Medio | Ventas, Compras, Devoluciones, Terceros, Consulta de Stock |
| **Cliente** | No (Entidad Externa) | N/A | N/A | Gestionado en Terceros y vinculado a Ventas y Devoluciones |
| **Proveedor** | No (Entidad Externa) | N/A | N/A | Gestionado en Terceros y vinculado a Compras |
