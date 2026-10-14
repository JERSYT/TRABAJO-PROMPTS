# Documento: Registro de Riesgos y Bloqueos de Preparación

## Metadatos
* **Nombre:** 03_registro_riesgos_y_bloqueos.md
* **Descripción:** Identificación de riesgos técnicos durante la fase de scaffolding e inspección del entorno local (Java, Maven, Node, MySQL).
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Ingeniero Senior & Líder Técnico)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Estado Real del Entorno Técnico Local
Tras la auditoría inicial de herramientas en la máquina local se identificó:
* **Java:** Eclipse Temurin OpenJDK 21 LTS instalado exitosamente y verificado (`openjdk version "21.0.12.1"`).
* **Maven:** Apache Maven 3.9.9 instalado exitosamente y configurado en el PATH (`mvn 3.9.9`).
* **Node.js y npm:** Node.js v24.19.0 y npm 11.17.0 disponibles y funcionales.
* **Git:** Git versión 2.55.0 disponible.
* **MySQL:** MySQL Server 8.0 instalado (`mysql.exe Ver 8.0.46`). El servicio Windows `MySQL80` se encuentra actualmente detenido en la máquina anfitriona.

---

## 2. Registro de Riesgos y Bloqueos de la Etapa

| ID | Riesgo / Bloqueo | Causa | Impacto | Tareas Afectadas | Estrategia de Mitigación / Siguiente Acción |
| :--- | :--- | :--- | :---: | :--- | :--- |
| **RB-01** | Servicio MySQL local detenido. | El servicio `MySQL80` no tiene arranque automático o requiere privilegios administrativos para iniciar. | Medio | Pruebas de integración directa contra base real en local durante esta etapa. | Para la preparación técnica del backend (Etapa 3), se utiliza el perfil `test` con base de datos en memoria H2 para compilar y probar el arranque del contexto Spring y el endpoint de salud sin dependencias externas bloqueantes. La conexión real con MySQL se activa y verifica formalmente en la Etapa 4 al aplicar los scripts de esquema. |
| **RB-02** | Conflicto de cookies SameSite en dominios cruzados. | Frontend y backend correrán en puertos distintos en desarrollo local (`5173` y `8080`). | Alto | Autenticación y conservación de sesión. | Configurar `SameSite=Lax` y `withCredentials=true` en Axios, garantizando que el navegador remita la cookie `JSESSIONID` en peticiones originadas desde el frontend local. |
| **RB-03** | Bloqueo por falta de token CSRF en peticiones POST. | Spring Security bloquea por defecto cualquier petición POST/PUT/DELETE que carezca de token CSRF. | Alto | Formularios de login y operaciones mutativas. | Configurar `CookieCsrfTokenRepository.withHttpOnlyFalse()` en backend y un interceptor de Axios en frontend que lea la cookie `XSRF-TOKEN` y la inyecte en la cabecera `X-XSRF-TOKEN`. |
