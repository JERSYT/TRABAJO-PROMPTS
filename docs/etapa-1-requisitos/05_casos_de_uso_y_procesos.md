# Documento: Casos de Uso y Procesos Principales

## Metadatos
* **Nombre:** 05_casos_de_uso_y_procesos.md
* **Descripción:** Especificación de casos de uso del sistema de inventario con flujos principales, alternativos, errores relevantes y postcondiciones conforme a las reglas de negocio confirmadas.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Analista de Procesos y Sistemas)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Índice de Casos de Uso

| Código | Nombre del Caso de Uso | Actor Principal | Módulo |
| :--- | :--- | :--- | :--- |
| **CU-01** | Registrar y Administrar Productos del Catálogo | Administrador / Usuario Operativo | Catálogo |
| **CU-02** | Consultar y Controlar Existencias de Inventario | Usuario Operativo / Administrador | Inventario |
| **CU-03** | Registrar Carga de Inventario Inicial | Administrador | Inventario |
| **CU-04** | Registrar Compra a Proveedor | Usuario Operativo / Administrador | Compras |
| **CU-05** | Registrar Venta de Contado en Mostrador | Usuario Operativo / Administrador | Ventas |
| **CU-06** | Gestionar Clientes y Proveedores (Terceros) | Usuario Operativo / Administrador | Terceros |
| **CU-07** | Registrar Devolución de Cliente (Vinculada / No Vinculada) | Usuario Operativo / Administrador | Devoluciones |
| **CU-08** | Realizar Ajuste de Inventario Autorizado | Administrador (Exclusivo) | Ajustes |
| **CU-09** | Modificar Precios de Venta del Catálogo | Administrador / Operativo autorizado | Catálogo |
| **CU-10** | Cancelar Documento Comercial (Compra o Venta) | Administrador (Exclusivo) | Ventas/Compras |
| **CU-11** | Consultar Auditoría y Trazabilidad Operativa | Administrador (Exclusivo) | Auditoría |
| **CU-12** | Generar e Imprimir Comprobantes Internos | Usuario Operativo / Administrador | Comprobantes |

---

## 2. Especificación Detallada de Casos de Uso

### CU-01: Registrar y Administrar Productos del Catálogo
* **Actor Principal:** Administrador / Usuario Operativo (con permisos de catálogo).
* **Precondiciones:** Usuario autenticado con permisos para gestionar productos.
* **Flujo Principal:**
  1. El usuario accede a la sección de Catálogo y pulsa "Nuevo Producto".
  2. Ingresa los datos: Nombre del producto, Marca, Presentación comercial, Unidad de medida base, Código de barras único y Precio de venta en COP.
  3. El sistema valida la unicidad del código de barras y que los datos sean coherentes.
  4. El sistema guarda la referencia con saldo inicial 0 y estado `ACTIVO`.
  5. El sistema confirma la creación y actualiza la lista del catálogo.
* **Flujos Alternativos:**
  * **2a. Desactivación de producto:** El usuario selecciona "Desactivar" sobre un producto existente. El sistema verifica si tiene historial. Si tiene historial, cambia su estado a `INACTIVO` (RN-02). Si no tiene ningún movimiento, confirma su eliminación.
* **Errores Relevantes:**
  * **E1: Código de barras duplicado:** El sistema notifica error 409 y solicita un código único.
  * **E2: Precio inválido:** Precio <= 0 o campo obligatorio vacío genera mensaje de validación.
* **Postcondiciones:** La referencia queda registrada y disponible para operaciones comerciales si está activa.

---

### CU-02: Consultar y Controlar Existencias de Inventario
* **Actor Principal:** Usuario Operativo / Administrador.
* **Precondiciones:** Usuario autenticado.
* **Flujo Principal:**
  1. El usuario ingresa a la vista de Inventario.
  2. El sistema lista las referencias con: código de barras, nombre comercial, unidad de medida, saldo disponible actual y costo promedio ponderado vigente (el CPP se muestra según visibilidad de rol).
  3. El usuario puede filtrar por nombre, marca o código de barras.
  4. El usuario puede seleccionar un producto para ver su Kárdex (historial de movimientos de entrada, salida y ajustes).
* **Flujos Alternativos:**
  * Si no hay productos que coincidan con la búsqueda, se muestra mensaje de "No se encontraron resultados".
* **Postcondiciones:** Se visualiza la información de stock sin alterar los datos.

---

### CU-03: Registrar Carga de Inventario Inicial
* **Actor Principal:** Administrador.
* **Precondiciones:** Usuario con rol `ADMINISTRADOR`. La referencia debe tener saldo en cero y sin movimientos previos.
* **Flujo Principal:**
  1. El Administrador selecciona la referencia para inicializar.
  2. Ingresa la cantidad inicial y el costo unitario de adquisición de referencia.
  3. El sistema valida que no existan movimientos previos (no se simulan compras ficticias - RN-04).
  4. El sistema genera un movimiento `INICIALIZACION`, establece el saldo inicial y fija el primer Costo Promedio Ponderado.
  5. Registra el evento en la auditoría con usuario, fecha y descripción.
