# Documento: Procedimiento de Copias de Seguridad y Restauración

## Metadatos
* **Nombre:** 04_procedimiento_copias_seguridad.md
* **Descripción:** Procedimiento operativo estandarizado para la realización de copias de seguridad lógicas (backups), política de retención, precauciones de integridad y protocolo documentado de restauración.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Administrador de Base de Datos y DevOps)
* **Versión:** 1.0
* **Estado:** Aprobado

---

## 1. Política de Copias de Seguridad (Presupuesto $0 COP)
En el contexto del plan gratuito sin costo recurrente, se establece una política de copias de seguridad lógicas mediante la utilidad estándar `mysqldump` combinada con tareas programadas de servidor (cron jobs o scripts de mantenimiento):

* **Frecuencia Recomendada:** Respaldo diario al cierre de la jornada comercial (ej. 21:00 horas COT).
* **Política de Retención:** Conservar los últimos 7 respaldos diarios y 4 respaldos semanales (requiere < 50 MB de almacenamiento para la base de demostración).
* **Ubicación de Almacenamiento:** Directorio seguro separado del contenedor de la base de datos o almacenamiento en nube gratuito (Google Drive / GitHub Artifacts / AWS Free S3 si está disponible).
* **Protección de Credenciales:** Las contraseñas de MySQL nunca deben incluirse como texto claro en líneas de comando guardadas en historial; se utiliza un archivo de credenciales seguro `~/.my.cnf` con permisos `chmod 600`.

---

## 2. Procedimiento para Generar una Copia de Seguridad

### Script Automatizable de Respaldo (`backup_ferreteria.sh`)
```bash
#!/bin/bash
# Script de Backup Lógico para MySQL 8.0 - Sistema Ferretería
set -e

BACKUP_DIR="/var/backups/ferreteria"
FECHA=$(date +"%Y%m%d_%H%M%S")
ARCHIVO="$BACKUP_DIR/ferreteria_backup_${FECHA}.sql.gz"

mkdir -p "$BACKUP_DIR"

echo "[$(date)] Iniciando copia de seguridad de ferreteria_db..."

# mysqldump con opciones de consistencia transaccional InnoDB
mysqldump \
  --host="${DB_HOST:-localhost}" \
  --port="${DB_PORT:-3306}" \
  --user="${DB_USER:-ferreteria_user}" \
  --password="${DB_PASSWORD}" \
  --single-transaction \
  --quick \
  --routines \
  --triggers \
  --default-character-set=utf8mb4 \
  "${DB_NAME:-ferreteria_db}" | gzip > "$ARCHIVO"

# Verificar que el archivo generado no esté vacío
if [ -s "$ARCHIVO" ]; then
  echo "[$(date)] Copia de seguridad generada con éxito: $ARCHIVO ($(du -h "$ARCHIVO" | cut -f1))"
else
  echo "[$(date)] ERROR: El archivo de copia de seguridad está vacío" >&2
  exit 1
fi

# Eliminar respaldos más antiguos a 7 días para optimizar cuota
find "$BACKUP_DIR" -type f -name "ferreteria_backup_*.sql.gz" -mtime +7 -delete
echo "[$(date)] Depuración de respaldos antiguos completada."
```

---

## 3. Procedimiento Documentado de Restauración (Disaster Recovery)

*Nota Metodológica:* Conforme a la sección 11.3 de la Etapa 6 de `PROMPTS.pdf`, **en esta etapa se exige documentar el procedimiento de restauración sin ejecutar una restauración destructiva real sobre datos existentes**.

### Protocolo de Restauración Paso a Paso:

1. **Paso 1: Identificación y Verificación de Integridad de la Copia:**
   * Localizar el archivo de respaldo más reciente antes del incidente.
   * Verificar la integridad del archivo comprimido:
     ```bash
     gzip -t /var/backups/ferreteria/ferreteria_backup_YYYYMMDD_HHMMSS.sql.gz
     ```
2. **Paso 2: Aislamiento y Creación de Base de Datos de Destino:**
   * Nunca restaurar directamente sobre una base con datos sin probar primero en una base de datos de contingencia aislada (`ferreteria_restore_test`):
     ```sql
     CREATE DATABASE ferreteria_restore_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
     ```
3. **Paso 3: Descompresión e Ingesta de Datos:**
   ```bash
   gunzip -c /var/backups/ferreteria/ferreteria_backup_YYYYMMDD_HHMMSS.sql.gz | mysql -u root -p ferreteria_restore_test
   ```
4. **Paso 4: Auditoría de Integridad y Consistencia:**
   * Ejecutar consultas de validación en la base restaurada:
     ```sql
     -- Verificar que las tablas críticas existan y tengan registros
     SELECT COUNT(*) FROM inventario_saldo;
     SELECT COUNT(*) FROM inventario_movimiento;
     SELECT COUNT(*) FROM venta;
     
     -- Verificar coherencia entre saldos de inventario y movimientos
     SELECT s.referencia_id, s.cantidad_disponible, 
            COALESCE((SELECT saldo_resultante FROM inventario_movimiento m 
                      WHERE m.referencia_id = s.referencia_id 
                      ORDER BY m.fecha_hora DESC, m.id DESC LIMIT 1), 0) AS saldo_kardex
     FROM inventario_saldo s;
     ```
5. **Paso 5: Conmutación Segura:**
   * Si las pruebas en `ferreteria_restore_test` son 100% satisfactorias, renombrar o apuntar la variable de entorno `DB_NAME` a la base validada y reiniciar el backend.
