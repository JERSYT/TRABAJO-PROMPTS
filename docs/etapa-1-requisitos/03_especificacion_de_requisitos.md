# Documento: Especificación de Requisitos de Software (SRS)

## Metadatos
* **Nombre:** 03_especificacion_de_requisitos.md
* **Descripción:** Especificación formal de requisitos funcionales y no funcionales estructurados conforme a ISO/IEC/IEEE 29148:2018 e ISO/IEC 25010:2023 para el sistema de inventario de ferretería.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Ingeniero de Requisitos)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Introducción y Convenciones
Los requisitos definidos en este documento poseen identificadores estables y trazables con el siguiente formato:
* **RF-[MOD]-[NUM]:** Requisito Funcional.
* **RNF-[CAT]-[NUM]:** Requisito No Funcional (Calidad de Software).

**Categorías de módulos:**
* `CAT`: Catálogo y Productos
* `INV`: Inventario y Movimientos
* `COM`: Compras y Costos
* `VEN`: Ventas y Facturación
* `DEV`: Devoluciones
* `AJU`: Ajustes de Inventario
* `TER`: Terceros (Clientes y Proveedores)
* `DOC`: Documentos y Consecutivos
* `SEG`: Seguridad y Auditoría

---

## 2. Requisitos Funcionales por Módulo

### 2.1 Módulo: Catálogo y Referencias Comerciales (`CAT`)

#### **RF-CAT-001: Registro de referencias comerciales únicas**
* **Descripción:** El sistema debe permitir registrar una nueva referencia vendible en el catálogo mediante la combinación obligatoria de Producto (ej. "Tornillo Drywall"), Marca (ej. "Herragro") y Presentación (ej. "Caja x 100 unidades"), asociándole una unidad de medida base, un código de barras único y un precio de venta inicial en pesos colombianos (COP).
* **Actores:** `ADMINISTRADOR`, `USUARIO_OPERATIVO` (si cuenta con permiso de inventario).
* **Precondiciones:** El usuario está autenticado y posee permisos para gestionar inventario.
* **Flujo Principal:**
  1. El usuario ingresa nombre del producto, marca, presentación comercial y selecciona la unidad base.
  2. El usuario introduce el código de barras y el precio de venta sugerido.
  3. El sistema valida que el código de barras no exista en ninguna otra referencia activa o inactiva.
  4. El sistema almacena la referencia con estado inicial "ACTIVO" y saldo inicial en cero.
* **Flujos Alternativos / Errores:**
  * Si el código de barras ya está registrado, el sistema rechaza la operación e indica error de duplicidad (HTTP 409 Conflict).
  * Si los campos obligatorios están incompletos o el precio es negativo o cero, el sistema rechaza la creación (HTTP 400 Bad Request).
* **Criterio de Aceptación:** La referencia queda guardada en la base de datos con identificador único y estado ACTIVO.
* **Trazabilidad:** RN-02, RN-09, RN-10.

#### **RF-CAT-002: Desactivación lógica de productos con historial**
* **Descripción:** El sistema debe impedir la eliminación física de cualquier producto o referencia que posea movimientos de inventario, compras, ventas o ajustes asociados. En su lugar, el sistema ejecutará un borrado lógico (desactivación).
* **Actores:** `ADMINISTRADOR`.
* **Precondiciones:** El producto existe en el catálogo.
* **Flujo Principal:**
  1. El usuario solicita desactivar una referencia existente.
  2. El sistema verifica si existen compras, ventas o movimientos asociados.
  3. Si existen registros asociados, el sistema cambia el estado a "INACTIVO", impidiendo su selección en nuevas compras y ventas, pero conservando íntegro su historial y consultas anteriores.
* **Flujos Alternativos / Errores:**
  * Si la referencia no tiene absolutamente ningún movimiento histórico registrado, el sistema permite la eliminación física bajo confirmación explícita.
* **Criterio de Aceptación:** Los productos inactivos no aparecen en las listas de selección para ventas ni compras nuevas, pero sus registros históricos y comprobantes antiguos permanecen consultables.
* **Trazabilidad:** RN-02.

