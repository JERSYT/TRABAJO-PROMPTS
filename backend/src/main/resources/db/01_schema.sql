-- =============================================================================
-- PROYECTO: Sistema Web de Gestión de Inventario para Ferretería
-- ETAPA 4: Script DDL de Creación del Esquema de Base de Datos MySQL 8.0
-- ARCHIVO: 01_schema.sql
-- DESCRIPCIÓN: Estructura de tablas, claves primarias, claves foráneas,
--              restricciones UNIQUE, restricciones CHECK e índices.
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- 1. IDENTIDAD Y SEGURIDAD
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS auditoria_log;
DROP TABLE IF EXISTS devolucion_linea;
DROP TABLE IF EXISTS devolucion;
DROP TABLE IF EXISTS venta_linea;
DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS compra_linea;
DROP TABLE IF EXISTS compra;
DROP TABLE IF EXISTS ajuste_inventario;
DROP TABLE IF EXISTS inventario_movimiento;
DROP TABLE IF EXISTS inventario_saldo;
DROP TABLE IF EXISTS referencia;
DROP TABLE IF EXISTS presentacion;
DROP TABLE IF EXISTS marca;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS unidad_medida;
DROP TABLE IF EXISTS tercero;
DROP TABLE IF EXISTS comprobante_consecutivo;
DROP TABLE IF EXISTS usuario_rol;
DROP TABLE IF EXISTS rol;
DROP TABLE IF EXISTS usuario;

SET FOREIGN_KEY_CHECKS = 1;

