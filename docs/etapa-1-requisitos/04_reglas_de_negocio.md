# Documento: Reglas de Negocio del Sistema

## Metadatos
* **Nombre:** 04_reglas_de_negocio.md
* **Descripción:** Catálogo formal de reglas de negocio confirmadas, propuestas de mejora y decisiones pendientes para el sistema de inventario de ferretería.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Analista de Negocio y Requisitos)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Reglas de Negocio Confirmadas (Decisiones Aprobadas)

### RN-01. Integridad del Inventario y Prevención de Stock Negativo
* **Identificador:** RN-01
* **Descripción:** No se permitirá completar ninguna venta ni salida de mercancía cuya cantidad solicitada supere las existencias físicas disponibles en el inventario. La solución técnica debe preservar esta regla incluso ante transacciones simultáneas o concurrentes sobre el mismo producto. En ningún escenario el saldo de una referencia podrá ser menor que cero.
* **Justificación:** Previene ventas de mostrador de productos inexistentes, descalces contables y fricciones con los clientes por falta de producto físico.
* **Requisitos Relacionados:** RF-INV-001, RF-INV-004, RF-VEN-001, RNF-FIA-002.
* **Criterio de Verificación:** 
  1. Si un producto tiene saldo 5 e ingresa una solicitud de venta por 6 unidades, el sistema debe rechazar la operación inmediatamente.
  2. Si dos transacciones concurrentes intentan vender simultáneamente 3 y 3 unidades del mismo producto con saldo 5, exactamente una transacción debe completarse y la otra debe ser rechazada con error de stock insuficiente, quedando el saldo final en 2 unidades (nunca -1).

---

### RN-02. Protección de Productos con Historial (Desactivación Lógica)
* **Identificador:** RN-02
* **Descripción:** Los productos o referencias del catálogo que tengan operaciones históricas asociadas (movimientos de inventario, compras, ventas, devoluciones o ajustes) no podrán ser eliminados físicamente de la base de datos. En su lugar, se procederá a su desactivación lógica, conservando intacto su historial, trazabilidad y referencias desde documentos antiguos.
* **Justificación:** Garantiza la integridad referencial histórica y el cumplimiento de auditorías contables; evita errores de clave foránea rota y orfandad de registros.
* **Requisitos Relacionados:** RF-CAT-001, RF-CAT-002, RF-DOC-002.
* **Criterio de Verificación:** Al solicitar la eliminación de un producto con historial, el sistema debe cambiar su estado a `INACTIVO` e impedir su selección en nuevas compras y ventas, manteniendo visibles sus transacciones anteriores en reportes y comprobantes pasados.

---

### RN-03. Recepción Completa de Compras
* **Identificador:** RN-03
* **Descripción:** Toda orden de compra registrada en el sistema se recibirá completa y en su totalidad en una única operación transaccional. La recepción parcial o fraccionada de compras queda expresamente fuera del alcance.
* **Justificación:** Simplifica el flujo operativo de una micro o pequeña ferretería de sede única y reduce la complejidad innecesaria en la conciliación de pedidos pendientes.
* **Requisitos Relacionados:** RF-COM-001, RF-COM-002, RF-INV-002.
* **Criterio de Verificación:** Al guardar un documento de compra, todas sus líneas actualizan de forma inmediata y automática el stock disponible de las referencias involucradas en un único paso transaccional.

---

### RN-04. Método de Valoración del Inventario (Costo Promedio Ponderado)
* **Identificador:** RN-04
* **Descripción:** El inventario se valorará exclusivamente mediante el método de **Costo Promedio Ponderado (CPP)**. El costo se actualiza con cada compra registrada. En las ventas, las líneas deben congelar y almacenar el costo unitario histórico que tenía el producto al momento exacto de la venta. En las devoluciones vinculadas a una venta, la mercancía reingresa valorada con dicho costo histórico; en devoluciones no vinculadas, reingresa al costo promedio ponderado vigente al momento de la devolución.
* **Justificación:** Proporciona una valoración contable y financiera precisa sin las complejidades de gestión de lotes o métodos PEPS/UEPS en un negocio de mostrador.
* **Requisitos Relacionados:** RF-INV-002, RF-COM-002, RF-VEN-001, RF-DEV-001, RF-DEV-002.
* **Criterio de Verificación:** Al comprar 10 unidades a $1.000 teniendo un saldo previo de 10 unidades a $2.000, el nuevo CPP unitario debe calcularse exactamente como:
  $$\frac{(10 \times 2000) + (10 \times 1000)}{20} = \frac{30000}{20} = \$1.500$$