#### **RF-CAT-003: Modificación y auditoría de precios de venta**
* **Descripción:** El sistema debe permitir modificar el precio de venta de una referencia activa, registrando obligatoriamente un evento de auditoría con el usuario que ejecutó el cambio, fecha, hora y descripción.
* **Actores:** `ADMINISTRADOR`, `USUARIO_OPERATIVO` (autorizado para inventario).
* **Criterio de Aceptación:** El nuevo precio aplica para ventas posteriores y el evento queda asentado en la bitácora de auditoría. Las ventas pasadas no modifican su valor facturado.
* **Trazabilidad:** RN-09, RN-11.

---

### 2.2 Módulo: Núcleo de Inventario y Movimientos (`INV`)

#### **RF-INV-001: Consulta de existencias y saldos en tiempo real**
* **Descripción:** El sistema debe mostrar el saldo actual disponible de cada referencia de producto, su costo promedio ponderado vigente y su estado.
* **Actores:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **Criterio de Aceptación:** El saldo mostrado coincide exactamente con el valor consolidado del balance de movimientos.
* **Trazabilidad:** RN-01, RN-04.

#### **RF-INV-002: Registro persistente e inmutable de movimientos (Kárdex)**
* **Descripción:** Cada operación que incremente o disminuya el stock (inicialización, compra, venta, devolución, ajuste, anulación) debe crear un registro inmutable de movimiento de inventario indicando: tipo de movimiento, referencia, cantidad, costo unitario de la transacción, saldo resultante, documento de origen y fecha/hora.
* **Actores:** Sistema (automático tras operaciones comerciales y ajustes).
* **Criterio de Aceptación:** Ningún movimiento de inventario puede ser editado o eliminado de la base de datos una vez creado.
* **Trazabilidad:** RN-01, RN-04, RN-11.

#### **RF-INV-003: Inicialización controlada de existencias**
* **Descripción:** El sistema debe permitir registrar el inventario inicial de una referencia mediante un movimiento explícito de tipo `INICIALIZACION`, capturando cantidad inicial y costo unitario de referencia, sin crear órdenes de compra ficticias.
* **Actores:** `ADMINISTRADOR`.
* **Criterio de Aceptación:** Se asienta el stock y el costo promedio inicial de la referencia; solo puede ejecutarse cuando la referencia tiene saldo cero y no registra operaciones previas.
* **Trazabilidad:** RN-04, RN-08.

#### **RF-INV-004: Control atómico contra existencias negativas en concurrencia**
* **Descripción:** El sistema debe garantizar que ninguna transacción comercial pueda completarse si la cantidad solicitada excede el saldo de existencias disponible en el momento exacto de la confirmación, incluso bajo operaciones de venta simultáneas.
* **Actores:** Sistema (`ADMINISTRADOR`, `USUARIO_OPERATIVO`).
* **Criterio de Aceptación:** Ante dos ventas concurrentes que compiten por el mismo inventario limitado, una se procesa con éxito y la otra es rechazada con mensaje de stock insuficiente sin permitir saldos negativos.
* **Trazabilidad:** RN-01.

---

### 2.3 Módulo: Compras y Costos (`COM`)

#### **RF-COM-001: Registro y recepción completa de compras**
* **Descripción:** El sistema debe registrar órdenes de compra a proveedores con múltiples líneas de producto (referencia, cantidad comprada, costo unitario de adquisición), completando la recepción física de todas las líneas en un único paso transaccional (no se admiten recepciones parciales).
* **Actores:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **Precondiciones:** El proveedor y los productos deben estar previamente registrados y activos.
* **Criterio de Aceptación:** Al confirmar la compra, se incrementa el stock de cada producto, se generan los movimientos de entrada respectivos y se calcula el nuevo Costo Promedio Ponderado.
* **Trazabilidad:** RN-03, RN-04, RN-10.

