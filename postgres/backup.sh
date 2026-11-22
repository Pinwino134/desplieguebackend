#!/bin/sh
# Script de respaldo automatizado de las 7 bases de datos PostgreSQL.
# Uso: ./postgres/backup.sh [directorio_destino]
set -euo pipefail

DEST_DIR="${1:-./backups}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
mkdir -p "$DEST_DIR"

for db in authdb productosdb ventasdb pagosdb logisticadb gerenciadb serviciosdb; do
  FILE="$DEST_DIR/${db}_${TIMESTAMP}.sql.gz"
  echo "Respaldando $db -> $FILE"
  docker exec postgres pg_dump -U "${DB_USER:-postgres}" "$db" | gzip > "$FILE"
done

# Retención: elimina respaldos mayores a 14 días
find "$DEST_DIR" -name "*.sql.gz" -mtime +14 -delete
echo "Respaldo completado en $DEST_DIR"
