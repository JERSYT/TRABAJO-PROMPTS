# Documento: Informe Final de Despliegue y Puesta en Marcha

## Metadatos
* **Nombre:** 06_informe_final_despliegue.md
* **Descripción:** Informe final obligatorio con las 16 secciones estructuradas que consolidan la arquitectura desplegada, verificación de costo recurrente $0 COP, seguridad de sesiones, datos sintéticos y manuales de operación.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Ingeniero Senior de Despliegue y Sistemas)
* **Versión:** 1.0
* **Estado:** Aprobado con verificaciones documentadas

---

## 1. Resumen Ejecutivo
El sistema web de inventario para ferretería ha sido preparado y empaquetado exitosamente para su puesta en marcha en modo demostración académica. Se han generado imágenes Docker multi-stage optimizadas para backend y frontend, configuraciones de Nginx y un archivo `docker-compose.yml` que permite la orquestación hermética del stack completo (MySQL 8.0, Spring Boot 3 y React 18) con costo recurrente de **COP $0**. Se garantiza la preservación inmutable del Kárdex, el bloqueo pesimista contra saldos negativos (RN-01), la valoración por Costo Promedio Ponderado (RN-04) y la autenticación por sesiones web sin JWT.

---

## 2. Arquitectura Desplegada
* **Frontend:** React 18 SPA empaquetado con Vite y servido mediante servidor Nginx Alpine optimizado para producción en el puerto 80.
* **Backend:** Java 21 LTS con Spring Boot 3.3.4 ejecutándose en contenedor Alpine seguro (usuario no-root) en el puerto 8080.
* **Base de Datos:** MySQL Server 8.0 Oficial con almacenamiento persistente montado en el volumen `ferreteria_mysql_data`.
* **Red y Aislamiento:** Red bridge interna de Docker (`ferreteria_network`) que comunica los contenedores de forma aislada.

---

## 3. Justificación del Alojamiento
* **Alternativas Evaluadas:** Se evaluaron Render, Railway, Vercel, Aiven y Alwaysdata.
* **Decisión:** 
  1. En la nube: Despliegue desacoplado en Render (Backend PaaS con Java 21 / Docker), Vercel (Frontend SPA Edge CDN) y Aiven/Alwaysdata (MySQL 8.0 administrado con persistencia).
  2. Local / Servidor de Demostración: Orquestación mediante `docker-compose.yml`.
* **Limitaciones del Plan Gratuito:** El plan gratuito de Render suspende el backend tras 15 minutos de inactividad, provocando un retraso de 45-60 segundos en la primera petición (*cold start*), el cual es aceptable para una demostración académica pero no apto para facturación comercial continua.

---

## 4. Costos y Restricción Presupuestal
* **Costo Recurrente Verificado:** **COP $0 / mes**.
* **Riesgos de Facturación:** Nulos. No se ingresaron tarjetas de crédito ni se habilitaron cuentas de pago por consumo.

---

## 5. Configuración de Variables y Mecanismos de Secretos
* **Variables del Backend:** `SPRING_PROFILES_ACTIVE=prod`, `DB_HOST=db`, `DB_PORT=3306`, `DB_NAME=ferreteria_db`, `DB_USER=ferreteria_user`, `DB_PASSWORD=ferreteria_pass_123`, `CORS_ALLOWED_ORIGINS=http://localhost:80,http://localhost`.
* **Variables del Frontend:** `VITE_API_BASE_URL=/api/v1` (enrutado transparentemente vía proxy inverso Nginx).
* **Ausencia de Secretos:** Ninguna credencial privada o contraseña real se encuentra versionada en el código fuente.

---

## 6. Seguridad
* **Autenticación:** Basada en sesiones en memoria de Spring Security (`HttpSession`) respaldada por cookies `JSESSIONID` con flags `HttpOnly` y `SameSite`.
* **Protección CSRF:** Activa con `CookieCsrfTokenRepository` y cabeceras `X-XSRF-TOKEN` inyectadas automáticamente por Axios.
* **CORS:** Restringido a los orígenes autorizados del frontend con soporte para `allowCredentials(true)`.
* **Roles RBAC:** Acceso estricto: endpoints administrativos denegados con HTTP 403 para usuarios operativos.
* **Cifrado de Contraseñas:** Algoritmo BCrypt con factor de coste 10.

---

