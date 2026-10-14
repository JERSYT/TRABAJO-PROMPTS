# Documento: Diccionario de Datos MySQL 8.0

## Metadatos
* **Nombre:** 03_diccionario_datos_mysql.md
* **Descripción:** Diccionario de datos exhaustivo para MySQL 8.0, especificando tablas, columnas, tipos de datos SQL, restricciones (PK, FK, UK, CHECK), índices y justificaciones de diseño.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Especialista en Bases de Datos y MySQL)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Convenciones y Estándares de Base de Datos
* **Nomenclatura:** Tablas y columnas en minúsculas con guiones bajos (`snake_case`).
* **Representación Monetaria (COP):** `DECIMAL(15, 2)` con redondeo estándar a 2 decimales. Queda **prohibido el uso de FLOAT o DOUBLE**.
* **Representación de Cantidades Físicas:** `DECIMAL(12, 3)` para admitir unidades enteras y fracciones de hasta 3 decimales (metros, kilos, galones).
* **Representación de Costo Promedio (CPP):** `DECIMAL(15, 4)` para preservar precisión matemática y evitar acumulación de errores de redondeo.
* **Marcas temporales:** `DATETIME(6)` para alta precisión en auditoría y Kárdex.
* **Motor:** `InnoDB` con juego de caracteres `utf8mb4` y cotejamiento `utf8mb4_unicode_ci`.

---

## 2. Tablas del Módulo: Identidad y Seguridad

### 2.1 Tabla: `usuario`
* **Responsabilidad:** Credenciales y estado de las cuentas de usuario con acceso al sistema web.
* **Motor:** InnoDB.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador primario subrogado. |
| `username` | `VARCHAR(50)` | NO | **UK** `uk_usuario_username` | Nombre de usuario único para login. |
| `password_hash` | `VARCHAR(100)` | NO | | Hash seguro generado con BCrypt. |
| `nombre_completo`| `VARCHAR(120)` | NO | | Nombre y apellidos del usuario. |
| `email` | `VARCHAR(100)` | SÍ | | Correo electrónico de contacto. |
| `activo` | `BOOLEAN` | NO | DEFAULT `TRUE` | Estado de la cuenta (borrado lógico). |
| `fecha_creacion` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Fecha y hora de alta. |
| `fecha_actualizacion` | `DATETIME(6)` | SÍ | | Fecha y hora del último cambio. |

* **Índices:** `idx_usuario_username` (B-Tree sobre `username`).

---

### 2.2 Tabla: `rol`
* **Responsabilidad:** Catálogo de roles de autorización reconocidos por Spring Security.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del rol. |
| `nombre` | `VARCHAR(40)` | NO | **UK** `uk_rol_nombre` | Código del rol: `ROLE_ADMINISTRADOR`, `ROLE_USUARIO_OPERATIVO`. |
| `descripcion` | `VARCHAR(150)` | SÍ | | Descripción funcional del rol. |

---

### 2.3 Tabla: `usuario_rol`
* **Responsabilidad:** Tabla asociativa muchos a muchos entre usuarios y roles.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `usuario_id` | `BIGINT` | NO | **PK**, **FK** `usuario(id)` ON DELETE CASCADE | ID del usuario. |
| `rol_id` | `BIGINT` | NO | **PK**, **FK** `rol(id)` ON DELETE RESTRICT | ID del rol asignado. |

---

## 3. Tablas del Módulo: Catálogo y Terceros

