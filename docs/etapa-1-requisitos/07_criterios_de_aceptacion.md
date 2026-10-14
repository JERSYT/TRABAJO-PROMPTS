# Documento: Criterios de Aceptación del Sistema

## Metadatos
* **Nombre:** 07_criterios_de_aceptacion.md
* **Descripción:** Criterios de aceptación objetivos formulados en formato Dado / Cuando / Entonces (Given / When / Then) para los flujos críticos del sistema de inventario.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Analista de Calidad y Requisitos)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Escenarios de Aceptación para Flujos Críticos

### Escenario 1: Intentar vender más unidades de las disponibles (Stock Negativo)
* **Identificador:** CA-SC-001
* **Requisito / Regla:** RF-INV-004, RN-01
* **Dado que** existe una referencia de producto "Martillo de Uña 16oz" con un saldo disponible actual de **3 unidades**,
* **Y que** un usuario operativo se encuentra en la pantalla de registro de venta,
* **Cuando** el usuario intenta registrar una venta solicitando **4 unidades** de dicha referencia,
* **Entonces** el sistema rechaza la operación,
* **Y** muestra un mensaje de error claro indicando "Existencias insuficientes para el producto seleccionado (Disponibles: 3, Solicitadas: 4)",
* **Y** no se descuenta ninguna unidad del inventario, manteniéndose el saldo en 3 unidades,
* **Y** no se genera ningún comprobante de venta ni movimiento en el Kárdex.

---

### Escenario 2: Registrar una venta válida de contado
* **Identificador:** CA-SC-002
* **Requisito / Regla:** RF-VEN-001, RN-01, RN-05, RN-06
* **Dado que** existe una referencia "Pintura Esmalte Blanco 1 Galón" con saldo disponible de **10 unidades** y precio de venta de **$45.000 COP**,
* **Y que** existe un cliente registrado con CC "1098765432",
* **Cuando** el usuario operativo registra una venta de **2 unidades** para dicho cliente pagando en efectivo un total de **$90.000 COP**,
* **Entonces** la venta se completa exitosamente en estado `COMPLETADA`,
* **Y** el saldo disponible de la referencia disminuye atómicamente a **8 unidades**,
* **Y** se genera un movimiento de salida en el Kárdex asociado a la referencia,
* **Y** se emite un comprobante interno con consecutivo correlativo (ej. `VTA-000105`),
* **Y** en la línea de la venta queda congelado el costo histórico del producto al momento de la transacción.

---

### Escenario 3: Intentar una operación sin los permisos requeridos (Control de Roles)
* **Identificador:** CA-SC-003
* **Requisito / Regla:** RF-SEG-002, RN-08
* **Dado que** ha iniciado sesión un usuario con el rol `USUARIO_OPERATIVO`,
* **Cuando** dicho usuario envía una solicitud HTTP para ejecutar un ajuste manual de inventario o para crear un nuevo usuario administrativo,
* **Entonces** el backend intercepta la solicitud y responde con código HTTP **403 Forbidden**,
* **Y** no se ejecuta ninguna modificación sobre la base de datos,
* **Y** el frontend despliega un aviso indicando "No cuenta con los permisos necesarios para realizar esta acción".

---

### Escenario 4: Registrar una compra con múltiples líneas y recepción completa
* **Identificador:** CA-SC-004
* **Requisito / Regla:** RF-COM-001, RN-03
* **Dado que** existe un proveedor activo con NIT "900123456-1",
* **Y que** existen los productos "Alicate Universal" (saldo 5, CPP $12.000) y "Destornillador Pala" (saldo 10, CPP $6.000),
* **Cuando** el usuario registra una orden de compra para dicho proveedor con:
  * Línea 1: 5 Alicates a costo unitario de $14.000 COP,
  * Línea 2: 10 Destornilladores a costo unitario de $7.000 COP,
* **Entonces** en una única transacción atómica:
  * La compra queda registrada en estado `RECIBIDA` con consecutivo único (`CMP-000042`),
  * El stock de Alicates pasa a ser exactamente 10 unidades,
  * El stock de Destornilladores pasa a ser exactamente 20 unidades,
  * Se crean los respectivos movimientos de entrada en el Kárdex,
  * Se recalculan los nuevos costos promedio ponderados de ambas referencias.

---

