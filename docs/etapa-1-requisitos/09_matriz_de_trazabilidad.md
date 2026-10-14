# Documento: Matriz de Trazabilidad de Requisitos

## Metadatos
* **Nombre:** 09_matriz_de_trazabilidad.md
* **Descripción:** Matriz integral de trazabilidad bidireccional que relaciona objetivos del negocio, requisitos funcionales, requisitos de calidad, reglas de negocio, casos de uso y criterios de aceptación.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Ingeniero de Requisitos y Sistemas)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Matriz de Trazabilidad Integral

| Objetivo de Negocio | Requisito Funcional (RF) | Requisito de Calidad (RNF) | Regla de Negocio (RN) | Caso de Uso (CU) | Criterio de Aceptación (CA) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **OBJ-01:** Catálogo centralizado y estructurado | **RF-CAT-001** (Registro referencias) | **RNF-AF-001** (Completitud) | **RN-02**, **RN-10** | **CU-01** | **CA-SC-006** |
| **OBJ-01:** Catálogo centralizado y estructurado | **RF-CAT-002** (Desactivación lógica) | **RNF-AF-001** | **RN-02** | **CU-01** | **CA-SC-006** |
| **OBJ-01:** Precios auditados | **RF-CAT-003** (Modificación precios) | **RNF-SEG-003** (Auditoría) | **RN-09**, **RN-11** | **CU-09** | **CA-SC-009** |
| **OBJ-02:** Integridad y consistencia de stock | **RF-INV-001** (Consulta stock real) | **RNF-DES-001** (Tiempo <500ms) | **RN-01** | **CU-02** | **CA-SC-001** |
| **OBJ-02:** Integridad y consistencia de stock | **RF-INV-002** (Historial Kárdex) | **RNF-FIA-001** (Atomicidad) | **RN-01**, **RN-04** | **CU-02** | **CA-SC-002** |
| **OBJ-02:** Integridad y consistencia de stock | **RF-INV-003** (Inventario inicial) | **RNF-AF-001** | **RN-04**, **RN-08** | **CU-03** | **CA-SC-005** |
| **OBJ-02:** Prevención de stock negativo | **RF-INV-004** (Anti-stock negativo) | **RNF-FIA-002** (Concurrencia) | **RN-01** | **CU-05** | **CA-SC-001** |
| **OBJ-03:** Valoración financiera (CPP) | **RF-COM-001** (Compras completas) | **RNF-FIA-001** (Atomicidad) | **RN-03**, **RN-10** | **CU-04** | **CA-SC-004** |
| **OBJ-03:** Valoración financiera (CPP) | **RF-COM-002** (Cálculo CPP) | **RNF-AF-002** (Precisión cálculo) | **RN-04** | **CU-04** | **CA-SC-005** |
| **OBJ-03:** Reversión segura de operaciones | **RF-COM-003** (Cancelar compra) | **RNF-FIA-001** (Atomicidad) | **RN-03**, **RN-08** | **CU-10** | **CA-SC-004** |
| **OBJ-04:** Ventas de contado y cobro ágil | **RF-VEN-001** (Ventas con cliente) | **RNF-FIA-001**, **RNF-FIA-002** | **RN-01**, **RN-05**, **RN-06**| **CU-05** | **CA-SC-002** |
| **OBJ-04:** Ventas de contado y cobro ágil | **RF-VEN-002** (Cancelar venta) | **RNF-FIA-001** | **RN-01**, **RN-08** | **CU-10** | **CA-SC-008** |
| **OBJ-04:** Devoluciones con reincorporación | **RF-DEV-001** (Devolución vinculada)| **RNF-AF-001** | **RN-04**, **RN-07** | **CU-07** | **CA-SC-007** |
| **OBJ-04:** Devoluciones con reincorporación | **RF-DEV-002** (Devolución libre) | **RNF-AF-001** | **RN-04**, **RN-07** | **CU-07** | **CA-SC-007** |
| **OBJ-05:** Control y supervisión de stock | **RF-AJU-001** (Ajustes manuales) | **RNF-SEG-002** (RBAC) | **RN-08**, **RN-11** | **CU-08** | **CA-SC-008** |
| **OBJ-05:** Gestión unificada de terceros | **RF-TER-001** (Clientes/Proveedores)| **RNF-AF-001** | **RN-05**, **RN-10** | **CU-06** | **CA-SC-002** |
| **OBJ-06:** Comprobantes y trazabilidad fiscal| **RF-DOC-001** (Consecutivos) | **RNF-AF-001** | **RN-12** | **CU-12** | **CA-SC-010** |
| **OBJ-06:** Comprobantes y trazabilidad fiscal| **RF-DOC-002** (Impresión comprobante)|**RNF-COM-001** (Compatibilidad) | **RN-12** | **CU-12** | **CA-SC-010** |
| **OBJ-05:** Seguridad y control de acceso | **RF-SEG-001** (Autenticación sesión)| **RNF-SEG-001**, **RNF-SEG-002**| **RN-11** | N/A (Transversal)| **CA-SC-003** |
| **OBJ-05:** Seguridad y control de acceso | **RF-SEG-002** (Control RBAC) | **RNF-SEG-002** | **RN-08**, **RN-11** | **CU-08** | **CA-SC-003** |
| **OBJ-05:** Auditoría inmutable | **RF-SEG-003** (Bitácora auditoría) | **RNF-SEG-003** | **RN-09**, **RN-11** | **CU-11** | **CA-SC-009** |

---

## 2. Cobertura de Requisitos y Conclusiones de Trazabilidad
* **100% de los requisitos funcionales** están vinculados a objetivos del negocio y a las reglas de negocio confirmadas.
* Cada flujo crítico posee al menos un criterio de aceptación en formato Dado/Cuando/Entonces y un caso de uso detallado.
* No existen requisitos huérfanos ni reglas de negocio sin especificación formal.