### 3.1 Tabla: `tercero`
* **Responsabilidad:** Información unificada de clientes y proveedores (personas naturales o jurídicas).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del tercero. |
| `tipo_documento` | `VARCHAR(10)` | NO | CHECK (`tipo_documento` IN ('CC','NIT','CE','PASAPORTE')) | Tipo de documento fiscal colombiano. |
| `numero_documento`| `VARCHAR(30)` | NO | **UK** `uk_tercero_documento` | Número de documento fiscal único. |
| `razon_social` | `VARCHAR(150)` | NO | | Nombre comercial o nombre de la persona. |
| `tipo_persona` | `VARCHAR(15)` | NO | CHECK (`tipo_persona` IN ('NATURAL','JURIDICA')) | Distinción fiscal. |
| `telefono` | `VARCHAR(30)` | SÍ | | Teléfono o celular de contacto. |
| `email` | `VARCHAR(100)` | SÍ | | Correo de facturación / contacto. |
| `direccion` | `VARCHAR(150)` | SÍ | | Dirección física. |
| `es_cliente` | `BOOLEAN` | NO | DEFAULT `TRUE` | Bandera que habilita rol comercial cliente. |
| `es_proveedor` | `BOOLEAN` | NO | DEFAULT `FALSE` | Bandera que habilita rol comercial proveedor. |
| `activo` | `BOOLEAN` | NO | DEFAULT `TRUE` | Borrado lógico. |

* **Índices:** `idx_tercero_documento` (`numero_documento`), `idx_tercero_razon_social` (`razon_social`).

---

### 3.2 Tabla: `producto`
* **Responsabilidad:** Concepto genérico del artículo (ej. Martillo de bola, Esmalte sintético).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del producto. |
| `nombre` | `VARCHAR(100)` | NO | | Nombre genérico del artículo. |
| `descripcion` | `VARCHAR(255)` | SÍ | | Detalle técnico del producto. |
| `activo` | `BOOLEAN` | NO | DEFAULT `TRUE` | Estado lógico. |

---

### 3.3 Tabla: `marca`
* **Responsabilidad:** Fabricante o marca comercial (ej. Herragro, Stanley, Pintuco).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la marca. |
| `nombre` | `VARCHAR(80)` | NO | **UK** `uk_marca_nombre` | Nombre de la marca. |
| `activo` | `BOOLEAN` | NO | DEFAULT `TRUE` | Estado lógico. |

---

### 3.4 Tabla: `presentacion`
* **Responsabilidad:** Empaque comercial (ej. Caja x 100, Galón, Unidad, Rollo 50m).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la presentación. |
| `nombre` | `VARCHAR(80)` | NO | **UK** `uk_presentacion_nombre` | Nombre de la presentación comercial. |
| `activo` | `BOOLEAN` | NO | DEFAULT `TRUE` | Estado lógico. |

---

### 3.5 Tabla: `unidad_medida`
* **Responsabilidad:** Unidades de magnitud física estándar para el control de inventario.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador. |
| `codigo` | `VARCHAR(10)` | NO | **UK** `uk_unidad_codigo` | Código estándar (UND, MTR, KGM, GLN). |
| `nombre` | `VARCHAR(50)` | NO | | Nombre legible (Unidad, Metro, Kilo). |

---

### 3.6 Tabla: `referencia`
* **Responsabilidad:** Renglón vendible definitivo en el catálogo (Producto + Marca + Presentación).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la referencia vendible. |
| `producto_id` | `BIGINT` | NO | **FK** `producto(id)` ON DELETE RESTRICT | Producto base. |
| `marca_id` | `BIGINT` | NO | **FK** `marca(id)` ON DELETE RESTRICT | Marca fabricante. |
| `presentacion_id`| `BIGINT` | NO | **FK** `presentacion(id)` ON DELETE RESTRICT | Presentación comercial. |
| `unidad_medida_id`| `BIGINT` | NO | **FK** `unidad_medida(id)` ON DELETE RESTRICT| Unidad base. |
| `codigo_barras` | `VARCHAR(50)` | NO | **UK** `uk_ref_codigo_barras` | Código de barras único para venta/búsqueda. |
| `precio_venta` | `DECIMAL(15, 2)`| NO | CHECK (`precio_venta` >= 0) | Precio unitario al público en COP. |
| `activo` | `BOOLEAN` | NO | DEFAULT `TRUE` | Borrado lógico (RN-02). |
| `fecha_creacion` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Fecha de alta en catálogo. |

* **Restricción de tupla:** `UK` `uk_ref_prod_marca_pres` (`producto_id`, `marca_id`, `presentacion_id`).
* **Índices:** `idx_ref_codigo_barras`, `idx_ref_producto`.

---

