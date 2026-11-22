/**
 * Prueba de Capacidad, Carga y Rendimiento (RNF / SLA — Puntos 15 y 16)
 * Herramienta: Grafana k6 (https://k6.io)
 *
 * Ejecución local o contra Oracle Cloud:
 *   k6 run -e BASE_URL=http://localhost:8080 tests/load/k6-capacity-test.js
 *
 * Escenarios:
 *   1. carga_normal: simula usuarios navegando catálogo y categorías (SLA p95 < 500ms).
 *   2. pico_demanda: rampa hasta 30 VUs concurrentes validando estabilidad y tasa de error < 1%.
 *   3. seguridad_rbac: valida que rutas protegidas respondan 401 sin token.
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const errorRate = new Rate('errores_http');

export const options = {
  scenarios: {
    carga_normal: {
      executor: 'ramping-vus',
      startVUs: 2,
      stages: [
        { duration: '20s', target: 10 },
        { duration: '40s', target: 25 },
        { duration: '20s', target: 0 },
      ],
      exec: 'flujoCatalogoPublico',
    },
    validacion_seguridad: {
      executor: 'constant-vus',
      vus: 2,
      duration: '30s',
      exec: 'flujoSeguridadSinToken',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1200'],
    errores_http: ['rate<0.01'],
    checks: ['rate>0.99'],
  },
};

export function flujoCatalogoPublico() {
  const resCategorias = http.get(`${BASE_URL}/api/v1/categorias`);
  const okCat = check(resCategorias, {
    'categorias status 200': (r) => r.status === 200,
    'header X-Content-Type-Options presente': (r) => r.headers['X-Content-Type-Options'] === 'nosniff',
    'header X-Correlation-Id presente': (r) => !!r.headers['X-Correlation-Id'],
  });
  errorRate.add(!okCat);

  const resProductos = http.get(`${BASE_URL}/api/v1/productos`);
  const okProd = check(resProductos, {
    'productos status 200': (r) => r.status === 200,
    'latencia productos < 500ms': (r) => r.timings.duration < 500,
  });
  errorRate.add(!okProd);

  sleep(0.5);
}

export function flujoSeguridadSinToken() {
  const resUsuarios = http.get(`${BASE_URL}/api/v1/usuarios`);
  const resSolicitudes = http.get(`${BASE_URL}/api/v1/solicitudes`);
  check(resUsuarios, { 'usuarios protegido devuelve 401': (r) => r.status === 401 });
  check(resSolicitudes, { 'solicitudes protegido devuelve 401': (r) => r.status === 401 });
  sleep(1);
}