---

### RN-05. Asociación Obligatoria de Cliente en Ventas
* **Identificador:** RN-05
* **Descripción:** Toda venta registrada en el sistema debe estar asociada obligatoriamente a un cliente previamente registrado en la base de datos de terceros. No se permiten ventas anónimas o sin cliente.
* **Justificación:** Garantiza la trazabilidad comercial de clientes, facilita devoluciones futuras y asegura el cumplimiento básico de control administrativo.
* **Requisitos Relacionados:** RF-VEN-001, RF-TER-001, RF-DOC-002.
* **Criterio de Verificación:** Intentar registrar una venta sin `clienteId` o con un identificador inexistente debe disparar un error de validación (HTTP 400) y rechazar la persistencia de la transacción.

---

### RN-06. Medio de Pago Exclusivo en Efectivo
* **Identificador:** RN-06
* **Descripción:** Las ventas se procesan exclusivamente bajo la modalidad de contado con pago en efectivo. Queda expresamente excluida cualquier modalidad de venta a crédito o pagos diferidos.
* **Justificación:** Mantiene acotado el alcance a la operatividad esencial de una ferretería pequeña, evitando la complejidad de carteras de crédito y cobranzas.
* **Requisitos Relacionados:** RF-VEN-001, RF-DOC-001.
* **Criterio de Verificación:** El documento de venta registra el monto total pagado en efectivo; no existen campos de fecha de vencimiento de crédito ni abonos parciales.

---

### RN-07. Gestión y Reincorporación de Devoluciones de Clientes
* **Identificador:** RN-07
* **Descripción:** Las devoluciones de clientes serán validadas de forma flexible por el usuario responsable. No es obligatorio que toda devolución esté vinculada a una venta previa, pero las aceptadas se reincorporarán automáticamente al inventario disponible. Cuando la devolución esté vinculada a una venta, no se podrá devolver una cantidad superior a la vendida en ese documento.
* **Justificación:** Brinda flexibilidad en mostrador para mercancía estándar de ferretería respetando la trazabilidad del stock físico.
* **Requisitos Relacionados:** RF-DEV-001, RF-DEV-002, RF-INV-002.
* **Criterio de Verificación:** Al registrar una devolución aprobada de 2 unidades de un producto, el saldo en inventario se incrementa inmediatamente en 2 unidades y se genera el movimiento compensatorio en el Kárdex.

---

### RN-08. Autorización Exclusiva de Ajustes de Inventario por el Administrador
* **Identificador:** RN-08
* **Descripción:** Únicamente los usuarios con rol `ADMINISTRADOR` están autorizados para ejecutar y aprobar ajustes manuales de inventario (ingreso o egreso por discrepancias, daños o mermas). Todo ajuste debe contener obligatoriamente un motivo documentado y conservar su historial inmutable.
* **Justificación:** Previene manipulaciones no supervisadas del stock y hurtos internos por parte de personal operativo.
* **Requisitos Relacionados:** RF-AJU-001, RF-SEG-002, RF-INV-002.
* **Criterio de Verificación:** Cualquier solicitud de ajuste enviada por un usuario con rol `USUARIO_OPERATIVO` debe ser denegada por el backend con HTTP 403 Forbidden. Ajustes sin texto en el motivo deben retornar HTTP 400 Bad Request.

---