* **Errores Relevantes:**
  * **E1:** Intento de inicializar un producto que ya tiene compras o ventas registradas. El sistema bloquea la acción.
* **Postcondiciones:** El producto cuenta con existencias y costo inicial listos para la venta.

---

### CU-04: Registrar Compra a Proveedor
* **Actor Principal:** Usuario Operativo / Administrador.
* **Precondiciones:** Proveedor registrado; referencias de catálogo registradas y activas.
* **Flujo Principal:**
  1. El usuario inicia el registro de compra y selecciona el Proveedor mediante búsqueda por NIT/CC.
  2. Añade una o más líneas de compra indicando: referencia de producto, cantidad adquirida y costo unitario de compra en COP.
  3. El sistema calcula subtotales por línea y el total general de la compra.
  4. El usuario confirma el registro y recepción del pedido.
  5. En una única transacción atómica (RN-03):
     * Se crea la cabecera del documento de compra con consecutivo único (`CMP-XXXXXX`) en estado `RECIBIDA`.
     * Se persisten las líneas de detalle.
     * Para cada línea, se suma la cantidad al saldo de inventario y se genera un movimiento de entrada.
     * Se recalcula el nuevo Costo Promedio Ponderado (CPP) de cada referencia (RN-04).
  6. El sistema emite el comprobante interno de compra.
* **Errores Relevantes:**
  * **E1: Cantidades o costos <= 0:** Validación rechaza la línea.
  * **E2: Falla de base de datos durante la transacción:** Se ejecuta rollback completo y ningún stock se modifica.
* **Postcondiciones:** Inventario actualizado y nuevo CPP calculado para los productos adquiridos.

---

### CU-05: Registrar Venta de Contado en Mostrador
* **Actor Principal:** Usuario Operativo / Administrador.
* **Precondiciones:** Cliente registrado; referencias activas y con stock suficiente; pago en efectivo.
* **Flujo Principal:**
  1. El usuario inicia la venta y selecciona obligatoriamente al Cliente (RN-05).
  2. Agrega una o varias líneas seleccionando el producto y la cantidad vendida.
  3. El sistema verifica en tiempo real que el stock disponible sea >= cantidad solicitada (RN-01).
  4. El sistema asigna el precio unitario vigente de venta y calcula el total de la venta en COP.
  5. El usuario confirma el cobro en efectivo (RN-06).
  6. En una única transacción atómica con bloqueo de concurrencia:
     * Se revalida que el stock disponible siga siendo suficiente.
     * Se crea el comprobante de venta (`VTA-XXXXXX`) en estado `COMPLETADA`.
     * Para cada línea se almacena: cantidad, precio unitario de venta y el **costo histórico vigente** en ese momento.
     * Se descuenta la cantidad vendida de las existencias.
     * Se crea el movimiento de salida en el Kárdex.
  7. El sistema genera el comprobante de venta imprimible en pantalla.
* **Flujos Alternativos / Errores:**
  * **E1: Stock insuficiente:** El sistema rechaza la venta, informa qué producto no tiene existencias suficientes y aborta la transacción sin descontar unidades de ninguna línea.
  * **E2: Venta sin cliente:** El sistema bloquea el avance hasta seleccionar o crear un cliente válido.
* **Postcondiciones:** Existencias descontadas, comprobante generado e histórico de costos preservado.

---

### CU-06: Gestionar Clientes y Proveedores (Terceros)
* **Actor Principal:** Usuario Operativo / Administrador.
* **Precondiciones:** Usuario autenticado.
* **Flujo Principal:**
  1. El usuario accede al módulo de Terceros.
  2. Ingresa: Tipo de documento (CC, NIT, CE), Número de documento único, Nombre o Razón Social, Teléfono, Correo, Dirección y Roles comerciales (Marca "Es Cliente", "Es Proveedor" o ambos - RN-10).
  3. El sistema valida formato y unicidad del número de documento.
  4. Se guarda el tercero y queda habilitado de inmediato para ventas y/o compras.
* **Flujos Alternativos:**
  * Si el tercero ya existe, el usuario puede editar sus datos de contacto o habilitarle el rol comercial que le falte (ej. convertir un cliente en proveedor).
* **Postcondiciones:** Tercero disponible para transacciones comerciales.

---

### CU-07: Registrar Devolución de Cliente
* **Actor Principal:** Usuario Operativo / Administrador.
* **Precondiciones:** Producto activo; motivo de devolución ingresado.
* **Flujo Principal (Devolución Vinculada):**
  1. El usuario busca la venta original por su consecutivo (`VTA-XXXXXX`).
  2. Selecciona la referencia y la cantidad a devolver (validando que no supere la cantidad vendida previamente).
  3. El usuario indica el motivo de la devolución.
  4. Al confirmar:
     * Se genera el documento de devolución (`DEV-XXXXXX`).
     * Las unidades aceptadas se suman automáticamente al inventario disponible (RN-07).
     * Se genera un movimiento de entrada valorado al **costo unitario histórico** de la venta original.
