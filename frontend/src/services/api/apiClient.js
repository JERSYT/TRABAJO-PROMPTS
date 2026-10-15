import axios from 'axios';

/**
 * Cliente HTTP centralizado con soporte obligatorio para:
 * - Sesiones de servidor mediante cookies (withCredentials = true)
 * - Inyección automática de token CSRF (X-XSRF-TOKEN) con bootstrap explícito
 * - Manejo consistente de errores ProblemDetail (RFC 9457)
 */
let rawBaseURL = import.meta.env.VITE_API_BASE_URL || '/api/v1';
rawBaseURL = rawBaseURL.replace(/\/+$/, '');
if (!rawBaseURL.endsWith('/api/v1')) {
  rawBaseURL = `${rawBaseURL}/api/v1`;
}
const baseURL = rawBaseURL;

const apiClient = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json, application/problem+json',
  },
});

// Función auxiliar para extraer el valor de la cookie XSRF-TOKEN
const getCsrfTokenFromCookie = () => {
  if (typeof document === 'undefined') return null;
  const match = document.cookie.match(new RegExp('(^|;\\s*)XSRF-TOKEN=([^;]*)'));
  return match ? decodeURIComponent(match[2]) : null;
};

// Bootstrap explícito: si no hay cookie CSRF, pedirla al backend.
// El endpoint GET /auth/csrf pasa por el filtro CSRF y emite Set-Cookie: XSRF-TOKEN.
let csrfBootstrapInFlight = null;
const ensureCsrfToken = async () => {
  if (getCsrfTokenFromCookie()) return;
  if (!csrfBootstrapInFlight) {
    csrfBootstrapInFlight = axios
      .get(`${baseURL}/auth/csrf`, { withCredentials: true })
      .catch(() => null)
      .finally(() => {
        csrfBootstrapInFlight = null;
      });
  }
  await csrfBootstrapInFlight;
};

// Interceptor ASÍNCRONO: garantiza token CSRF antes de cualquier petición mutativa
apiClient.interceptors.request.use(
  async (config) => {
    const method = config.method?.toUpperCase();
    if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
      // No pedir token para el propio login (es público e ignorado por CSRF)
      const isLogin = config.url?.includes('/auth/login');
      if (!isLogin) {
        await ensureCsrfToken();
      }
      const csrfToken = getCsrfTokenFromCookie();
      if (csrfToken) {
        config.headers['X-XSRF-TOKEN'] = csrfToken;
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor para capturar respuestas 401 y 403
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status } = error.response;
      if (status === 401) {
        // Redirección a login si la sesión expiró y no estamos ya en login
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login?expired=true';
        }
      }
      // 403: NO redirigir; dejar que cada pantalla muestre el detalle del backend
    }
    return Promise.reject(error);
  }
);

export default apiClient;
export { ensureCsrfToken, getCsrfTokenFromCookie };