#### **RF-COM-002: Cálculo automático del Costo Promedio Ponderado (CPP)**
* **Descripción:** Tras registrar una compra, el sistema debe actualizar el costo promedio ponderado de cada referencia afectada aplicando la fórmula matemática:
  $$CPP_{nuevo} = \frac{(Stock_{actual} \times CPP_{actual}) + (Cantidad_{comprada} \times CostoUnitario_{compra})}{Stock_{actual} + Cantidad_{comprada}}$$
* **Actores:** Sistema (motor de inventario).
* **Criterio de Aceptación:** El cálculo se ejecuta con precisión decimal fija (escala 2 o 4) sin utilizar aritmética de punto flotante inexacta.
* **Trazabilidad:** RN-04.

#### **RF-COM-003: Cancelación controlada de compras**
* **Descripción:** El Administrador puede cancelar una compra registrada especificando un motivo obligatorio, siempre y cuando las existencias actuales de las referencias compradas sean iguales o superiores a las cantidades recibidas.
* **Actores:** `ADMINISTRADOR`.
* **Flujos Alternativos / Errores:**
  * Si parte del stock de los productos de la compra ya fue vendido o consumido y las existencias actuales no alcanzan para cubrir la reversión, el sistema bloquea la cancelación e informa el conflicto.
* **Criterio de Aceptación:** La cancelación no borra el registro original: genera movimientos compensatorios de salida y actualiza el estado del documento a `CANCELADO`.
* **Trazabilidad:** RN-03, RN-08, RN-11.

---

### 2.4 Módulo: Ventas de Contado y Pagos (`VEN`)

#### **RF-VEN-001: Registro de ventas de contado con cliente obligatorio**
* **Descripción:** El sistema debe permitir registrar una venta de mostrador compuesta por una o múltiples líneas de productos, vinculada obligatoriamente a un cliente previamente registrado en el sistema, y pagada de contado en efectivo.
* **Actores:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **Precondiciones:** Cliente registrado, productos activos, stock suficiente para cada línea.
* **Flujo Principal:**
  1. El usuario selecciona el cliente (búsqueda por documento CC/NIT o nombre).
  2. El usuario agrega líneas seleccionando referencia, cantidad y confirmando el precio unitario vigente.
  3. El sistema valida el stock disponible para cada ítem en tiempo real.
  4. El sistema calcula subtotales y total a pagar en COP.
  5. El usuario confirma el pago en efectivo.
  6. El sistema crea la cabecera de la venta, las líneas con su precio de venta y su **costo histórico vigente al momento de la venta**, descuenta las cantidades del saldo y genera los movimientos de salida.
* **Flujos Alternativos / Errores:**
  * Si no se especifica un cliente, el sistema rechaza la venta (RN-05).
  * Si la cantidad supera las existencias, se rechaza la venta indicando la referencia sin stock (RN-01).
* **Criterio de Aceptación:** La venta queda registrada en estado `COMPLETADA`, las existencias se descuentan atómicamente y se genera un comprobante interno con consecutivo único.
* **Trazabilidad:** RN-01, RN-05, RN-06, RN-12.

#### **RF-VEN-002: Cancelación controlada de ventas**
* **Descripción:** El Administrador puede cancelar una venta completada especificando un motivo documentado, reintegrando las cantidades vendidas al inventario disponible mediante movimientos compensatorios de entrada.
* **Actores:** `ADMINISTRADOR`.
* **Precondición:** La venta no debe presentar devoluciones asociadas registradas previamente (si tiene devoluciones, la operación queda bloqueada hasta resolución).
* **Criterio de Aceptación:** El comprobante de venta pasa a estado `CANCELADO` y las existencias vuelven a estar disponibles.
* **Trazabilidad:** RN-01, RN-08, RN-11.

---

### 2.5 Módulo: Devoluciones de Clientes (`DEV`)

#### **RF-DEV-001: Registro de devoluciones vinculadas a una venta**
* **Descripción:** El sistema debe permitir registrar una devolución de mercancía asociada a una venta original. Las cantidades devueltas no pueden exceder las cantidades vendidas en dicho documento.
* **Actores:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **Criterio de Aceptación:** Las cantidades aceptadas se reincorporan automáticamente al saldo de inventario disponible y se valoran internamente utilizando el **costo unitario histórico** registrado en la venta original.
* **Trazabilidad:** RN-04, RN-07.