### Escenario 5: Cálculo del Costo Promedio Ponderado (CPP) tras compra
* **Identificador:** CA-SC-005
* **Requisito / Regla:** RF-COM-002, RN-04
* **Dado que** la referencia "Cinta Aislante 20m" tiene un saldo actual de **10 unidades** con un CPP unitario vigente de **$2.000 COP** (Valor total en inventario: $20.000),
* **Cuando** se registra una compra de **10 unidades** adicionales a un costo unitario de compra de **$3.000 COP** (Valor compra: $30.000),
* **Entonces** el nuevo saldo en existencias es de **20 unidades**,
* **Y** el nuevo CPP unitario se actualiza automáticamente a exactamente **$2.500 COP** ($\frac{\$20.000 + \$30.000}{20} = \$2.500$),
* **Y** este nuevo costo promedio regirá para la valoración del inventario a partir de este instante.

---

### Escenario 6: Desactivar un producto con historial (Borrado Lógico)
* **Identificador:** CA-SC-006
* **Requisito / Regla:** RF-CAT-002, RN-02
* **Dado que** la referencia "Tornillo Ensamble 2 pulg" posee 15 ventas y 2 compras registradas en el pasado,
* **Cuando** el Administrador solicita la eliminación de dicho producto en el catálogo,
* **Entonces** el sistema no ejecuta un `DELETE` físico en la base de datos,
* **Y** actualiza el atributo de estado a `INACTIVO`,
* **Y** la referencia deja de aparecer en los selectores de búsqueda de nuevas compras y ventas,
* **Y** todos los comprobantes históricos donde figura el tornillo continúan mostrándolo sin errores de datos.

---

### Escenario 7: Registrar una devolución y reincorporar existencias
* **Identificador:** CA-SC-007
* **Requisito / Regla:** RF-DEV-001, RN-07
* **Dado que** existe una venta `VTA-000080` donde se vendieron **5 unidades** de "Disco de Corte 4.5 pulg" a un precio de $8.000 COP (con costo histórico registrado de $4.500 COP),
* **Cuando** el cliente solicita la devolución de **2 unidades** y el usuario confirma el registro de la devolución vinculada a dicho comprobante,
* **Entonces** el sistema valida que 2 unidades <= 5 unidades vendidas,
* **Y** genera el comprobante de devolución `DEV-000012`,
* **Y** las **2 unidades** se reincorporan de inmediato al saldo disponible de existencias en el inventario,
* **Y** se genera un movimiento de Kárdex de tipo `DEVOLUCION_CLIENTE` valorado al costo histórico de **$4.500 COP**.

---

### Escenario 8: Ejecutar un ajuste autorizado y rechazar uno no autorizado
* **Identificador:** CA-SC-008
* **Requisito / Regla:** RF-AJU-001, RN-08
* **Dado que** existen 12 unidades de "Tubo PVC 1/2 pulg" y se detecta 1 tubo quebrado durante el conteo físico,
* **Caso 8A (Autorizado):**
  * **Cuando** el `ADMINISTRADOR` registra un ajuste de disminución de **1 unidad** con el motivo "Tubo roto en bodega durante acomodación de mercancía",
  * **Entonces** el saldo pasa a 11 unidades, se registra el movimiento de ajuste con su justificación y se crea un registro de auditoría.
* **Caso 8B (No autorizado):**
  * **Cuando** un `USUARIO_OPERATIVO` intenta registrar el mismo ajuste por API o interfaz,
  * **Entonces** el sistema bloquea la acción, retorna HTTP 403 y las existencias se mantienen inalteradas.

---

### Escenario 9: Modificar un precio y conservar su registro de auditoría
* **Identificador:** CA-SC-009
* **Requisito / Regla:** RF-CAT-003, RN-09, RN-11
* **Dado que** la referencia "Cerradura de Sobreponer" tiene precio de venta de **$60.000 COP**,
* **Cuando** un usuario autorizado actualiza el precio a **$65.000 COP**,
* **Entonces** el catálogo actualiza el precio vigente para nuevas ventas a $65.000 COP,
* **Y** se crea un registro inmutable en la tabla de auditoría con el ID del usuario, fecha/hora exacta y detalle de la modificación,
* **Y** las ventas anteriores a este momento siguen mostrando el precio facturado original de $60.000 COP.

---

### Escenario 10: Generar y consultar un comprobante interno
* **Identificador:** CA-SC-010
* **Requisito / Regla:** RF-DOC-001, RF-DOC-002, RN-12
* **Dado que** una venta de mostrador acaba de ser confirmada exitosamente,
* **Cuando** el usuario solicita visualizar o imprimir el comprobante,
* **Entonces** el sistema presenta el documento estructurado con numeración interna consecutiva, encabezado de ferretería, fecha/hora, datos del cliente, listado de productos, subtotales y total en COP,
* **Y** permite la impresión sin generar dependencias de conexión externa con la DIAN ni certificar el documento como factura fiscal autorizada.
