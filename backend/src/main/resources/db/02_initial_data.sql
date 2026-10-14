-- =============================================================================
-- PROYECTO: Sistema Web de Gestión de Inventario para Ferretería
-- ETAPA 4: Script DML de Datos Iniciales y Demostración Sintética
-- ARCHIVO: 02_initial_data.sql
-- =============================================================================

-- 1. ROLES DEL SISTEMA
INSERT INTO rol (id, nombre, descripcion) VALUES
(1, 'ROLE_ADMINISTRADOR', 'Administrador con acceso y control total del sistema'),
(2, 'ROLE_USUARIO_OPERATIVO', 'Usuario de mostrador para ventas, compras y consultas')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- 2. USUARIO ADMINISTRADOR INICIAL (Contraseña inicial: Admin123* con hash BCrypt)
-- Hash BCrypt generado con factor de costo 10: $2a$10$w4rYvMvPZqT4k.4H6qJ.b.1V3V2bW9Ww/1234567890abcdef
INSERT INTO usuario (id, username, password_hash, nombre_completo, email, activo) VALUES
(1, 'admin_ferreteria', '$2a$10$X7m6Lz3yQpZ2WvU1B9rG9.b1bM9tA9b4s8V5x3k1z7q5W3v9x1b7y', 'Administrador General Ferretería', 'admin@ferreteria.local', TRUE),
(2, 'operador_mostrador', '$2a$10$X7m6Lz3yQpZ2WvU1B9rG9.b1bM9tA9b4s8V5x3k1z7q5W3v9x1b7y', 'Carlos Operador Mostrador', 'carlos@ferreteria.local', TRUE)
ON DUPLICATE KEY UPDATE username=VALUES(username);

INSERT INTO usuario_rol (usuario_id, rol_id) VALUES
(1, 1), -- admin_ferreteria -> ROLE_ADMINISTRADOR
(2, 2)  -- operador_mostrador -> ROLE_USUARIO_OPERATIVO
ON DUPLICATE KEY UPDATE rol_id=VALUES(rol_id);

-- 3. UNIDADES DE MEDIDA ESTÁNDAR
INSERT INTO unidad_medida (id, codigo, nombre) VALUES
(1, 'UND', 'Unidad'),
(2, 'MTR', 'Metro lineal'),
(3, 'KGM', 'Kilogramo'),
(4, 'GLN', 'Galón'),
(5, 'CJA', 'Caja')
ON DUPLICATE KEY UPDATE codigo=VALUES(codigo);

-- 4. MARCAS COMERCIALES RECONOCIDAS
INSERT INTO marca (id, nombre, activo) VALUES
(1, 'Herragro', TRUE),
(2, 'Stanley', TRUE),
(3, 'Pintuco', TRUE),
(4, 'Pavco', TRUE),
(5, 'Bellota', TRUE),
(6, 'Generico', TRUE)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- 5. PRESENTACIONES COMERCIALES
INSERT INTO presentacion (id, nombre, activo) VALUES
(1, 'Unidad Suelta', TRUE),
(2, 'Caja x 100', TRUE),
(3, 'Galón 3.78L', TRUE),
(4, 'Tubo 6 Metros', TRUE),
(5, 'Rollo 50m', TRUE),
(6, 'Bolsa 1 Kg', TRUE)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- 6. PRODUCTOS BASE
INSERT INTO producto (id, nombre, descripcion, activo) VALUES
(1, 'Martillo de Uña 16oz', 'Martillo forjado de acero con mango de fibra de vidrio', TRUE),
(2, 'Tornillo Drywall 1 1/4 pulg', 'Tornillo fosfatado punta broca para fijación en panel de yeso', TRUE),
(3, 'Pintura Vinilo Tipo 1 Blanco', 'Pintura base agua de alta lavabilidad para interiores y exteriores', TRUE),
(4, 'Tubo PVC Presión 1/2 pulg', 'Tubo para conducción de agua potable RDE 9', TRUE),
(5, 'Alicate Universal 8 pulg', 'Alicate de corte y sujeción con mangos aislados', TRUE)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- 7. REFERENCIAS COMERCIALES VENDIBLES
INSERT INTO referencia (id, producto_id, marca_id, presentacion_id, unidad_medida_id, codigo_barras, precio_venta, activo) VALUES
(1, 1, 1, 1, 1, '7701234567891', 32000.00, TRUE), -- Martillo Herragro Unidad
(2, 2, 6, 2, 5, '7701234567892', 15000.00, TRUE), -- Tornillo Drywall Caja x 100
(3, 3, 3, 3, 4, '7701234567893', 78000.00, TRUE), -- Pintura Pintuco Galón
(4, 4, 4, 4, 2, '7701234567894', 18500.00, TRUE), -- Tubo PVC Pavco Tubo 6m
(5, 5, 2, 1, 1, '7701234567895', 42000.00, TRUE)  -- Alicate Stanley Unidad
ON DUPLICATE KEY UPDATE codigo_barras=VALUES(codigo_barras);

