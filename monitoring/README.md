# Monitoreo y pruebas de carga - Arte Andino

| Herramienta | Para qué | Dónde |
|---|---|---|
| **Micrometer + Actuator** | Cada servicio expone sus métricas (`/actuator/prometheus`, puerto 8081 interno) | dentro de los 9 servicios |
| **Prometheus** | Recolecta las métricas cada 10 s y las guarda 7 días | http://localhost:9090 |
| **Grafana** | Dashboard de rendimiento | http://localhost:3000 |
| **JMeter** | Pruebas de carga contra el API Gateway | `docker compose --profile loadtest run --rm jmeter` |

## 1. Levantar el monitoreo
```bash
docker compose up --build -d
```
Grafana: usuario `admin`, contraseña `GRAFANA_ADMIN_PASSWORD` del `.env` (por defecto `admin`).
El dashboard **Arte Andino - Rendimiento de microservicios** ya viene cargado
(carpeta *Arte Andino*). Prometheus > Status > Targets debe mostrar los 9 servicios en `UP`.

## 2. Prueba de carga
1. En el `.env` pon una cuenta real: `LOAD_EMAIL` y `LOAD_PASSWORD`.
2. Ejecuta: `docker compose --profile loadtest run --rm jmeter`
3. Mientras corre, mira Grafana (refresca cada 10 s).
4. Al terminar, el informe HTML queda en `jmeter/resultados/reporte/index.html`.

Cada usuario virtual hace login una vez y repite: categorías, productos y perfil (con JWT).
Solo lee datos: no crea pedidos ni modifica nada.

Cambiar la intensidad (en el `.env`): `LOAD_USERS`, `LOAD_RAMP`, `LOAD_DURATION`.

## 3. Medir la capacidad
Repite la prueba subiendo los usuarios y anota los resultados:

| Usuarios | Peticiones/s | Latencia p95 | Errores 5xx | CPU máx. | Conexiones BD en espera |
|---|---|---|---|---|---|
| 10 | | | | | |
| 50 | | | | | |
| 100 | | | | | |
| 200 | | | | | |

**Capacidad** = mayor número de usuarios con p95 < 1 s y errores < 1 %.
Cuando algo se degrada, el dashboard indica el cuello de botella: CPU alta (falta procesamiento),
conexiones en espera (pool de BD saturado), heap/hilos creciendo (memoria o hilos).

## Notas
- Prometheus solo escucha en `127.0.0.1:9090`; en la VM de Oracle entra con un túnel SSH
  (`ssh -L 9090:localhost:9090 usuario@IP`). Para ver Grafana desde fuera abre el puerto 3000
  en la Security List y **cambia la contraseña por defecto**.
- Las métricas usan el puerto 8081 de cada contenedor, que NO se publica fuera de Docker.
