import axios from 'axios';

/**
 * Cliente HTTP centralizado con soporte obligatorio para:
 * - Sesiones de servidor mediante cookies (withCredentials = true)
 * - Inyección automática de token CSRF (X-XSRF-TOKEN)
 * - Manejo consistente de errores ProblemDetail (RFC 9457)
 */
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

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
  const match = document.cookie.match(new RegExp('(^|;\\s*)XSRF-TOKEN=([^;]*)'));
  return match ? decodeURIComponent(match[2]) : null;
};

// Interceptor para inyectar token CSRF en métodos mutativos
apiClient.interceptors.request.use(
  (config) => {
    const method = config.method?.toUpperCase();
    if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
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
    }
    return Promise.reject(error);
  }
);

export default apiClient;
