import { ApiError } from './ApiError.js';

/** true = la API se simula en memoria (demo/presentación). false = backend Spring Boot real. */
export const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true';
const BASE_URL = import.meta.env.VITE_API_URL ?? '';

let token = null;
let actor = null;
let onUnauthorized = () => {};

export const setAuthToken = (t) => {
  token = t;
};
/** Usuario de la sesión: el backend identifica al empleado de las rutas por `employeeId` (o por `id`). */
export const setActor = (user) => {
  actor = user ?? null;
};
export const getActorId = () => actor?.employeeId ?? actor?.id ?? null;
export const setUnauthorizedHandler = (fn) => {
  onUnauthorized = fn;
};

/**
 * Adapter de transporte: el resto de la app solo conoce `api.get/post/...`.
 * Cambiar entre mock y backend real no toca ninguna pantalla.
 */
async function request(method, path, body) {
  try {
    if (USE_MOCK) {
      const { mockRequest } = await import('../mock/router.js');
      return await mockRequest(method, path, body, token);
    }
    let res;
    try {
      res = await fetch(BASE_URL + path, {
        method,
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: body === undefined ? undefined : JSON.stringify(body),
      });
    } catch {
      throw new ApiError(0, 'No pudimos conectarnos. Revisá tu conexión a internet y volvé a intentar.', {
        code: 'NETWORK',
      });
    }
    if (res.status === 204) return null;
    const data = await res.json().catch(() => null);
    if (!res.ok) {
      // El backend responde ProblemDetail ({ detail, errores: { campo: mensaje } }).
      const fieldErrors = data?.errors ?? (data?.errores ? Object.entries(data.errores).map(([field, message]) => ({ field, message })) : undefined);
      throw new ApiError(res.status, data?.detail || data?.message || 'Ocurrió un error inesperado.', {
        code: data?.code,
        errors: fieldErrors,
      });
    }
    return data;
  } catch (e) {
    if (e instanceof ApiError && e.status === 401 && token) onUnauthorized();
    throw e;
  }
}

export const api = {
  get: (path) => request('GET', path),
  post: (path, body) => request('POST', path, body ?? {}),
  put: (path, body) => request('PUT', path, body),
  patch: (path, body) => request('PATCH', path, body),
  delete: (path) => request('DELETE', path),
};