### RN-09. Modificación de Precios de Venta y su Auditoría
* **Identificador:** RN-09
* **Descripción:** El precio de venta puede ser modificado por usuarios autorizados (Administrador u Operativo con permiso de inventario). Toda modificación debe quedar registrada en la bitácora de auditoría. La modificación no afectará retroactivamente las ventas históricas ya cerradas.
* **Justificación:** Permite actualizar precios ante inflación o fluctuaciones de mercado sin distorsionar los balances históricos de facturación.
* **Requisitos Relacionados:** RF-CAT-003, RF-SEG-003.
* **Criterio de Verificación:** Al actualizar el precio de venta de $5.000 a $5.500, las ventas anteriores conservan el valor de $5.000 y se inserta un registro en la tabla de auditoría con el usuario responsable.

---

### RN-10. Unificación de Terceros (Clientes y Proveedores)
* **Identificador:** RN-10
* **Descripción:** Una misma persona natural o jurídica puede actuar tanto como cliente como proveedor en el sistema sin necesidad de duplicar su registro en la base de datos.
* **Justificación:** Optimiza el almacenamiento, evita duplicidad de datos fiscales (NIT/CC) y simplifica la gestión de terceros comerciales.
* **Requisitos Relacionados:** RF-TER-001, RF-COM-001, RF-VEN-001.
* **Criterio de Verificación:** Un tercero con un mismo documento de identidad puede ser seleccionado como proveedor en una compra y como cliente en una venta sin inconsistencias.

---

### RN-11. Alcance de la Auditoría Básica
* **Identificador:** RN-11
* **Descripción:** La auditoría básica del sistema registrará exclusivamente: Usuario responsable, Fecha y hora del sistema, y Descripción clara de la operación realizada. No se consideran obligatorios el almacenamiento de direcciones IP, coordenadas geográficas ni capturas fotográficas.
* **Justificación:** Evita sobrecarga de almacenamiento y complejidad innecesaria en una demostración académica, cumpliendo a cabalidad con la trazabilidad funcional solicitada.
* **Requisitos Relacionados:** RF-SEG-003, RF-AJU-001, RF-CAT-003.
* **Criterio de Verificación:** La tabla de auditoría almacena los campos `id`, `usuario_id`, `fecha_hora`, `accion`, `modulo`, `descripcion`.

---

### RN-12. Consecutivos Internos y Simulación de Facturación
* **Identificador:** RN-12
* **Descripción:** Los comprobantes comerciales (Ventas, Compras, Devoluciones, Ajustes) utilizarán numeraciones consecutivas internas independientes y correlativas. La facturación electrónica es una simulación visual y documental de control interno, sin integración técnica ni fiscal con la DIAN.
* **Justificación:** Permite ordenar los comprobantes internamente respetando el alcance presupuestal y tecnológico sin crear responsabilidades fiscales ficticias.
* **Requisitos Relacionados:** RF-DOC-001, RF-DOC-002.
* **Criterio de Verificación:** Cada venta genera un consecutivo secuencial único (ej. `VTA-000001`, `VTA-000002`) sin saltos y con formato visual para impresión.

---

## 2. Propuestas de Mejora Técnicas (No Obligatorias)
* **PM-01 (Auditoría de valores anterior/nuevo en cambios de precio):** Almacenar en la descripción de auditoría el precio anterior y el precio nuevo para facilitar revisiones comparativas.
* **PM-02 (Alerta visual de stock mínimo):** Parametrizar un umbral de stock mínimo en la referencia para destacar en la interfaz los artículos próximos a agotarse.

---

## 3. Decisiones Pendientes (Para Validación del Negocio)
1. **Política ante cancelación de venta con devoluciones previas:** Si una venta ya tiene una devolución parcial asociada, ¿se debe bloquear completamente la opción de cancelar la venta original hasta anular la devolución, o se permite cancelar descontando las unidades ya devueltas? *(Propuesta recomendada: Bloquear la cancelación de la venta mientras existan devoluciones asociadas).*
2. **Precisión decimal en unidades fraccionarias:** Para productos que se venden por metros, kilogramos o litros, ¿se fija una escala estándar de 2 decimales (ej. 1.50 metros)? *(Propuesta recomendada: Soportar hasta 3 decimales en cantidades físicas y 2 decimales en moneda COP).*