-- Tabla de Usuarios
CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    email VARCHAR(100) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_actualizacion DATETIME(6) NULL ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_usuario_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Roles
CREATE TABLE rol (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL,
    descripcion VARCHAR(150) NULL,
    CONSTRAINT uk_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla Asociativa Usuario - Rol
CREATE TABLE usuario_rol (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuariorol_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT fk_usuariorol_rol FOREIGN KEY (rol_id) REFERENCES rol(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. TERCEROS (CLIENTES Y PROVEEDORES UNIFICADOS - RN-10)
-- -----------------------------------------------------------------------------
CREATE TABLE tercero (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(10) NOT NULL,
    numero_documento VARCHAR(30) NOT NULL,
    razon_social VARCHAR(150) NOT NULL,
    tipo_persona VARCHAR(15) NOT NULL,
    telefono VARCHAR(30) NULL,
    email VARCHAR(100) NULL,
    direccion VARCHAR(150) NULL,
    es_cliente BOOLEAN NOT NULL DEFAULT TRUE,
    es_proveedor BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_tercero_documento UNIQUE (numero_documento),
    CONSTRAINT chk_tercero_tipo_doc CHECK (tipo_documento IN ('CC','NIT','CE','PASAPORTE')),
    CONSTRAINT chk_tercero_tipo_persona CHECK (tipo_persona IN ('NATURAL','JURIDICA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_tercero_documento ON tercero(numero_documento);
CREATE INDEX idx_tercero_razon_social ON tercero(razon_social);

-- -----------------------------------------------------------------------------
-- 3. CATÁLOGO DE PRODUCTOS Y REFERENCIAS COMERCIALES
-- -----------------------------------------------------------------------------
CREATE TABLE unidad_medida (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    CONSTRAINT uk_unidad_codigo UNIQUE (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE producto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE marca (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_marca_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE presentacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_presentacion_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE referencia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    marca_id BIGINT NOT NULL,
    presentacion_id BIGINT NOT NULL,
    unidad_medida_id BIGINT NOT NULL,
    codigo_barras VARCHAR(50) NOT NULL,
    precio_venta DECIMAL(15, 2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_ref_codigo_barras UNIQUE (codigo_barras),
    CONSTRAINT uk_ref_prod_marca_pres UNIQUE (producto_id, marca_id, presentacion_id),
    CONSTRAINT chk_ref_precio CHECK (precio_venta >= 0),
    CONSTRAINT fk_ref_producto FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ref_marca FOREIGN KEY (marca_id) REFERENCES marca(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ref_presentacion FOREIGN KEY (presentacion_id) REFERENCES presentacion(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ref_unidad FOREIGN KEY (unidad_medida_id) REFERENCES unidad_medida(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ref_codigo_barras ON referencia(codigo_barras);

-- -----------------------------------------------------------------------------
-- 4. INVENTARIO, SALDOS Y MOVIMIENTOS (KÁRDEX)
-- -----------------------------------------------------------------------------
CREATE TABLE inventario_saldo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    referencia_id BIGINT NOT NULL,
    cantidad_disponible DECIMAL(12, 3) NOT NULL DEFAULT 0.000,
    costo_promedio_ponderado DECIMAL(15, 4) NOT NULL DEFAULT 0.0000,
    fecha_actualizacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_saldo_referencia UNIQUE (referencia_id),
    CONSTRAINT chk_saldo_cantidad CHECK (cantidad_disponible >= 0),
    CONSTRAINT chk_saldo_cpp CHECK (costo_promedio_ponderado >= 0),
    CONSTRAINT fk_saldo_referencia FOREIGN KEY (referencia_id) REFERENCES referencia(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventario_movimiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    referencia_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(30) NOT NULL,
    cantidad DECIMAL(12, 3) NOT NULL,
    costo_unitario_movimiento DECIMAL(15, 4) NOT NULL,
    saldo_resultante DECIMAL(12, 3) NOT NULL,
    documento_tipo VARCHAR(20) NOT NULL,
    documento_id BIGINT NOT NULL,
    motivo VARCHAR(255) NULL,
    fecha_hora DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    usuario_id BIGINT NOT NULL,
    CONSTRAINT chk_mov_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_mov_costo CHECK (costo_unitario_movimiento >= 0),
    CONSTRAINT chk_mov_saldo CHECK (saldo_resultante >= 0),
    CONSTRAINT fk_mov_referencia FOREIGN KEY (referencia_id) REFERENCES referencia(id) ON DELETE RESTRICT,
    CONSTRAINT fk_mov_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_mov_ref_fecha ON inventario_movimiento(referencia_id, fecha_hora);
CREATE INDEX idx_mov_doc ON inventario_movimiento(documento_tipo, documento_id);

-- -----------------------------------------------------------------------------
-- 5. COMPRAS Y LÍNEAS DE COMPRA
-- -----------------------------------------------------------------------------
CREATE TABLE compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consecutivo VARCHAR(25) NOT NULL,
    proveedor_id BIGINT NOT NULL,
    fecha_hora DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    total DECIMAL(15, 2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'RECIBIDA',
    motivo_cancelacion VARCHAR(255) NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT uk_compra_consecutivo UNIQUE (consecutivo),
    CONSTRAINT chk_compra_total CHECK (total >= 0),
    CONSTRAINT chk_compra_estado CHECK (estado IN ('RECIBIDA','CANCELADA')),
    CONSTRAINT fk_compra_proveedor FOREIGN KEY (proveedor_id) REFERENCES tercero(id) ON DELETE RESTRICT,
    CONSTRAINT fk_compra_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE compra_linea (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    compra_id BIGINT NOT NULL,
    referencia_id BIGINT NOT NULL,
    cantidad DECIMAL(12, 3) NOT NULL,
    costo_unitario DECIMAL(15, 4) NOT NULL,
    subtotal DECIMAL(15, 2) NOT NULL,
    CONSTRAINT chk_complinea_cant CHECK (cantidad > 0),
    CONSTRAINT chk_complinea_costo CHECK (costo_unitario >= 0),
    CONSTRAINT chk_complinea_subtotal CHECK (subtotal >= 0),
    CONSTRAINT fk_complinea_compra FOREIGN KEY (compra_id) REFERENCES compra(id) ON DELETE CASCADE,
    CONSTRAINT fk_complinea_ref FOREIGN KEY (referencia_id) REFERENCES referencia(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. VENTAS DE CONTADO Y LÍNEAS DE VENTA
-- -----------------------------------------------------------------------------
CREATE TABLE venta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consecutivo VARCHAR(25) NOT NULL,
    cliente_id BIGINT NOT NULL,
    fecha_hora DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    subtotal DECIMAL(15, 2) NOT NULL,
    total DECIMAL(15, 2) NOT NULL,
    monto_pagado_efectivo DECIMAL(15, 2) NOT NULL,
    cambio_devuelto DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA',
    motivo_cancelacion VARCHAR(255) NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT uk_venta_consecutivo UNIQUE (consecutivo),
    CONSTRAINT chk_venta_total CHECK (total >= 0),
    CONSTRAINT chk_venta_pago CHECK (monto_pagado_efectivo >= total),
    CONSTRAINT chk_venta_estado CHECK (estado IN ('COMPLETADA','CANCELADA')),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (cliente_id) REFERENCES tercero(id) ON DELETE RESTRICT,
    CONSTRAINT fk_venta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE venta_linea (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    referencia_id BIGINT NOT NULL,
    cantidad DECIMAL(12, 3) NOT NULL,
    precio_unitario DECIMAL(15, 2) NOT NULL,
    costo_historico_unitario DECIMAL(15, 4) NOT NULL,
    subtotal DECIMAL(15, 2) NOT NULL,
    CONSTRAINT chk_ventalinea_cant CHECK (cantidad > 0),
    CONSTRAINT chk_ventalinea_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_ventalinea_costo CHECK (costo_historico_unitario >= 0),
    CONSTRAINT chk_ventalinea_subtotal CHECK (subtotal >= 0),
    CONSTRAINT fk_ventalinea_venta FOREIGN KEY (venta_id) REFERENCES venta(id) ON DELETE CASCADE,
    CONSTRAINT fk_ventalinea_ref FOREIGN KEY (referencia_id) REFERENCES referencia(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. DEVOLUCIONES DE CLIENTES
-- -----------------------------------------------------------------------------
CREATE TABLE devolucion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consecutivo VARCHAR(25) NOT NULL,
    venta_id BIGINT NULL,
    cliente_id BIGINT NULL,
    tipo_devolucion VARCHAR(20) NOT NULL,
    fecha_hora DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    motivo VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'APROBADA',
    usuario_id BIGINT NOT NULL,
    CONSTRAINT uk_dev_consecutivo UNIQUE (consecutivo),
    CONSTRAINT chk_dev_tipo CHECK (tipo_devolucion IN ('VINCULADA','NO_VINCULADA')),
    CONSTRAINT fk_dev_venta FOREIGN KEY (venta_id) REFERENCES venta(id) ON DELETE RESTRICT,
    CONSTRAINT fk_dev_cliente FOREIGN KEY (cliente_id) REFERENCES tercero(id) ON DELETE RESTRICT,
    CONSTRAINT fk_dev_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE devolucion_linea (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    devolucion_id BIGINT NOT NULL,
    referencia_id BIGINT NOT NULL,
    cantidad DECIMAL(12, 3) NOT NULL,
    costo_unitario_aplicado DECIMAL(15, 4) NOT NULL,
    CONSTRAINT chk_devlinea_cant CHECK (cantidad > 0),
    CONSTRAINT chk_devlinea_costo CHECK (costo_unitario_aplicado >= 0),
    CONSTRAINT fk_devlinea_dev FOREIGN KEY (devolucion_id) REFERENCES devolucion(id) ON DELETE CASCADE,
    CONSTRAINT fk_devlinea_ref FOREIGN KEY (referencia_id) REFERENCES referencia(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. AJUSTES DE INVENTARIO (ADMINISTRADOR EXCLUSIVO - RN-08)
-- -----------------------------------------------------------------------------
CREATE TABLE ajuste_inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consecutivo VARCHAR(25) NOT NULL,
    referencia_id BIGINT NOT NULL,
    tipo_ajuste VARCHAR(20) NOT NULL,
    cantidad DECIMAL(12, 3) NOT NULL,
    costo_unitario_referencia DECIMAL(15, 4) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    fecha_hora DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    usuario_id BIGINT NOT NULL,
    CONSTRAINT uk_ajuste_consecutivo UNIQUE (consecutivo),
    CONSTRAINT chk_ajuste_tipo CHECK (tipo_ajuste IN ('INCREMENTO','DISMINUCION')),
    CONSTRAINT chk_ajuste_cant CHECK (cantidad > 0),
    CONSTRAINT fk_ajuste_ref FOREIGN KEY (referencia_id) REFERENCES referencia(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ajuste_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 9. CONTROL DE CONSECUTIVOS Y AUDITORÍA BÁSICA (RN-11, RN-12)
-- -----------------------------------------------------------------------------
CREATE TABLE comprobante_consecutivo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(25) NOT NULL,
    prefijo VARCHAR(10) NOT NULL,
    ultimo_numero BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_consecutivo_tipo UNIQUE (tipo_documento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE auditoria_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NULL,
    fecha_hora DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    modulo VARCHAR(40) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,
    CONSTRAINT fk_audit_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_auditoria_fecha ON auditoria_log(fecha_hora);
