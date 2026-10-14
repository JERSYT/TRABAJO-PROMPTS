# Documento: Diagramas de Secuencia de Procesos Críticos

## Metadatos
* **Nombre:** 05_diagramas_secuencia.md
* **Descripción:** Diagramas de secuencia en formato Mermaid y explicación técnica para los flujos críticos: login, compra, venta, devolución, ajuste y cancelaciones.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Arquitecto de Software)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Diagrama de Secuencia: Inicio de Sesión (Login)

```mermaid
sequenceDiagram
    autonumber
    actor Usuario
    participant Frontend as React Frontend
    participant Filter as SecurityFilterChain
    participant AuthMgr as AuthenticationManager
    participant UserDetails as CustomUserDetailsService
    participant Session as HttpSession / SecurityContext

    Usuario->>Frontend: Ingresa usuario y contraseña
    Frontend->>Filter: POST /api/v1/auth/login {username, password}
    Filter->>AuthMgr: authenticate(UsernamePasswordAuthenticationToken)
    AuthMgr->>UserDetails: loadUserByUsername(username)
    UserDetails-->>AuthMgr: UserDetails (con BCrypt hash y roles)
    AuthMgr->>AuthMgr: PasswordEncoder.matches(raw, hash)
    
    alt Credenciales Válidas
        AuthMgr-->>Filter: Authentication (Authenticated)
        Filter->>Session: Guardar SecurityContext en sesión
        Filter-->>Frontend: 200 OK + Set-Cookie: JSESSIONID; HttpOnly; SameSite=Lax
        Frontend->>Frontend: Guardar datos de usuario en AuthContext
        Frontend-->>Usuario: Redirige al Dashboard
    else Credenciales Inválidas
        AuthMgr-->>Filter: BadCredentialsException
        Filter-->>Frontend: 401 Unauthorized (RFC 9457 ProblemDetail)
        Frontend-->>Usuario: Muestra mensaje de error
    end
```

---

## 2. Diagrama de Secuencia: Registro de Venta de Contado (Concurrencia y Kárdex)

```mermaid
sequenceDiagram
    autonumber
    actor Operador
    participant UI as React UI (Mostrador)
    participant Ctrl as SaleController
    participant Svc as SaleService
    participant InvSvc as InventoryService
    participant Repo as SaleRepository / InventoryRepository
    participant DB as MySQL Database

    Operador->>UI: Selecciona cliente, agrega líneas y recibe efectivo
    UI->>Ctrl: POST /api/v1/sales {clienteId, lineas: [{refId, cant}], efectivo}
    Ctrl->>Svc: registrarVenta(CrearVentaRequest, usuarioActual)
    
    Note over Svc,DB: Inicio Transacción Atómica (@Transactional)
    Svc->>InvSvc: descontarStockConBloqueo(lineas)
    
    loop Por cada línea de producto
        InvSvc->>Repo: findSaldoByReferenciaIdForUpdate(refId) [SELECT ... FOR UPDATE]
        Repo->>DB: Bloqueo de fila en inventario_saldo
        DB-->>Repo: Saldo actual y CPP vigente
        
        alt Saldo Disponible >= Cantidad Solicitada
            InvSvc->>InvSvc: Nuevo saldo = Saldo - Cantidad
            InvSvc->>Repo: updateSaldo(nuevoSaldo)
            InvSvc->>Repo: saveMovimiento(tipo: VENTA, costo: CPP_vigente)
        else Stock Insuficiente (Violación RN-01)
            InvSvc-->>Svc: Lanzar StockInsuficienteException
            Note over Svc,DB: Rollback Transaccional Completo
            Svc-->>Ctrl: Propagar Error
            Ctrl-->>UI: 409 Conflict (ProblemDetail)
            UI-->>Operador: Mostrar alerta: "Stock insuficiente"
        end
    end
    
    Svc->>Repo: saveVenta(consecutivo, lineas con costo_historico)
    Svc->>DB: Commit Transacción
    Svc-->>Ctrl: VentaResponse
    Ctrl-->>UI: 201 Created (VentaResponse)
    UI-->>Operador: Muestra recibo listo para imprimir
```

---

## 3. Diagrama de Secuencia: Registro de Compra Completa y Recálculo de Costo Promedio (CPP)

```mermaid
sequenceDiagram
    autonumber
    actor Operador
    participant UI as React UI (Compras)
    participant Ctrl as PurchaseController
    participant Svc as PurchaseService
    participant InvSvc as InventoryService
    participant DB as MySQL Database

    Operador->>UI: Selecciona proveedor, agrega ítems y costo unitario
    UI->>Ctrl: POST /api/v1/purchases {proveedorId, lineas: [{refId, cant, costoUnit}]}
    Ctrl->>Svc: registrarCompra(CrearCompraRequest, usuarioActual)
    
    Note over Svc,DB: Inicio Transacción Atómica (@Transactional)
    Svc->>Svc: Generar consecutivo CMP-XXXXXX
    Svc->>Svc: Guardar cabecera y líneas en estado RECIBIDA
    
    loop Por cada línea comprada
        Svc->>InvSvc: procesarEntradaCompra(refId, cant, costoUnit)
        InvSvc->>DB: Obtener saldo y CPP actual
        InvSvc->>InvSvc: Calcular nuevo CPP = [(Stock*CPP) + (Cant*CostoUnit)] / (Stock+Cant)
        InvSvc->>InvSvc: Nuevo stock = Stock + Cant
        InvSvc->>DB: Actualizar inventario_saldo con nuevo stock y nuevo CPP
        InvSvc->>DB: Registrar inventario_movimiento (tipo: COMPRA)
    end
    
    Svc->>DB: Commit Transacción
    Svc-->>Ctrl: CompraResponse
    Ctrl-->>UI: 201 Created (CompraResponse)
    UI-->>Operador: Muestra confirmación de compra y nuevo stock
```

