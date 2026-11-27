# Registro de Cambios — Backend Arte Andino

Todos los cambios relevantes del backend de microservicios se documentan bajo versionado semántico (SemVer) como parte del proceso de Control de Cambios (Avance 03).

## [3.0.0] - Entrega 03 (Gestión del Servicio, Capacidad, CI y Seguridad)
### Agregado
- Nuevo microservicio `serviciosservice` (`:8097`, `serviciosdb`) para Gestión de Solicitudes de Servicio, Información (ARCO) y Acceso con cálculo automático de SLA y Bitácora de Auditoría (`/api/v1/solicitudes/**`, `/api/v1/auditoria/**`).
- Seguridad en `apigateway`: filtro de Rate Limiting por IP (`RateLimitFilter`), cabeceras de seguridad OWASP (`SecurityHeadersFilter`) y trazabilidad con `X-Correlation-Id`.
- Observabilidad y capacidad: Spring Boot Actuator + Micrometer + Prometheus + Grafana (`--profile monitoring` en `docker-compose.yml`), caché Caffeine en categorías y script de pruebas de carga `tests/load/k6-capacity-test.js`.
- Pruebas automatizadas JUnit 5 + Mockito en los módulos del reactor y API Gateway ejecutadas en GitHub Actions sin `-DskipTests`.
- Plantilla de Control de Cambios (`.github/pull_request_template.md`), `.env.example` y script de respaldo `postgres/backup.sh`.

### Corregido y Optimizado
- Propagación de identidad verificada cuando un `ADMINISTRADOR` autenticado invoca `POST /api/v1/usuarios`.
- Cierre de brechas RBAC en `apigateway.yml` y protección anti-IDOR en `ProductoController` y `SolicitudController`.
- Eliminación del cuello de botella N+1 en `PedidoServiceImpl.listarPorArtesano` (de N×M llamadas Feign a 1 sola llamada).
- Endurecimiento de los 10 Dockerfiles con usuario no-root (`appuser`), `HEALTHCHECK` y límites de memoria JVM (`-XX:MaxRAMPercentage=75.0`).

## [2.0.0] - Entrega 02 (Back-end y Despliegue)
- Arquitectura de 6 microservicios Spring Boot + Config Server + Eureka + API Gateway + PostgreSQL 16 en Docker Compose y CI/CD en Oracle Cloud.
