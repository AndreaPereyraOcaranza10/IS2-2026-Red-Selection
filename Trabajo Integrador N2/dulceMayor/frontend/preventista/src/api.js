import { api, createRouteApi, locationApi, pointOfSaleApi } from '@dulcemayor/shared';

const routes = createRouteApi('preventista');

/** Facade de la API REST para el preventista: las pantallas no conocen URLs ni verbos HTTP. */
export const preventistaApi = {
  // Rutas: backend real (`/api/rutas/**`). El orden y el recorrido los calcula el backend.
  route: routes.route,
  optimizeRoute: routes.optimizeRoute,
  markVisited: routes.markVisited,
  // Puntos de venta, responsables y ubicación (Georef): backend real.
  createPointOfSale: pointOfSaleApi.create,
  searchResponsibles: pointOfSaleApi.searchResponsibles,
  provinces: locationApi.provinces,
  departments: locationApi.departments,
  localities: locationApi.localities,
  locate: locationApi.locate,
  // Pendientes de backend (pedidos, promociones, zona): contrato del README.
  zone: () => api.get('/api/preventor/zone'),
  products: () => api.get('/api/products'),
  validatePromo: (code) => api.post('/api/promotions/validate', { code }),
  createOrder: (dto) => api.post('/api/orders', dto),
  orders: () => api.get('/api/orders'),
  order: (id) => api.get(`/api/orders/${id}`),
};