---

## 4. Diagrama de Secuencia: Devolución de Cliente y Reincorporación de Stock

```mermaid
sequenceDiagram
    autonumber
    actor Operador
    participant UI as React UI (Devoluciones)
    participant Ctrl as ReturnController
    participant Svc as ReturnService
    participant InvSvc as InventoryService
    participant DB as MySQL Database

    Operador->>UI: Ingresa consecutivo de venta, ítem, cantidad devuelta y motivo
    UI->>Ctrl: POST /api/v1/returns {ventaId, lineas, motivo}
    Ctrl->>Svc: registrarDevolucion(CrearDevolucionRequest, usuarioActual)
    
    Note over Svc,DB: Inicio Transacción Atómica (@Transactional)
    alt Es Devolución Vinculada
        Svc->>DB: Validar venta original y que suma devuelta <= vendida
        Svc->>Svc: Obtener costo_historico_unitario de la venta_linea
    else Es Devolución No Vinculada
        Svc->>DB: Obtener CPP vigente de la referencia
    end
    
    Svc->>InvSvc: reintegrarStock(refId, cant, costoAplicado)
    InvSvc->>DB: Sumar cantidad a inventario_saldo
    InvSvc->>DB: Registrar inventario_movimiento (tipo: DEVOLUCION_CLIENTE)
    Svc->>DB: Guardar cabecera y líneas de devolucion (DEV-XXXXXX)
    Svc->>DB: Commit Transacción
    
    Svc-->>Ctrl: DevolucionResponse
    Ctrl-->>UI: 201 Created (DevolucionResponse)
    UI-->>Operador: Muestra comprobante de devolución
```

---

## 5. Diagrama de Secuencia: Ajuste Manual de Inventario (Solo Administrador)

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrador
    participant UI as React UI
    participant Ctrl as InventoryAdjustmentController
    participant Svc as InventoryAdjustmentService
    participant DB as MySQL Database

    Admin->>UI: Selecciona ítem, tipo (INCREMENTO/DISMINUCION), cantidad y motivo
    UI->>Ctrl: POST /api/v1/inventory/adjustments {refId, tipo, cant, motivo}
    
    Note over Ctrl: Verificación de Rol (@PreAuthorize("hasRole('ADMINISTRADOR')"))
    alt Usuario NO es Administrador
        Ctrl-->>UI: 403 Forbidden
        UI-->>Admin: "Acción no autorizada para su rol"
    else Usuario es Administrador
        Ctrl->>Svc: ejecutarAjuste(AjusteRequest, adminId)
        Note over Svc,DB: Inicio Transacción
        
        alt Tipo Disminución y Saldo < Cantidad
            Svc-->>Ctrl: Lanzar DiscrepanciaStockException
            Ctrl-->>UI: 409 Conflict ("Saldo resultante sería negativo")
        else Ajuste Válido
            Svc->>DB: Actualizar saldo_disponible
            Svc->>DB: Registrar movimiento (AJUSTE_ENTRADA o AJUSTE_SALIDA)
            Svc->>DB: Guardar documento de ajuste AJU-XXXXXX
            Svc->>DB: Insertar registro en auditoria_log con motivo y adminId
            Svc->>DB: Commit Transacción
            Svc-->>Ctrl: AjusteResponse
            Ctrl-->>UI: 201 Created
            UI-->>Admin: Confirma ajuste exitoso
        end
    end
```

---

## 6. Diagrama de Secuencia: Cancelación de Compra o Venta

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrador
    participant UI as React UI
    participant Ctrl as SaleController / PurchaseController
    participant Svc as CancelacionService
    participant InvSvc as InventoryService
    participant DB as MySQL Database

    Admin->>UI: Solicita cancelar venta/compra e ingresa motivo
    UI->>Ctrl: POST /api/v1/{sales|purchases}/{id}/cancel {motivo}
    Ctrl->>Svc: cancelarDocumento(id, motivo, adminId)
    
    Note over Svc,DB: Transacción con comprobación de consistencia
    alt Cancelación de Venta
        Svc->>DB: Verificar si la venta tiene devoluciones activas
        alt Tiene Devoluciones
            Svc-->>Ctrl: 409 Conflict ("No se puede cancelar una venta con devoluciones")
        else Sin Devoluciones
            Svc->>InvSvc: compensarEntradaStock(lineasVenta)
            Svc->>DB: Actualizar estado de venta a CANCELADA y guardar motivo
            Svc->>DB: Registrar movimientos compensatorios en Kárdex
            Svc->>DB: Auditar cancelación
            Svc-->>Ctrl: 200 OK
        end
    else Cancelación de Compra
        Svc->>DB: Verificar que stock actual >= unidades a descontar de la compra
        alt Stock actual insuficiente (ya se vendió parte de la mercancía)
            Svc-->>Ctrl: 409 Conflict ("Stock ya consumido por operaciones posteriores")
        else Stock suficiente
            Svc->>InvSvc: compensarSalidaStock(lineasCompra)
            Svc->>DB: Actualizar estado de compra a CANCELADA y guardar motivo
            Svc->>DB: Registrar movimientos compensatorios en Kárdex
            Svc->>DB: Auditar cancelación
            Svc-->>Ctrl: 200 OK
        end
    end
    Ctrl-->>UI: Resultado de la cancelación
```