## 4. Tablas del Módulo: Inventario y Movimientos (Kárdex)

### 4.1 Tabla: `inventario_saldo`
* **Responsabilidad:** Registro atómico del stock disponible y costo promedio actual por referencia.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del registro de saldo. |
| `referencia_id` | `BIGINT` | NO | **UK**, **FK** `referencia(id)` ON DELETE RESTRICT | Referencia a la que pertenece el stock. |
| `cantidad_disponible`| `DECIMAL(12, 3)`| NO | DEFAULT 0.000, CHECK (`cantidad_disponible` >= 0) | Saldo físico actual en almacén (RN-01). |
| `costo_promedio_ponderado`| `DECIMAL(15, 4)`| NO | DEFAULT 0.0000, CHECK (`costo_promedio_ponderado` >= 0) | Costo de valoración vigente (RN-04). |
| `fecha_actualizacion`| `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` ON UPDATE `CURRENT_TIMESTAMP(6)` | Última modificación de stock. |
| `version` | `BIGINT` | NO | DEFAULT 0 | Campo de versión para bloqueo optimista JPA. |

---

### 4.2 Tabla: `inventario_movimiento`
* **Responsabilidad:** Historial cronológico inmutable (Kárdex) de cada transacción física.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del movimiento. |
| `referencia_id` | `BIGINT` | NO | **FK** `referencia(id)` ON DELETE RESTRICT | Referencia afectada. |
| `tipo_movimiento`| `VARCHAR(30)` | NO | CHECK (`tipo_movimiento` IN ('INICIALIZACION','COMPRA','VENTA','DEVOLUCION_CLIENTE','AJUSTE_ENTRADA','AJUSTE_SALIDA','CANCELACION_VENTA','CANCELACION_COMPRA')) | Clasificación del flujo. |
| `cantidad` | `DECIMAL(12, 3)`| NO | CHECK (`cantidad` > 0) | Magnitud física movida. |
| `costo_unitario_movimiento`| `DECIMAL(15, 4)`| NO | CHECK (`costo_unitario_movimiento` >= 0) | Costo unitario aplicado en la operación. |
| `saldo_resultante`| `DECIMAL(12, 3)`| NO | CHECK (`saldo_resultante` >= 0) | Saldo en almacén tras el movimiento. |
| `documento_tipo` | `VARCHAR(20)` | NO | | `VENTA`, `COMPRA`, `DEVOLUCION`, `AJUSTE`. |
| `documento_id` | `BIGINT` | NO | | ID del documento comercial origen. |
| `motivo` | `VARCHAR(255)` | SÍ | | Justificación (obligatorio para ajustes). |
| `fecha_hora` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Momento exacto del registro. |
| `usuario_id` | `BIGINT` | NO | **FK** `usuario(id)` ON DELETE RESTRICT | Usuario responsable de la transacción. |

* **Índices:** `idx_mov_ref_fecha` (`referencia_id`, `fecha_hora`), `idx_mov_doc` (`documento_tipo`, `documento_id`).

---

## 5. Tablas de Operaciones Comerciales

### 5.1 Tabla: `compra`
* **Responsabilidad:** Cabecera de compra de mercancía a proveedores.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la compra. |
| `consecutivo` | `VARCHAR(25)` | NO | **UK** `uk_compra_consecutivo` | Consecutivo correlativo (ej. `CMP-000001`). |
| `proveedor_id` | `BIGINT` | NO | **FK** `tercero(id)` ON DELETE RESTRICT | Proveedor registrado (RN-10). |
| `fecha_hora` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Fecha y hora de recepción. |
| `total` | `DECIMAL(15, 2)`| NO | CHECK (`total` >= 0) | Total liquidado de la compra en COP. |
| `estado` | `VARCHAR(20)` | NO | CHECK (`estado` IN ('RECIBIDA','CANCELADA')) | Estado del documento (RN-03). |
| `motivo_cancelacion`| `VARCHAR(255)`| SÍ | | Motivo documentado si se anula. |
| `usuario_id` | `BIGINT` | NO | **FK** `usuario(id)` ON DELETE RESTRICT | Usuario que registró la compra. |

