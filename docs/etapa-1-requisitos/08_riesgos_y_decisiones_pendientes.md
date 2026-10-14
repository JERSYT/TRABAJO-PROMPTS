# Documento: Registro de Riesgos Iniciales y Decisiones Pendientes

## Metadatos
* **Nombre:** 08_riesgos_y_decisiones_pendientes.md
* **Descripción:** Registro estructurado de riesgos técnicos, operativos y de datos, junto con el inventario priorizado de decisiones pendientes para el sistema de inventario.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Analista de Riesgos y Sistemas)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Registro de Riesgos Iniciales del Proyecto

| ID | Área / Aspecto Crítico | Descripción del Riesgo | Probabilidad | Impacto | Prioridad | Posible Mitigación |
| :--- | :--- | :--- | :---: | :---: | :---: | :--- |
| **RG-01** | **Consistencia de Existencias** | Condiciones de carrera ante ventas o devoluciones concurrentes que provoquen saldos negativos o descuadres entre saldo actual y movimientos. | Media | Crítico | **Alta** | Emplear transacciones de base de datos con aislamiento adecuado y bloqueo pesimista (`PESSIMISTIC_WRITE`) o actualización atómica (`UPDATE stock = stock - qty WHERE stock >= qty`). |
| **RG-02** | **Cálculo de Costos** | Discrepancias en la valoración del inventario (CPP) al registrar compras sucesivas, cancelaciones de compras o devoluciones de mercancía antigua. | Media | Alto | **Alta** | Aplicar rigurosamente la fórmula ponderada con tipos de dato de escala fija (`BigDecimal`), congelando el costo histórico en cada renglón de venta. |
| **RG-03** | **Permisos por Rol** | Escalada de privilegios o ejecución de acciones administrativas (ajustes manuales de stock, cancelaciones) por usuarios operativos. | Baja | Crítico | **Alta** | Aplicar validación de seguridad a nivel de método en backend (`@PreAuthorize`) y reglas URL en Spring Security; no depender del frontend. |
| **RG-04** | **Validación de Devoluciones** | Reingreso indebido de cantidades mayores a las efectivamente vendidas o aceptación de devoluciones fraudulentas. | Media | Medio | **Media** | Validar que para devoluciones vinculadas la suma acumulada devuelta no supere la cantidad original facturada. |
| **RG-05** | **Estados y Cancelación** | Cancelación de compras cuyo stock ya fue consumido o vendido, generando stock negativo retroactivo. | Media | Crítico | **Alta** | Bloquear la anulación de una compra si las existencias actuales de cualquiera de sus ítems son inferiores a la cantidad a revertir. |
| **RG-06** | **Consecutivos Internos** | Consecutivos duplicados o desordenados por concurrencia en la emisión de comprobantes. | Media | Alto | **Media** | Manejo de secuencias atómicas o bloqueo a nivel de tabla en la generación del siguiente número correlativo. |
| **RG-07** | **Datos de Clientes y Proveedores** | Inconsistencias o duplicidad de registros fiscales (NIT / CC con diferentes tipografías o formatos). | Media | Medio | **Media** | Normalización de cadenas y restricción `UNIQUE` en la base de datos sobre el número de documento de identificación. |
| **RG-08** | **Seguridad de Autenticación** | Fugas de sesión, secuestro de cookies o ataques CSRF debido a la separación de dominios entre frontend y backend. | Media | Alto | **Alta** | Configuración estricta de cookies de sesión (`HttpOnly`, `SameSite=Lax/Strict`, `Secure`), origen CORS controlado y validación de token CSRF (`CookieCsrfTokenRepository`). |
| **RG-09** | **Respaldo y Recuperación** | Pérdida de información en caso de fallo del gestor MySQL al no contar con herramientas automáticas corporativas en el plan gratuito. | Media | Alto | **Media** | Documentar procedimientos estandarizados de exportación lógica (`mysqldump`) y restauración paso a paso. |
| **RG-10** | **Criterios de Calidad y Pruebas** | Falsos positivos en pruebas de integración si no se ejecutan contra un motor MySQL real. | Alta | Alto | **Alta** | Prohibir el uso exclusivo de H2 para validar comportamiento específico de tipos y concurrencia de MySQL. |

---

## 2. Registro Priorizado de Decisiones Pendientes

Las decisiones se clasifican conforme a las categorías requeridas por la metodología:

### Categoría 1: Bloqueantes para Aprobar Requisitos
*(No se detectan bloqueos absolutos de requisitos. El alcance de la ferretería de sede única, ventas de contado y pago en efectivo se encuentra completamente delimitado en el contexto).*

### Categoría 2: Necesarias antes de Diseñar un Módulo Específico
1. **DP-01: Política de Anulación de Ventas con Devoluciones Parciales Previas**
   * *Pregunta:* Si una venta ya tiene devoluciones parciales asentadas, ¿cómo debe proceder el Administrador si requiere cancelarla?
   * *Propuesta Técnica:* **Bloquear** la cancelación de la venta original si tiene devoluciones registradas, exigiendo primero anular o resolver documentalmente dichas devoluciones para evitar doble reintegro de stock.
   * *Estado:* Propuesta lista para aprobación.

2. **DP-02: Impacto en el Costo Promedio Ponderado al Anular una Compra**
   * *Pregunta:* Si una compra se anula días después y ya se realizaron otras operaciones, ¿se debe recalcular el CPP hacia atrás o registrar el ajuste al CPP vigente?
   * *Propuesta Técnica:* Si el stock lo permite, la cancelación de compra descuenta las unidades al costo de dicha compra y reajusta el CPP según el saldo remanente.
   * *Estado:* Propuesta técnica documentada para la Etapa de Diseño.

### Categoría 3: Resolubles Durante el Diseño o la Implementación
1. **DP-03: Precisión Numérica y Manejo de Fracciones**
   * *Pregunta:* ¿Qué escala decimal se utilizará en base de datos para cantidades de productos fraccionables (ej. alambre, tubería por metros)?
   * *Propuesta Técnica:* Configurar cantidades físicas con tipo `DECIMAL(12, 3)` (hasta 3 decimales, ej. 1.250 metros) y valores monetarios en COP con `DECIMAL(15, 2)`.
   * *Estado:* Resuelto como propuesta técnica en diseño.

2. **DP-04: Estrategia de Concurrencia en Inventario**
   * *Pregunta:* ¿Bloqueo pesimista mediante `SELECT ... FOR UPDATE` o bloqueo optimista `@Version`?
   * *Propuesta Técnica:* Bloqueo pesimista en el registro de la venta para garantizar la reserva de stock atómica e inmediata sin generar reintentos fallidos en el mostrador.
   * *Estado:* A formalizar en la Etapa 2.

### Categoría 4: Mejoras Opcionales para una Versión Posterior
1. **DP-05: Soporte para Métodos de Pago Electrónicos (Transferencias / QR)**
   * Queda explícitamente fuera del alcance inicial (solo efectivo), pero se propone diseñar la tabla de pagos con un campo extensible `metodo_pago` que permita incorporar transferencias bancarias en una versión futura.
