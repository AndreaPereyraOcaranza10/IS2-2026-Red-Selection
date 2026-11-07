import { api, createRouteApi } from '@dulcemayor/shared';

const routes = createRouteApi('transportista');

/** Facade de la API REST para el transportista. Esta app NO tiene acceso a endpoints de pedidos ni de preventa. */
export const deliveryApi = {
  // Rutas: backend real (`/api/rutas/**`). Registrar la entrega avisa por correo al próximo comercio.
  route: routes.route,
  optimizeRoute: routes.optimizeRoute,
  deliver: routes.deliver,
  // Detalle de la entrega (productos, instrucción de pago): pendiente de backend (pedidos y facturación).
  stop: (id) => api.get(`/api/delivery/stops/${id}`),
};