---

### 5.2 Tabla: `compra_linea`
* **Responsabilidad:** Detalle por renglón de artículos en la compra.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la línea. |
| `compra_id` | `BIGINT` | NO | **FK** `compra(id)` ON DELETE CASCADE | Compra a la que pertenece. |
| `referencia_id` | `BIGINT` | NO | **FK** `referencia(id)` ON DELETE RESTRICT | Producto adquirido. |
| `cantidad` | `DECIMAL(12, 3)`| NO | CHECK (`cantidad` > 0) | Unidades recibidas. |
| `costo_unitario`| `DECIMAL(15, 4)`| NO | CHECK (`costo_unitario` >= 0) | Costo de adquisición unitario. |
| `subtotal` | `DECIMAL(15, 2)`| NO | CHECK (`subtotal` >= 0) | Total de la línea en COP. |

---

### 5.3 Tabla: `venta`
* **Responsabilidad:** Cabecera de venta de mostrador de contado.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la venta. |
| `consecutivo` | `VARCHAR(25)` | NO | **UK** `uk_venta_consecutivo` | Consecutivo correlativo (ej. `VTA-000001`). |
| `cliente_id` | `BIGINT` | NO | **FK** `tercero(id)` ON DELETE RESTRICT | Cliente registrado obligatorio (RN-05). |
| `fecha_hora` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Fecha y hora de expedición. |
| `subtotal` | `DECIMAL(15, 2)`| NO | CHECK (`subtotal` >= 0) | Subtotal en COP. |
| `total` | `DECIMAL(15, 2)`| NO | CHECK (`total` >= 0) | Total a pagar en COP. |
| `monto_pagado_efectivo`| `DECIMAL(15, 2)`| NO | CHECK (`monto_pagado_efectivo` >= `total`) | Monto entregado en efectivo (RN-06). |
| `cambio_devuelto`| `DECIMAL(15, 2)`| NO | DEFAULT 0.00 | Cambio entregado al cliente. |
| `estado` | `VARCHAR(20)` | NO | CHECK (`estado` IN ('COMPLETADA','CANCELADA')) | Estado de la venta. |
| `motivo_cancelacion`| `VARCHAR(255)`| SÍ | | Justificación en caso de cancelación. |
| `usuario_id` | `BIGINT` | NO | **FK** `usuario(id)` ON DELETE RESTRICT | Vendedor o cajero responsable. |

---

### 5.4 Tabla: `venta_linea`
* **Responsabilidad:** Renglón de productos vendidos, congelando precio y costo histórico.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del ítem vendido. |
| `venta_id` | `BIGINT` | NO | **FK** `venta(id)` ON DELETE CASCADE | Venta asociada. |
| `referencia_id` | `BIGINT` | NO | **FK** `referencia(id)` ON DELETE RESTRICT | Producto despachado. |
| `cantidad` | `DECIMAL(12, 3)`| NO | CHECK (`cantidad` > 0) | Cantidad despachada. |
| `precio_unitario`| `DECIMAL(15, 2)`| NO | CHECK (`precio_unitario` >= 0) | Precio unitario facturado en COP. |
| `costo_historico_unitario`| `DECIMAL(15, 4)`| NO | CHECK (`costo_historico_unitario` >= 0) | **Costo promedio vigente al momento de la venta** (RN-04). |
| `subtotal` | `DECIMAL(15, 2)`| NO | CHECK (`subtotal` >= 0) | `cantidad * precio_unitario`. |

---

