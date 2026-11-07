import { api } from '@dulcemayor/shared';

/** Facade de la API REST para el preventista: las pantallas no conocen URLs ni verbos HTTP. */
export const preventistaApi = {
  route: () => api.get('/api/preventor/route/today'),
  saveRouteOrder: (stopIds) => api.put('/api/preventor/route/order', { stopIds }),
  zone: () => api.get('/api/preventor/zone'),
  markVisited: (stopId) => api.post(`/api/preventor/route/stops/${stopId}/visit`),
  products: () => api.get('/api/products'),
  validatePromo: (code) => api.post('/api/promotions/validate', { code }),
  createOrder: (dto) => api.post('/api/orders', dto),
  orders: () => api.get('/api/orders'),
  order: (id) => api.get(`/api/orders/${id}`),
  createPointOfSale: (dto) => api.post('/api/points-of-sale', dto),
  reverseGeocode: (lat, lng) => api.get(`/api/geo/reverse?lat=${lat}&lng=${lng}`),
};