* **Flujo Alternativo (Devolución No Vinculada):**
  1. Si no existe venta registrada, el usuario registra la devolución seleccionando directamente el producto y la cantidad.
  2. La mercancía reingresa al inventario disponible valorada al **Costo Promedio Ponderado vigente**.
* **Postcondiciones:** Stock repuesto en almacén y movimiento Kárdex documentado.

---

### CU-08: Realizar Ajuste de Inventario Autorizado
* **Actor Principal:** Administrador (Exclusivo - RN-08).
* **Precondiciones:** Usuario autenticado con rol `ADMINISTRADOR`.
* **Flujo Principal:**
  1. El Administrador selecciona la referencia a ajustar.
  2. Selecciona el tipo de ajuste: `INCREMENTO` (por sobrante) o `DISMINUCIÓN` (por merma, rotura o pérdida).
  3. Ingresa la cantidad del ajuste y redacta obligatoriamente el motivo / justificación detallada.
  4. El sistema valida que en caso de disminución, el stock resultante no sea menor que cero.
  5. Se actualiza el saldo del producto, se registra el movimiento de ajuste en el Kárdex y se emite el comprobante de ajuste (`AJU-XXXXXX`).
  6. Se crea un registro en la tabla de auditoría con la justificación y usuario.
* **Errores Relevantes:**
  * **E1: Intento por Usuario Operativo:** El backend responde HTTP 403 Forbidden.
  * **E2: Motivo vacío:** El sistema no permite procesar el ajuste sin justificación.
  * **E3: Disminución mayor al stock existente:** Se rechaza la operación para preservar RN-01.
* **Postcondiciones:** Stock corregido con justificación formal archivada en auditoría.

---

### CU-09: Modificar Precios de Venta del Catálogo
* **Actor Principal:** Administrador / Usuario Operativo (con permiso de inventario).
* **Precondiciones:** Usuario autenticado con permisos autorizados (RN-09).
* **Flujo Principal:**
  1. El usuario selecciona un producto del catálogo y accede a "Editar Precio".
  2. Ingresa el nuevo precio de venta en COP.
  3. El sistema valida que sea un valor mayor a cero.
  4. Se actualiza el precio vigente para ventas futuras.
  5. Se registra en la bitácora de auditoría: usuario, fecha, hora y descripción ("Cambio de precio de referencia X").
* **Postcondiciones:** El nuevo precio aplica en mostrador; ventas pasadas permanecen intactas.

---

### CU-10: Cancelar Documento Comercial (Compra o Venta)
* **Actor Principal:** Administrador (Exclusivo).
* **Precondiciones:** Usuario autenticado con rol `ADMINISTRADOR`.
* **Flujo Principal (Cancelación de Venta):**
  1. El Administrador selecciona una venta previa y pulsa "Cancelar Operación".
  2. Ingresa el motivo de anulación.
  3. El sistema verifica que la venta no tenga devoluciones asociadas.
  4. El documento pasa a estado `CANCELADO`.
  5. Se generan movimientos compensatorios de entrada en el Kárdex para reintegrar las existencias al almacén.
  6. Se audita la cancelación.
* **Flujo Alternativo (Cancelación de Compra):**
  1. El Administrador cancela una compra. El sistema verifica si el stock actual de los productos cubre la salida de las cantidades compradas. Si hay stock suficiente, se descuentan y se pasa a `CANCELADO`. Si no hay stock porque ya se vendió, se bloquea la operación.
* **Postcondiciones:** Operación anulada con trazabilidad inmutable sin borrado físico.

---

### CU-11: Consultar Auditoría y Trazabilidad Operativa
* **Actor Principal:** Administrador (Exclusivo).
* **Precondiciones:** Usuario con rol `ADMINISTRADOR`.
* **Flujo Principal:**
  1. El Administrador accede al panel de Auditoría.
  2. El sistema muestra la lista cronológica de eventos: fecha, hora, usuario, acción y descripción.
  3. Permite filtrar por rango de fechas, usuario o módulo.
* **Postcondiciones:** Consulta de bitácora sin posibilidad de modificación o eliminación de registros.

---

### CU-12: Generar e Imprimir Comprobantes Internos
* **Actor Principal:** Usuario Operativo / Administrador.
* **Precondiciones:** Operación comercial (venta, compra, devolución o ajuste) completada.
* **Flujo Principal:**
  1. Al finalizar la operación o desde el historial de documentos, el usuario pulsa "Imprimir Comprobante".
  2. El sistema despliega una vista limpia con formato de factura de mostrador: consecutivo, datos de la ferretería, fecha, cliente/proveedor, líneas detalladas, subtotales y total en COP.
  3. Se habilita el diálogo nativo de impresión del navegador.
* **Postcondiciones:** Documento físico o PDF generado por el navegador sin impacto en la base de datos.
