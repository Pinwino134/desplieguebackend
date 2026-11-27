# Manual de Instalación, Configuración y Operación — Arte Andino (Avance 03)

## 1. Requisitos Previos
- **Docker Engine** 24+ y **Docker Compose** v2+ (recomendado para levantar los 10 servicios + PostgreSQL + Prometheus/Grafana).
- **Java JDK 25** (Eclipse Temurin) para compilación/pruebas locales con Maven Wrapper (`mvnw.cmd` / `./mvnw`).
- **Node.js 22+** y **npm 10+** para el frontend React + Vite.
- **Grafana k6** (opcional, para pruebas de carga y capacidad RNF).

---

## 2. Configuración del Backend (`desplieguebackend`)

### 2.1 Variables de Entorno
Copia la plantilla de ejemplo y reemplaza las credenciales antes de desplegar en producción:
```bash
cp .env.example .env
```
Variables principales:
- `DB_USER` / `DB_PASSWORD`: credenciales de PostgreSQL 16 (sirve las 7 bases de datos: `authdb`, `productosdb`, `ventasdb`, `pagosdb`, `logisticadb`, `gerenciadb`, `serviciosdb`).
- `JWT_SECRET`: clave HMAC-SHA Base64 compartida entre `authservice` y `apigateway`.
- `RATE_LIMIT_ENABLED`, `RATE_LIMIT_AUTH_RPM` (defecto `25`), `RATE_LIMIT_API_RPM` (defecto `200`).
- `GRAFANA_USER` / `GRAFANA_PASSWORD`: acceso al tablero de capacidad Grafana.

### 2.2 Arranque con Docker Compose
**Modo estándar (PostgreSQL + 10 microservicios):**
```bash
docker compose up --build -d
docker compose ps
```

**Modo completo con Observabilidad (incluye Prometheus `:9090` y Grafana `:3000`):**
```bash
docker compose --profile monitoring up --build -d
```

> **Nota si ya existía el volumen de PostgreSQL antes de agregar `serviciosdb`:**
> Ejecuta una sola vez:
> ```bash
> docker exec -it postgres psql -U postgres -c "CREATE DATABASE serviciosdb;"
> ```

### 2.3 Puertos y Verificación de Salud
| Componente | Puerto | URL de Verificación |
|---|---|---|
| API Gateway (único expuesto) | `8080` | `http://localhost:8080/actuator/health` |
| Eureka Discovery (solo localhost) | `8761` | `http://127.0.0.1:8761` |
| Prometheus (perfil monitoring) | `9090` | `http://127.0.0.1:9090/targets` |
| Grafana (perfil monitoring) | `3000` | `http://localhost:3000` (Dashboard *Arte Andino — Capacidad*) |

---

## 3. Configuración del Frontend (`front-artesania`)

### 3.1 Variables de Entorno
Edita `.env` apuntando al API Gateway:
```env
VITE_API_URL=http://localhost:8080
```
*(En producción en Vercel configurar `VITE_API_URL` con la URL del gateway desplegado en Oracle Cloud).*

### 3.2 Instalación, Desarrollo y Build WPO
```bash
npm ci
npm run dev      # Desarrollo local en http://localhost:5173
npm test         # Pruebas unitarias (JWT + Matriz ITIL)
npm run lint     # Análisis estático oxlint
npm run build    # Build de producción con code splitting por rol
```

---

## 4. Ejecución de Pruebas Automatizadas (RF, Seguridad y RNF)

### 4.1 Pruebas Unitarias y de Seguridad Backend (Maven)
```bash
# Reactor completo (configserver, discoveryserver y los 7 microservicios)
./configserver/mvnw -B test

# API Gateway (JWT, RBAC, Rate Limit y Cabeceras OWASP)
cd apigateway && ./mvnw -B test
```

### 4.2 Pruebas de Capacidad y Rendimiento RNF (k6)
```bash
k6 run -e BASE_URL=http://localhost:8080 tests/load/k6-capacity-test.js
```
Umbrales verificados automáticamente: `p(95) < 500ms`, `p(99) < 1200ms`, tasa de error `< 1%`, cabeceras de seguridad presentes y HTTP `401` en endpoints protegidos sin token.

---

## 5. Respaldos y Recuperación ante Desastres (DRP)

### 5.1 Ejecutar Respaldo de las 7 Bases de Datos
```bash
./postgres/backup.sh ./backups
```
Genera archivos comprimidos `<db>_YYYYMMDD_HHMMSS.sql.gz` y purga automáticamente respaldos mayores a 14 días.

### 5.2 Restaurar en Caso de Desastre (Simulacro / Producción)
```bash
./postgres/restore.sh ./backups 20261123_020000
```
El script restaura las 7 bases de datos e imprime el tiempo total transcurrido para validar el cumplimiento del **RTO ≤ 30 min**.

---

## 6. Despliegue Continuo (CI/CD)
- **Backend:** `.github/workflows/CI-CD BACKEND.yml` ejecuta pruebas reales (sin `-DskipTests`), construye las 10 imágenes Docker, valida `docker-compose.yml` y despliega en Oracle Cloud con smoke tests de disponibilidad (`/api/v1/categorias`, `/actuator/health`) y seguridad (`/api/v1/solicitudes` sin token = `401`).
- **Frontend:** `.github/workflows/CI Frontend.yml` ejecuta `npm test`, `npm run lint`, `npm run build` con reporte de tamaño WPO y `npm audit`; Vercel despliega automáticamente con cabeceras de seguridad y caché de `vercel.json`.
