# Documento: Registro de Decisiones Técnicas y Arquitectónicas (ADRs)

## Metadatos
* **Nombre:** 09_registro_decisiones_tecnicas.md
* **Descripción:** Registro formal de decisiones de arquitectura (ADRs), justificaciones, alternativas descartadas, consecuencias técnicas y estado de aprobación para el sistema de inventario.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Arquitecto de Software)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. ADR-01: Arquitectura Tradicional por Capas en Backend
* **Decisión:** Adoptar una arquitectura por capas tradicional (`Controller` → `Service` → `Repository` → `Database`).
* **Contexto:** Se requiere un sistema web mantenible y robusto para una ferretería de sede única, con alcance académico riguroso pero sin sobredimensionamiento empresarial.
* **Alternativas Descartadas:**
  * *Arquitectura Hexagonal / Clean Architecture:* Descartada por introducir complejidad accidental desproporcionada (puertos, adaptadores, múltiples mappers redundantes) para una aplicación monolítica enfocada en inventario.
  * *Microservicios:* Descartada expresamente en el alcance aprobado por generar sobrecostos de red, transacciones distribuidas complejas y costo de infraestructura > $0.
* **Consecuencias:**
  * *Positivas:* Claridad organizativa, facilidad para pruebas con Spring Boot Test, desarrollo directo y trazabilidad inmediata.
  * *Negativas:* Requiere disciplina para evitar que los controladores ejecuten lógica de negocio o que los servicios devuelvan objetos HTTP.
* **Estado:** **Confirmada (Aprobada)**.

---

## 2. ADR-02: Separación Estricta en Dos Repositorios
* **Decisión:** Mantener el backend Spring Boot (`backend/`) y el frontend React (`frontend/`) en dos carpetas y repositorios Git separados.
* **Contexto:** Se busca independencia en los ciclos de vida, dependencias y despliegue de la API y de la SPA.
* **Alternativas Descartadas:**
  * *Monorepo unificado con herramientas tipo Nx/Turborepo:* Descartada por mezclar ecosistemas dispares (Maven y NPM) y aumentar la fricción de configuración.
* **Consecuencias:** Requiere una configuración explícita de CORS, orígenes confiables y compartición segura de cookies de sesión entre ambos proyectos.
* **Estado:** **Confirmada (Aprobada)**.

---

## 3. ADR-03: Motor de Base de Datos MySQL 8.0 y Gestión Manual de Scripts
* **Decisión:** Utilizar MySQL 8.0 como sistema gestor de base de datos relacional y gestionar el esquema mediante scripts SQL versionados (`01_schema.sql`, etc.), sin Flyway ni Liquibase.
* **Contexto:** Demostración académica pequeña con presupuesto $0, donde se prioriza la inspección transparente de las sentencias DDL y DML.
* **Alternativas Descartadas:**
  * *Flyway / Liquibase:* Descartadas explícitamente en el contexto del proyecto para evitar dependencias adicionales y riesgos de bloqueos de migración automática.
  * *Generación automática con `spring.jpa.hibernate.ddl-auto=update`:* Prohibida rotundamente por el riesgo inminente de mutaciones imprevistas o corrupción del esquema en bases con datos persistentes.
* **Consecuencias:** Los cambios de esquema se revisan y ejecutan manualmente en orden controlado, documentando cada intervención en un registro de scripts.
* **Estado:** **Confirmada (Aprobada)**.

---

## 4. ADR-04: Autenticación por Sesiones Web con Spring Security (No JWT)
* **Decisión:** Utilizar el mecanismo de sesiones de Spring Security (`HttpSession`) respaldado por cookies `JSESSIONID` con flags `HttpOnly` y `SameSite`. Prohibir expresamente JWT.
* **Contexto:** Es una aplicación para mostrador y bodega en una única sede física. Las sesiones permiten invalidación instantánea en el servidor ante cierre de sesión y eliminan la vulnerabilidad de robo de tokens almacenados en `localStorage`.
* **Alternativas Descartadas:**
  * *JSON Web Tokens (JWT):* Descartada por requerir gestión compleja de listas negras de revocación en memoria o almacenamiento inseguro en el cliente navegador para un sistema cerrado de sede única.
