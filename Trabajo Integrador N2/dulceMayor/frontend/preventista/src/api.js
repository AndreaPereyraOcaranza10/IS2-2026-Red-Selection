import { api, createRouteApi } from '@dulcemayor/shared';

const routes = createRouteApi('preventista');

/** Facade de la API REST para el preventista: las pantallas no conocen URLs ni verbos HTTP. */
export const preventistaApi = {
  // Rutas: backend real (`/api/rutas/**`). El orden y el recorrido los calcula el backend.
  route: routes.route,
  optimizeRoute: routes.optimizeRoute,
  markVisited: routes.markVisited,
  // Pendientes de backend (pedidos, promociones, puntos de venta): contrato del README.
  zone: () => api.get('/api/preventor/zone'),
  products: () => api.get('/api/products'),
  validatePromo: (code) => api.post('/api/promotions/validate', { code }),
  createOrder: (dto) => api.post('/api/orders', dto),
  orders: () => api.get('/api/orders'),
  order: (id) => api.get(`/api/orders/${id}`),
  createPointOfSale: (dto) => api.post('/api/points-of-sale', dto),
  reverseGeocode: (lat, lng) => api.get(`/api/geo/reverse?lat=${lat}&lng=${lng}`),
};