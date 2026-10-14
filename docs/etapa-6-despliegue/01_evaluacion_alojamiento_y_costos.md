# Documento: Evaluación de Plataformas de Alojamiento y Restricción Presupuestal

## Metadatos
* **Nombre:** 01_evaluacion_alojamiento_y_costos.md
* **Descripción:** Análisis técnico comparativo de plataformas de alojamiento administrado gratuitas (PaaS/Cloud), evaluación de costos recurrentes ($0 COP) y viabilidad de Docker y dominios para la demostración académica del sistema de inventario.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Ingeniero DevOps y Especialista en Infraestructura Cloud)
* **Versión:** 1.0
* **Estado:** Aprobado

---

## 1. Restricción Fundamental de Presupuesto: COP $0 Recurrentes
El presupuesto aprobado para la infraestructura de alojamiento del sistema de inventario es estrictamente de **COP $0 recurrentes**.

**Reglas de cumplimiento:**
1. Queda prohibida la contratación de servicios de pago o el ingreso de datos de tarjetas de crédito que puedan generar cobros por sobregiro o cargos imprevistos.
2. No se adquirirán dominios comerciales pagos (`.com`, `.co`, `.com.co`); se utilizarán los subdominios HTTPS gratuitos provistos de fábrica por las plataformas administradas (ej. `*.onrender.com`, `*.vercel.app`).
3. La puesta en marcha es exclusivamente para demostración académica y validación técnica, no para producción comercial real.

---

## 2. Evaluación Comparativa de Proveedores PaaS Gratuitos

### 2.1 Backend (Java 21 / Spring Boot 3)

| Plataforma | Soporte Java 21 / Docker | Plan Gratuito Real | Límites de CPU / RAM | Comportamiento ante Inactividad | Dictamen Técnico |
| :--- | :---: | :---: | :--- | :--- | :--- |
| **Render** | Nativo y Docker | Sí (Free Web Service) | 0.1 vCPU / 512 MB RAM | Se suspende tras 15 minutos sin peticiones. Tiempo de arranque en frío (*cold start*) de 45-60s. | **Recomendada:** Compatible con Docker multi-stage, HTTPS gratuito, variables de entorno seguras. |
| **Railway** | Docker / Nixpacks | Prueba de crédito $5 | 1 vCPU / 512 MB RAM | No ofrece plan gratuito indefinido (expira al agotar los $5 de crédito). | **Descartada:** Viola la restricción de costo $0 recurrente sostenido. |
| **Koyeb** | Docker | Sí (Free Nano instance) | 0.1 vCPU / 512 MB RAM | Permite 1 servicio web gratuito continuo sin suspensión rápida. | **Alternativa Viable:** Alta disponibilidad para demostración. |

---

### 2.2 Base de Datos Relacional (MySQL 8.0)

| Proveedor | Versión MySQL | Límite Gratuito | Persistencia y Respaldos | Conectividad TLS | Dictamen Técnico |
| :--- | :---: | :--- | :--- | :---: | :--- |
| **Aiven for MySQL** | MySQL 8.0 | Free Tier (1 servicio) | Persistencia garantizada en disco SSD. Respaldos diarios incluidos. | Sí (SSL/TLS obligatorio) | **Recomendada:** Excelente estabilidad, compatible con Spring Data JPA y scripts manuales. |
| **Alwaysdata** | MySQL 8.0 | 100 MB almacenamiento | Persistencia en disco, administración phpMyAdmin y consola. | Sí | **Alternativa Viable:** Suficiente para demostración sintética (<10 MB). |
| **Clever Cloud** | MySQL 8.0 | Add-on gratuito (5 MB) | Persistencia real en disco. | Sí | **Descartada:** 5 MB es un límite excesivamente restrictivo para esquemas y auditoría. |

---

### 2.3 Frontend (React 18 + SPA + Vite)

| Plataforma | Soporte React / Vite | Plan Gratuito | SSL / HTTPS Automático | Despliegue Git Continuo | Dictamen Técnico |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Vercel** | Nativo (Node / Vite) | Ilimitado para proyectos no comerciales | Sí (Certificado Let's Encrypt) | Sí (GitHub integration) | **Recomendada:** Tiempos de carga ultrarrápidos (Edge CDN global) y configuración de reescritura SPA. |
| **Render Static Site** | Nativo (Vite) | Sí (Gratuito ilimitado) | Sí | Sí | **Alternativa Viable:** Centraliza frontend y backend bajo un mismo proveedor. |

---

## 3. Justificación de Decisiones Técnicas

### 3.1 Evaluación de Docker y Docker Compose
* **Decisión:** **Adoptado para reproducibilidad y empaquetado hermético.**
* **Justificación:** Se construyeron `Dockerfile` optimizados multi-stage (`eclipse-temurin:21-jre-alpine` para backend y `nginx:alpine` para frontend) y un archivo `docker-compose.yml` que orquesta la base de datos MySQL 8.0, el backend y el frontend. 
* Esto permite dos modalidades de despliegue $0:
  1. Despliegue en la nube mediante PaaS gratuitos (Render / Vercel / Aiven).
  2. Despliegue local o en servidor de demostración con un solo comando (`docker compose up -d`) sin requerir instalaciones previas de JDK o Node en la máquina de evaluación.

### 3.2 Evaluación de Compra de Dominio
* **Decisión:** **No se adquiere dominio propio.**
* **Justificación:** Cumple al 100% con la restricción presupuestal de COP $0. Los proveedores seleccionados emiten certificados TLS/HTTPS automáticos y válidos sobre sus subdominios institucionales (`*.onrender.com` y `*.vercel.app`), garantizando la seguridad en el transporte y la protección de las cookies de sesión con el atributo `Secure`.
