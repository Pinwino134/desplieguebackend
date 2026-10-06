#!/bin/sh
# Se ejecuta UNA sola vez, la primera vez que se crea el volumen de datos
# de Postgres. Si ya tienes el volumen creado de antes, esto no se vuelve
# a correr (borra el volumen con "docker compose down -v" si necesitas
# resetear todo desde cero).
set -e

for db in authdb productosdb ventasdb pagosdb logisticadb gerenciadb; do
  echo "Creando base de datos: $db"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE $db;
EOSQL
done