### 5.5 Tabla: `devolucion`
* **Responsabilidad:** Cabecera de devolución de cliente (vinculada o no vinculada).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la devolución. |
| `consecutivo` | `VARCHAR(25)` | NO | **UK** `uk_dev_consecutivo` | Consecutivo correlativo (ej. `DEV-000001`). |
| `venta_id` | `BIGINT` | SÍ | **FK** `venta(id)` ON DELETE RESTRICT | Venta de origen (nulo si no es vinculada). |
| `cliente_id` | `BIGINT` | SÍ | **FK** `tercero(id)` ON DELETE RESTRICT | Cliente que devuelve. |
| `tipo_devolucion`| `VARCHAR(20)` | NO | CHECK (`tipo_devolucion` IN ('VINCULADA','NO_VINCULADA')) | Modalidad de la devolución (RN-07). |
| `fecha_hora` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Fecha y hora de recepción. |
| `motivo` | `VARCHAR(255)` | NO | | Justificación de la devolución. |
| `estado` | `VARCHAR(20)` | NO | DEFAULT 'APROBADA' | Estado del trámite. |
| `usuario_id` | `BIGINT` | NO | **FK** `usuario(id)` ON DELETE RESTRICT | Usuario que autorizó la devolución. |

---

### 5.6 Tabla: `devolucion_linea`
* **Responsabilidad:** Detalle de productos devueltos y su costo de reingreso a inventario.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la línea. |
| `devolucion_id` | `BIGINT` | NO | **FK** `devolucion(id)` ON DELETE CASCADE | Documento de devolución. |
| `referencia_id` | `BIGINT` | NO | **FK** `referencia(id)` ON DELETE RESTRICT | Referencia reincorporada. |
| `cantidad` | `DECIMAL(12, 3)`| NO | CHECK (`cantidad` > 0) | Unidades devueltas al stock. |
| `costo_unitario_aplicado`| `DECIMAL(15, 4)`| NO | | Costo con el que reingresa (histórico o promedio). |

---

### 5.7 Tabla: `ajuste_inventario`
* **Responsabilidad:** Registro de ajustes manuales de stock autorizados por el Administrador.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del ajuste. |
| `consecutivo` | `VARCHAR(25)` | NO | **UK** `uk_ajuste_consecutivo` | Consecutivo correlativo (ej. `AJU-000001`). |
| `referencia_id` | `BIGINT` | NO | **FK** `referencia(id)` ON DELETE RESTRICT | Referencia ajustada. |
| `tipo_ajuste` | `VARCHAR(20)` | NO | CHECK (`tipo_ajuste` IN ('INCREMENTO','DISMINUCION')) | Sentido del ajuste físico. |
| `cantidad` | `DECIMAL(12, 3)`| NO | CHECK (`cantidad` > 0) | Unidades ajustadas. |
| `costo_unitario_referencia`| `DECIMAL(15, 4)`| NO | | Costo de valoración del ajuste. |
| `motivo` | `VARCHAR(255)` | NO | | Justificación obligatoria del ajuste (RN-08). |
| `fecha_hora` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Momento del ajuste. |
| `usuario_id` | `BIGINT` | NO | **FK** `usuario(id)` ON DELETE RESTRICT | Administrador que ejecutó el ajuste. |

---

## 6. Tablas de Control Transversal

### 6.1 Tabla: `comprobante_consecutivo`
* **Responsabilidad:** Control correlativo de numeraciones internas por tipo de comprobante (RN-12).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador. |
| `tipo_documento` | `VARCHAR(25)` | NO | **UK** `uk_consecutivo_tipo` | `VENTA`, `COMPRA`, `DEVOLUCION`, `AJUSTE`. |
| `prefijo` | `VARCHAR(10)` | NO | | Prefijo textual (`VTA-`, `CMP-`, `DEV-`, `AJU-`). |
| `ultimo_numero` | `BIGINT` | NO | DEFAULT 0 | Último correlativo asignado. |

---

### 6.2 Tabla: `auditoria_log`
* **Responsabilidad:** Registro básico inmutable de auditoría del sistema (RN-11).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del log. |
| `usuario_id` | `BIGINT` | SÍ | **FK** `usuario(id)` ON DELETE SET NULL | Usuario responsable. |
| `fecha_hora` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Fecha y hora exacta. |
| `modulo` | `VARCHAR(40)` | NO | | Módulo del sistema afectado. |
| `accion` | `VARCHAR(50)` | NO | | Acción ejecutada (MODIFICAR_PRECIO, CANCELAR, etc.). |
| `descripcion` | `TEXT` | NO | | Detalle explicativo de la operación. |

* **Índice:** `idx_auditoria_fecha` (`fecha_hora`).
