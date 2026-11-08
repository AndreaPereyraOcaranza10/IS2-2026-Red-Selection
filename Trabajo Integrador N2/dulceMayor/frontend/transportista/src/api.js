import { ApiError, USE_MOCK, api, createRouteApi } from '@dulcemayor/shared';

const routes = createRouteApi('transportista');

/** Facade de la API REST para el transportista. Esta app NO tiene acceso a endpoints de pedidos ni de preventa. */
export const deliveryApi = {
  // Rutas: backend real (`/api/rutas/**`). Registrar la entrega avisa por correo al próximo comercio.
  route: routes.route,
  optimizeRoute: routes.optimizeRoute,
  deliver: routes.deliver,
  // Detalle de la entrega. Con el backend real se arma desde la ruta (comercio, dirección y total del pedido);
  // los productos y el medio de pago llegan cuando el backend sume pedidos y facturación.
  stop: async (id) => {
    if (USE_MOCK) return api.get(`/api/delivery/stops/${id}`);
    const route = await routes.route();
    const stop = route.stops.find((s) => String(s.id) === String(id));
    if (!stop) throw new ApiError(404, 'No encontramos la entrega.', { code: 'NOT_FOUND' });
    return {
      stop,
      state: stop.status === 'ENTREGADO' ? 'ENTREGADO' : 'EN_REPARTO',
      items: [],
      paymentInstruction: '',
      trace: { events: [], carrier: null },
    };
  },
};