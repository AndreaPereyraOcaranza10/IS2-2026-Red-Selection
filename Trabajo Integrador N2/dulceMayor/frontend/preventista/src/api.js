import { api, createRouteApi, locationApi, pointOfSaleApi } from '@dulcemayor/shared';

const routes = createRouteApi('preventista');

/** Número de pedido para mostrar: los ids del backend son UUID largos, alcanza con los primeros 8 caracteres. */
export const pedidoCorto = (id) => String(id).slice(0, 8);

// ---------- Adapters: DTO del backend -> modelo de las pantallas ----------

/** PortalProduct -> { id, name, presentation, price, category, stockLevel } */
const productFromDTO = (p) => ({
  id: p.id,
  name: p.name,
  presentation: p.pack,
  price: Number(p.price),
  category: p.category,
  stockLevel: !p.inStock || p.stock <= 0 ? 'NONE' : p.stock <= p.stockMinimum ? 'LOW' : 'OK',
});

/** OrderResponse -> { id, state, total, discount, createdAt, pointOfSale, items[], trace } */
const orderFromDTO = (o) => ({
  id: o.id,
  state: o.status,
  total: Number(o.total),
  discount: Number(o.discount ?? 0),
  createdAt: o.createdAt,
  pointOfSale: o.pointOfSale,
  items: o.items.map((i) => ({ productId: i.productId, name: i.productName, unitPrice: Number(i.unitPrice), quantity: i.quantity })),
  trace: { events: o.trace?.events ?? [], carrier: o.trace?.carrier ?? null },
});

const direccionTexto = (d) => (d ? d.textoCompleto || `${d.calle} ${d.numeracion}, ${d.localidadNombre}` : '');

/**
 * Mi zona = mis puntos de venta (`/api/puntos-venta`) cruzados con la ruta de hoy.
 * Si el comercio está en la ruta, el punto toma los datos de su parada (id de parada, orden y estado),
 * así "Marcar como visitado" funciona. `clientId` es siempre el id del comercio, que es el que lleva el pedido.
 */
async function zone() {
  const [puntos, route] = await Promise.all([api.get('/api/puntos-venta'), routes.route()]);
  const paradas = Object.fromEntries(route.stops.map((s) => [s.clientId, s]));
  return puntos.map((p) => {
    const parada = paradas[p.id];
    if (parada) return { ...parada, inRoute: true };
    return {
      id: p.id,
      clientId: p.id,
      name: p.nombreComercio,
      address: direccionTexto(p.direccion),
      lat: p.direccion?.latitud,
      lng: p.direccion?.longitud,
      contactName: p.responsable ? `${p.responsable.nombre} ${p.responsable.apellido}` : '',
      phone: p.responsable?.telefono ?? '',
      status: 'PENDIENTE',
      inRoute: false,
    };
  });
}

/** Carga el pedido (el backend marca la visita) y propone el siguiente punto pendiente de la ruta. */
async function createOrder(dto) {
  const order = orderFromDTO(await api.post('/api/orders', dto));
  let nextStop = null;
  try {
    nextStop = (await routes.route()).stops.find((s) => s.status === 'PENDIENTE') ?? null;
  } catch {
    // sin ruta no hay siguiente punto sugerido: la pantalla lo resuelve sola
  }
  return { order, nextStop };
}

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
  zone,
  // Pedidos y productos: backend real (PV-01).
  products: async () => (await api.get('/api/products')).map(productFromDTO).filter((p) => p.stockLevel !== 'NONE'),
  createOrder,
  orders: async () => (await api.get('/api/orders')).map(orderFromDTO),
  order: async (id) => orderFromDTO(await api.get(`/api/orders/${id}`)),
  // Promociones (BK-06): el backend valida el código; el descuento definitivo lo calcula al cargar el pedido.
  validatePromo: (code) => api.post('/api/promociones/validar', { code }),
};