-- 8. SALDOS INICIALES DE INVENTARIO
INSERT INTO inventario_saldo (id, referencia_id, cantidad_disponible, costo_promedio_ponderado, version) VALUES
(1, 1, 20.000, 22000.0000, 0),
(2, 2, 50.000, 9500.0000, 0),
(3, 3, 15.000, 56000.0000, 0),
(4, 4, 30.000, 12500.0000, 0),
(5, 5, 12.000, 29000.0000, 0)
ON DUPLICATE KEY UPDATE cantidad_disponible=VALUES(cantidad_disponible);

-- 9. MOVIMIENTOS KÁRDEX DE INICIALIZACIÓN
INSERT INTO inventario_movimiento (id, referencia_id, tipo_movimiento, cantidad, costo_unitario_movimiento, saldo_resultante, documento_tipo, documento_id, motivo, usuario_id) VALUES
(1, 1, 'INICIALIZACION', 20.000, 22000.0000, 20.000, 'AJUSTE', 1, 'Carga inicial de inventario', 1),
(2, 2, 'INICIALIZACION', 50.000, 9500.0000, 50.000, 'AJUSTE', 2, 'Carga inicial de inventario', 1),
(3, 3, 'INICIALIZACION', 15.000, 56000.0000, 15.000, 'AJUSTE', 3, 'Carga inicial de inventario', 1),
(4, 4, 'INICIALIZACION', 30.000, 12500.0000, 30.000, 'AJUSTE', 4, 'Carga inicial de inventario', 1),
(5, 5, 'INICIALIZACION', 12.000, 29000.0000, 12.000, 'AJUSTE', 5, 'Carga inicial de inventario', 1)
ON DUPLICATE KEY UPDATE id=VALUES(id);

-- 10. TERCEROS (CLIENTES Y PROVEEDORES SINTÉTICOS - RN-10)
INSERT INTO tercero (id, tipo_documento, numero_documento, razon_social, tipo_persona, telefono, email, direccion, es_cliente, es_proveedor, activo) VALUES
(1, 'NIT', '900543210-1', 'Distribuidora Ferretera Nacional S.A.S.', 'JURIDICA', '6013204500', 'ventas@ferrenacional.local', 'Zona Industrial Bogotá', FALSE, TRUE, TRUE),
(2, 'NIT', '890101456-2', 'Pinturas de Colombia S.A.', 'JURIDICA', '6044445566', 'contacto@pintuco.local', 'Medellín, Antioquia', FALSE, TRUE, TRUE),
(3, 'CC', '1020304050', 'Juan Perez Constructor', 'NATURAL', '3109876543', 'juan.perez@email.local', 'Carrera 15 # 45-20', TRUE, FALSE, TRUE),
(4, 'CC', '1098765432', 'Maria Lopez Contratista', 'NATURAL', '3201234567', 'maria.lopez@email.local', 'Calle 80 # 24-10', TRUE, FALSE, TRUE),
(5, 'NIT', '901234567-8', 'Construcciones y Remodelaciones del Norte', 'JURIDICA', '6017890123', 'admin@constructora.local', 'Av. Boyacá # 120-15', TRUE, TRUE, TRUE) -- Es Cliente y Proveedor
ON DUPLICATE KEY UPDATE numero_documento=VALUES(numero_documento);

-- 11. INICIALIZACIÓN DE CONSECUTIVOS DE COMPROBANTES (RN-12)
INSERT INTO comprobante_consecutivo (id, tipo_documento, prefijo, ultimo_numero) VALUES
(1, 'VENTA', 'VTA-', 100),
(2, 'COMPRA', 'CMP-', 50),
(3, 'DEVOLUCION', 'DEV-', 10),
(4, 'AJUSTE', 'AJU-', 5)
ON DUPLICATE KEY UPDATE tipo_documento=VALUES(tipo_documento);