* **Consecuencias:** Requiere activar `allowCredentials(true)` en CORS y gestionar la protección CSRF en el cliente React para solicitudes mutativas.
* **Estado:** **Confirmada (Aprobada)**.

---

## 5. ADR-05: Manejo Estandarizado de Errores con ProblemDetail (RFC 9457)
* **Decisión:** Implementar el estándar IETF RFC 9457 (`application/problem+json`) mediante la clase `ProblemDetail` de Spring Framework en un manejador transversal `@RestControllerAdvice`.
* **Contexto:** La API debe brindar mensajes de error consistentes, claros y estructurados sin inventar envolturas propietarias no estándar.
* **Alternativas Descartadas:**
  * *Envoltorio universal `ApiResponse<T>`:* Descartada explícitamente por violar los contratos semánticos REST puros.
* **Consecuencias:** Respuestas de error uniformes con campos `type`, `title`, `status`, `detail`, `instance` e `invalidParams`.
* **Estado:** **Confirmada (Aprobada)**.

---

## 6. ADR-06: Modelo de Kárdex Inmutable y Saldo Atómico
* **Decisión:** Separar la persistencia en dos tablas: `inventario_saldo` (un registro por referencia con la cantidad actual y CPP) e `inventario_movimiento` (Kárdex cronológico inmutable de solo inserción).
* **Contexto:** Es imprescindible conocer el stock actual en milisegundos para validar ventas de mostrador y, simultáneamente, contar con una bitácora forense de cada unidad que entra o sale.
* **Alternativas Descartadas:**
  * *Calcular el saldo en tiempo real sumando todos los movimientos históricos con `SUM(cantidad)`:* Descartada por degradar el rendimiento a medida que crece el volumen de transacciones de mostrador.
  * *Actualizar únicamente una columna de stock sin guardar movimientos:* Descartada por violar la regla de auditoría y trazabilidad exigida por el negocio.
* **Consecuencias:** Ambas tablas deben actualizarse en la misma transacción `@Transactional`. Si una falla, se hace rollback completo.
* **Estado:** **Confirmada (Aprobada)**.

---

## 7. ADR-07: Control de Concurrencia contra Stock Negativo
* **Decisión:** Aplicar bloqueo pesimista en base de datos (`SELECT ... FOR UPDATE` en JPA mediante `@Lock(LockModeType.PESSIMISTIC_WRITE)`) al descontar existencias durante una venta.
* **Contexto:** En mostrador, dos ventas simultáneas de las últimas unidades disponibles no deben generar existencias negativas (RN-01).
* **Alternativas Descartadas:**
  * *Bloqueo optimista exclusivo (`@Version`):* Descartada como mecanismo único de venta de mostrador, pues arrojaría excepciones de versión al segundo cajero obligándolo a reiniciar la venta manualmente en lugar de serializar ordenadamente la solicitud.
* **Consecuencias:** Garantiza atomicidad absoluta y consistencia estricta del inventario en escenarios concurrentes.
* **Estado:** **Confirmada (Aprobada)**.

---

## 8. ADR-08: Valoración Financiera con Costo Promedio Ponderado (CPP)
* **Decisión:** Recalcular el CPP unitario de una referencia tras cada compra registrada y congelar dicho costo en el atributo `costo_historico_unitario` de cada línea de venta efectuada.
* **Contexto:** Permite conocer con precisión el margen de beneficio de cada venta sin incurrir en la sobreingeniería de asignar números de lote específicos a cada clavo o tornillo despachado.
* **Consecuencias:** En las devoluciones vinculadas, la mercancía reingresa valorada con el costo histórico registrado en esa venta específica, preservando la exactitud del balance.
* **Estado:** **Confirmada (Aprobada)**.