#### **RF-DEV-002: Registro de devoluciones no vinculadas**
* **Descripción:** El sistema debe permitir registrar devoluciones directas sin requerir documento de venta previo, con validación flexible por parte del usuario responsable.
* **Actores:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **Criterio de Aceptación:** Las cantidades aceptadas ingresan al stock disponible valoradas al **costo promedio ponderado vigente** de la referencia.
* **Trazabilidad:** RN-04, RN-07.

---

### 2.6 Módulo: Ajustes de Inventario (`AJU`)

#### **RF-AJU-001: Ejecución de ajustes manuales autorizados**
* **Descripción:** El sistema debe permitir realizar ajustes de inventario (positivos o negativos por mermas, conteos físicos o discrepancias) exclusivamente al usuario Administrador, exigiendo un motivo documentado (justificación).
* **Actores:** `ADMINISTRADOR` (exclusivo).
* **Precondiciones:** Usuario autenticado con rol `ADMINISTRADOR`.
* **Flujos Alternativos / Errores:**
  * Si un `USUARIO_OPERATIVO` intenta ejecutar un ajuste, el backend rechaza la petición con código HTTP 403 Forbidden.
  * Si no se proporciona un motivo de texto válido, la operación es rechazada con HTTP 400.
* **Criterio de Aceptación:** Se actualiza el saldo disponible, se genera un movimiento de tipo `AJUSTE_ENTRADA` o `AJUSTE_SALIDA` y se registra el evento en auditoría.
* **Trazabilidad:** RN-08, RN-11.

---

### 2.7 Módulo: Terceros - Clientes y Proveedores (`TER`)

#### **RF-TER-001: Gestión unificada de personas naturales y jurídicas**
* **Descripción:** El sistema debe permitir registrar, consultar y actualizar personas naturales y jurídicas mediante su tipo de documento (CC, NIT, CE, etc.), número de documento único, nombre/razón social, dirección, teléfono y correo electrónico.
* **Actores:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **Criterio de Aceptación:** El sistema permite marcar si un tercero opera como Cliente, Proveedor o Ambos, impidiendo la duplicidad de registros para la misma cédula o NIT.
* **Trazabilidad:** RN-05, RN-10.

---

### 2.8 Módulo: Documentos, Consecutivos y Comprobantes (`DOC`)

#### **RF-DOC-001: Generación y consulta de consecutivos internos**
* **Descripción:** El sistema debe mantener numeraciones consecutivas independientes y correlativas para comprobantes de Venta (ej. `VTA-000001`), Compras (`CMP-000001`), Devoluciones (`DEV-000001`) y Ajustes (`AJU-000001`).
* **Criterio de Aceptación:** Ningún comprobante puede duplicar consecutivo; la numeración es secuencial e ininterrumpida.
* **Trazabilidad:** RN-12.

#### **RF-DOC-002: Emisión y vista imprimible de comprobantes simulados**
* **Descripción:** El sistema debe presentar una vista de comprobante estructurado en pesos colombianos con datos del negocio, cliente/proveedor, líneas, totales y simulación de factura interna, con formato apto para impresión web directa.
* **Criterio de Aceptación:** El comprobante no contiene integración fiscal DIAN real pero contiene todos los campos necesarios para identificación de la operación en mostrador.
* **Trazabilidad:** RN-12.

---

### 2.9 Módulo: Seguridad y Auditoría (`SEG`)

#### **RF-SEG-001: Autenticación por sesión segura**
* **Descripción:** El sistema debe autenticar usuarios mediante credenciales (nombre de usuario y contraseña cifrada con BCrypt), generando sesiones web administradas por Spring Security respaldadas por cookies `HttpOnly` y `SameSite`.
* **Criterio de Aceptación:** No se emplean tokens JWT; las solicitudes no autenticadas a recursos protegidos retornan HTTP 401 Unauthorized.
* **Trazabilidad:** RN-11.

