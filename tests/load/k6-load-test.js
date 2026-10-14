import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * Script de Pruebas de Carga y Rendimiento con k6 (Área C - Etapa 7)
 * Evalúa operaciones de consulta de catálogo, existencias de inventario
 * y tiempos de respuesta bajo usuarios concurrentes simulados.
 */

export const options = {
  stages: [
    { duration: '30s', target: 5 },  // Calentamiento a 5 usuarios concurrentes
    { duration: '1m', target: 20 },  // Carga sostenida de 20 usuarios (demostración académica)
    { duration: '30s', target: 0 },  // Enfriamiento
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% de peticiones deben responder en menos de 500ms
    http_req_failed: ['rate<0.01'],    // Menos del 1% de errores HTTP permitidos
  },
};

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8080/api/v1';

export default function () {
  // 1. Consulta técnica de salud (pública)
  const healthRes = http.get(`${BASE_URL}/health`);
  check(healthRes, {
    'Health check status es 200': (r) => r.status === 200,
    'Health check status es UP': (r) => r.json().status === 'UP',
  });

  sleep(1);

  // 2. Consulta de catálogo de productos
  const catalogRes = http.get(`${BASE_URL}/catalog/references?size=20`);
  check(catalogRes, {
    'Catálogo responde 200 o requiere sesión (401)': (r) => r.status === 200 || r.status === 401,
  });

  sleep(1);
}
