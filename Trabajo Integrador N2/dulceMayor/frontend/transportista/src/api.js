import { ApiError, USE_MOCK, api, createRouteApi, getActorId } from '@dulcemayor/shared';

const routes = createRouteApi('transportista');

/** Facade de la API REST para el transportista. Esta app NO tiene acceso a endpoints de pedidos ni de preventa. */
export const deliveryApi = {
  // Rutas: backend real (`/api/rutas/**`). Registrar la entrega avisa por correo al próximo comercio.
  route: routes.route,
  optimizeRoute: routes.optimizeRoute,
  deliver: routes.deliver,
  // Detalle de la entrega desde la ruta de hoy: la parada trae el pedido (orderId) y el detalle sale de /api/entregas.
  stop: async (id) => {
    if (USE_MOCK) return api.get(`/api/delivery/stops/${id}`);
    const route = await routes.route();
    const stop = route.stops.find((s) => String(s.id) === String(id));
    if (!stop) throw new ApiError(404, 'No encontramos la entrega.', { code: 'NOT_FOUND' });
    return detailFromDTO(await api.get(`/api/entregas/pedido/${stop.orderId}`), stop);
  },
  // Detalle por pedido: sirve para entregas de días anteriores, que ya no están en la ruta de hoy.
  orderDetail: async (orderId) => {
    if (USE_MOCK) return api.get(`/api/delivery/stops/${orderId}`);
    return detailFromDTO(await api.get(`/api/entregas/pedido/${orderId}`));
  },
  // Historial de entregas realizadas (por defecto, los últimos 30 días). Fechas en formato AAAA-MM-DD.
  history: async (from, to) => {
    if (USE_MOCK) {
      const route = await routes.route();
      return route.stops.filter((s) => s.status === 'ENTREGADO')
        .map((s) => ({ orderId: s.id, name: s.name, address: s.address, deliveredAt: s.deliveredAt, total: s.total, paymentMethod: s.paymentMethod }));
    }
    const params = new URLSearchParams();
    if (from) params.set('desde', from);
    if (to) params.set('hasta', to);
    const qs = params.toString();
    const list = await api.get(`/api/entregas/transportista/${getActorId()}${qs ? `?${qs}` : ''}`);
    return list.map((h) => ({
      orderId: h.pedidoId, name: h.comercio, address: h.direccion, deliveredAt: h.fechaEntrega,
      total: h.total, paymentMethod: h.medioPago, paymentStatus: h.estadoPago,
    }));
  },
  // Cobro en efectivo contra entrega. Es idempotente: si se reintenta, el backend devuelve el mismo pago.
  collectCash: (orderId) => (USE_MOCK ? Promise.resolve({}) : api.post(`/api/entregas/pedido/${orderId}/cobro`)),
};

/** EntregaDetalleDTO (backend, en español) -> el modelo que usan las pantallas de detalle. */
function detailFromDTO(d, stop) {
  return {
    stop: {
      ...(stop ?? {
        id: d.pedidoId, name: d.comercio, address: d.direccion, contactName: d.responsable, orderId: d.pedidoId,
        status: d.estado === 'ENTREGADO' ? 'ENTREGADO' : 'PENDIENTE', total: d.total,
      }),
      paymentMethod: d.medioPago,
      paymentStatus: d.estadoPago,
    },
    state: d.estado,
    items: d.items.map((i) => ({ productId: i.productoId, name: i.nombre, quantity: i.cantidad, unitPrice: i.precioUnitario })),
    paymentInstruction: d.instruccionesPago ?? '',
    trace: {
      events: [
        { state: 'CARGADO', at: d.fechaCarga },
        { state: 'FACTURADO', at: d.fechaFactura },
        { state: 'EN_REPARTO', at: d.fechaDespacho },
        { state: 'ENTREGADO', at: d.fechaEntrega },
      ].filter((e) => e.at),
      carrier: d.transportista ? { name: d.transportista, vehicle: d.patenteVehiculo ?? '' } : null,
    },
  };
}