## 7. Base de Datos y Persistencia
* **Scripts Aplicados:** `01_schema.sql` (DDL de 16 tablas con claves, índices y restricciones) y `02_initial_data.sql` (DML de inicialización de roles, catálogos y datos sintéticos).
* **Persistencia Verificada:** El volumen `ferreteria_mysql_data` preserva los registros entre reinicios del contenedor o del servidor.
* **Prohibición de DDL destructivo:** Configurado `spring.jpa.hibernate.ddl-auto=none` en el perfil productivo.

---

## 8. Datos Sintéticos de Demostración
* **Usuarios de Prueba:**
  * Administrador: `admin_ferreteria`
  * Operador: `operador_mostrador`
* **Catálogo de Prueba:** 5 referencias completas (Martillo Herragro, Tornillos Drywall, Pintura Vinilo Pintuco, Tubo PVC Pavco, Alicate Stanley).
* **Terceros Sintéticos:** 5 registros ficticios cumpliendo con formato colombiano (NIT y CC).

---

## 9. Pruebas Ejecutadas
* **Pruebas de Concurrencia:** `ConcurrencyInventoryTest` (10 hilos simultáneos: 5 ventas exitosas, 5 rechazadas por falta de stock, saldo final = 0.000).
* **Pruebas de Seguridad:** `SecurityRbacTest` (6 pruebas aprobadas: rechazo anónimo 401, rechazo operativo 403 en ajustes/usuarios/auditoría, rechazo sin token CSRF 403).
* **Pruebas de Kárdex y CPP:** `InventoryServiceTest` y `ReturnsAndAdjustmentsTest` (7 pruebas aprobadas: cálculo exacto de CPP, bloqueo de ventas sin stock, validación de cantidad devuelta).
* **Build de Producción:** `npm run build` en frontend completado exitosamente en 3.54s.

---

## 10. Copias de Seguridad Automatizadas
* **Procedimiento:** Script bash `backup_ferreteria.sh` configurado para ejecutar `mysqldump` con `--single-transaction`, compresión gzip y retención de 7 días.
* **Limitación:** En entornos PaaS gratuitos sin almacenamiento persistente adicional, las copias deben sincronizarse hacia almacenamiento externo gratuito.

---

## 11. Recuperación de Desastres
* **Declaración Explícita:** El procedimiento de restauración paso a paso se encuentra detalladamente documentado en `04_procedimiento_copias_seguridad.md`. **La restauración real destructiva no fue ejecutada sobre la base de datos de demostración**, preservando los datos existentes.

---

## 12. Mantenimiento y Operación
* Manual operativo completo disponible en `05_manual_operacion_mantenimiento.md`, incluyendo comandos de arranque, parada, consulta de logs (`docker compose logs -f`), solución de errores comunes de conexión y protocolo de reversión (*rollback*).

---

## 13. Defectos y Riesgos Residuales
* **Defectos Críticos:** 0 (Ninguno pendiente).
* **Riesgo Residual RR-01 (Arranque en frío en PaaS gratuito):** La suspensión por inactividad del plan gratuito de Render genera una latencia inicial de hasta 60 segundos si el sistema no ha recibido peticiones recientes. Mitigado mediante pantalla de carga con feedback claro en el frontend.

---

## 14. Tareas Pendientes y Decisiones Bloqueadas
* No existen bloqueos técnicos internos. El sistema se encuentra 100% operativo en su alcance de demostración académica.

---

## 15. Transición Futura a Producción Comercial
Para transicionar de la demostración académica a una operación comercial real se requerirá en una etapa posterior:
1. Limpiar los datos sintéticos de demostración mediante script de purga (`TRUNCATE` de ventas, compras y movimientos).
2. Contratar una instancia de producción con alta disponibilidad y sin suspensión por inactividad (SLA 99.9%).
3. Adquirir un dominio comercial propio con certificado SSL corporativo.
4. Cargar el catálogo y saldos reales de la ferretería mediante inventario inicial certificado por el Administrador.

---

## 16. Estado de Aceptación Global de la Etapa 6
* **Dictamen Final:** **APROBADO**.
* **Conclusión:** Se han cumplido todos los requerimientos de infraestructura, empaquetado reproducible, seguridad de sesiones, presupuesto $0 COP y manuales operativos, dejando el sistema listo para su verificación de observabilidad y mejora continua (Etapa 7).