#### **RF-SEG-002: Control de acceso basado en roles (RBAC)**
* **Descripción:** El sistema debe autorizar o denegar cada endpoint de la API según el rol del usuario autenticado (`ADMINISTRADOR` o `USUARIO_OPERATIVO`). Ocultar botones en frontend es solo ayuda visual; la validación definitiva la ejecuta el backend.
* **Criterio de Aceptación:** Todo intento de acceso no autorizado devuelve HTTP 403 Forbidden.

#### **RF-SEG-003: Bitácora básica de auditoría**
* **Descripción:** El sistema debe registrar en una tabla de auditoría los eventos clave (modificación de precios, ajustes de stock, cancelaciones, altas de usuarios), capturando: identificador de usuario responsable, fecha y hora del sistema, y descripción textual de la operación.
* **Criterio de Aceptación:** El registro de auditoría es inmutable y consultable únicamente por el Administrador.
* **Trazabilidad:** RN-11.

---

## 3. Requisitos No Funcionales (Calidad de Software - ISO/IEC 25010:2023)

### 3.1 Adecuación Funcional
* **RNF-AF-001 (Completitud funcional):** El sistema debe cubrir el 100% de las operaciones aprobadas de inventario, compras, ventas, catálogo, devoluciones y ajustes de una ferretería de sede única.
* **RNF-AF-002 (Corrección en cálculos):** Los cálculos de totales en COP y costo promedio ponderado deben redondearse a 2 decimales para totales finales y hasta 4 decimales en costos unitarios promedio para minimizar el error por redondeo acumulativo.

### 3.2 Fiabilidad e Integridad Transaccional
* **RNF-FIA-001 (Atomicidad transaccional):** Toda operación compuesta (ej. venta con múltiples líneas y movimientos asociados) debe ejecutarse bajo una transacción atómica `@Transactional`. Si alguna línea o comprobante falla, se ejecuta rollback total garantizando que no existan estados parciales en la base de datos.
* **RNF-FIA-002 (Prevención de condiciones de carrera):** El sistema debe soportar transacciones concurrentes asegurando que dos usuarios intentando vender las últimas unidades disponibles no produzcan saldo negativo ni lecturas inconsistentes (nivel de aislamiento Serializable o Bloqueo Pesimista en JPA).

### 3.3 Eficiencia del Desempeño
* **RNF-DES-001 (Tiempo de respuesta de API):** Para una carga de demostración académica (hasta 20 usuarios concurrentes), el 95% de las solicitudes REST habituales (búsqueda de producto, registro de venta) deben responder en menos de 500 ms en entorno local.
* **RNF-DES-002 (Paginación en listas):** Las consultas de catálogo, movimientos y comprobantes deben admitir parámetros de paginación (`page`, `size`) para evitar transferencias de datos excesivas.

### 3.4 Seguridad
* **RNF-SEG-001 (Protección de credenciales):** Las contraseñas no se almacenarán en texto plano bajo ninguna circunstancia. Se utilizará el algoritmo BCrypt con factor de costo mínimo 10.
* **RNF-SEG-002 (Protección CSRF y CORS):** La aplicación debe incluir protección obligatoria contra falsificación de solicitudes en sitios cruzados (CSRF) mediante tokens `XSRF-TOKEN` y restringir CORS a los orígenes autorizados del frontend.
* **RNF-SEG-003 (Manejo de errores RFC 9457):** Las respuestas de error del backend deben usar el estándar `ProblemDetail` sin exponer trazas de pila (stack traces), nombres de tablas internas ni secretos en la respuesta al cliente.

### 3.5 Mantenibilidad y Compatibilidad
* **RNF-MAN-001 (Arquitectura por capas):** La base de código backend debe organizarse estrictamente en capas `Controller` → `Service` → `Repository`, garantizando desacoplamiento y facilidad de pruebas unitarias con JUnit 5 y Mockito.
* **RNF-COM-001 (Compatibilidad del frontend):** La interfaz construida en React + Material UI debe operar de forma óptima en navegadores modernos basados en Chromium, Firefox y WebKit.
