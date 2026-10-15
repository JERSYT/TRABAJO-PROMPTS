import axios from 'axios';

/**
 * Cliente HTTP centralizado con soporte obligatorio para:
 * - Sesiones de servidor mediante cookies (withCredentials = true)
 * - Inyección automática de token CSRF (X-XSRF-TOKEN) tanto desde Cookie como desde JSON en memoria
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

// Almacén en memoria del token CSRF para entornos cross-origin (Vercel -> Render)
// donde el navegador no permite leer document.cookie entre dominios distintos.
let inMemoryCsrfToken = null;

export const clearCsrfToken = () => {
  inMemoryCsrfToken = null;
};

// Función auxiliar para extraer el valor de la cookie XSRF-TOKEN si está disponible
const getCsrfTokenFromCookie = () => {
  if (typeof document === 'undefined') return null;
  const match = document.cookie.match(new RegExp('(^|;\\s*)XSRF-TOKEN=([^;]*)'));
  return match ? decodeURIComponent(match[2]) : null;
};

// Bootstrap explícito: obtiene el token CSRF desde el backend (JSON body o Set-Cookie)
let csrfBootstrapInFlight = null;
export const ensureCsrfToken = async () => {
  if (inMemoryCsrfToken) return inMemoryCsrfToken;

  const cookieVal = getCsrfTokenFromCookie();
  if (cookieVal) {
    inMemoryCsrfToken = cookieVal;
    return inMemoryCsrfToken;
  }

  if (!csrfBootstrapInFlight) {
    csrfBootstrapInFlight = axios
      .get(`${baseURL}/auth/csrf`, { withCredentials: true })
      .then((res) => {
        if (res.data && res.data.token) {
          inMemoryCsrfToken = res.data.token;
        } else {
          inMemoryCsrfToken = getCsrfTokenFromCookie();
        }
        return inMemoryCsrfToken;
      })
      .catch(() => null)
      .finally(() => {
        csrfBootstrapInFlight = null;
      });
  }
  return await csrfBootstrapInFlight;
};

// Interceptor ASÍNCRONO: garantiza token CSRF antes de cualquier petición mutativa
apiClient.interceptors.request.use(
  async (config) => {
    const method = config.method?.toUpperCase();
    if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
      // El login es público e ignorado por CSRF
      const isLogin = config.url?.includes('/auth/login');
      if (!isLogin) {
        const token = await ensureCsrfToken();
        if (token) {
          config.headers['X-XSRF-TOKEN'] = token;
        }
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
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login?expired=true';
        }
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
export { getCsrfTokenFromCookie };
