import { api } from '@dulcemayor/shared';

/** Facade de la API REST para el transportista. Esta app NO tiene acceso a endpoints de pedidos ni de preventa. */
export const deliveryApi = {
  route: () => api.get('/api/delivery/route/today'),
  saveRouteOrder: (stopIds) => api.put('/api/delivery/route/order', { stopIds }),
  stop: (id) => api.get(`/api/delivery/stops/${id}`),
  deliver: (id) => api.post(`/api/delivery/stops/${id}/deliver`),
};