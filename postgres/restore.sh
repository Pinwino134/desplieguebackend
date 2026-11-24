#!/bin/sh
# =========================================================================
# Script de Recuperación ante Desastres (DRP) — Restauración de PostgreSQL
# Uso: ./postgres/restore.sh <directorio_backups> <timestamp_YYYYMMDD_HHMMSS>
# Ejemplo: ./postgres/restore.sh ./backups 20261123_020000
# =========================================================================
set -euo pipefail

BACKUP_DIR="${1:-./backups}"
TIMESTAMP="${2:-}"

if [ -z "$TIMESTAMP" ]; then
  echo "Error: especifica el timestamp del respaldo a restaurar."
  echo "Uso: ./postgres/restore.sh <directorio_backups> <YYYYMMDD_HHMMSS>"
  exit 1
fi

START_TIME=$(date +%s)
echo "=== INICIANDO PROCEDIMIENTO DRP DE RESTAURACIÓN ($TIMESTAMP) ==="

for db in authdb productosdb ventasdb pagosdb logisticadb gerenciadb serviciosdb; do
  FILE="$BACKUP_DIR/${db}_${TIMESTAMP}.sql.gz"
  if [ ! -f "$FILE" ]; then
    echo "[ADVERTENCIA] No se encontró $FILE, omitiendo $db."
    continue
  fi
  echo "Restaurando base de datos $db desde $FILE..."
  docker exec -i postgres psql -U "${DB_USER:-postgres}" -c "DROP DATABASE IF EXISTS ${db} WITH (FORCE);"
  docker exec -i postgres psql -U "${DB_USER:-postgres}" -c "CREATE DATABASE ${db};"
  gunzip -c "$FILE" | docker exec -i postgres psql -U "${DB_USER:-postgres}" -d "$db" > /dev/null
  echo "[OK] $db restaurada."
done

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))
echo "=== RESTAURACIÓN COMPLETADA EN ${ELAPSED} SEGUNDOS (RTO objetivo: <= 1800s) ==="
