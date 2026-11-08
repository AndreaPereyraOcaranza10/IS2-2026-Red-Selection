/**
 * API simulada en memoria. Respeta EXACTAMENTE los contratos REST del README
 * (rutas, DTOs y códigos de estado), así que para pasar al backend real alcanza con
 * VITE_USE_MOCK=false. También reproduce la regla "cambiar la clave inicial antes de usar el sistema".
 */
import { ApiError } from '../api/ApiError.js';
import { INITIAL_PASSWORD } from '../utils/password.js';
import { NOT_HANDLED, routeRoutes } from './routes.js';

const DELAY = import.meta.env.MODE === 'test' ? 0 : 250;
const iso = (day, time) => `2026-11-${String(day).padStart(2, '0')}T${time}:00-03:00`;
const pos = (x, y) => ({ lat: -32.89 - (y - 150) * 0.00014, lng: -68.846 + (x - 200) * 0.00018 });

const CARRIER = { name: 'Marcos Giménez', vehicle: 'Utilitario · AB 123 CD' };
const ALIAS = 'dulcemayor.mza';

const USERS = {
  'preventista.mza': { id: 1, username: 'preventista.mza', fullName: 'Preventista Mendoza', role: 'PREVENTISTA', password: INITIAL_PASSWORD, mustChangePassword: true },
  'transportista.mza': { id: 2, username: 'transportista.mza', fullName: 'Transporte Mendoza', role: 'TRANSPORTISTA', password: INITIAL_PASSWORD, mustChangePassword: true },
};

const PRODUCTS = [
  { id: 1, name: 'Alfajor triple chocolate', presentation: 'Bulto x 24 unidades', price: 18400, category: 'Alfajores', stock: 120 },
  { id: 2, name: 'Chicles menta (display)', presentation: 'Caja x 20 displays', price: 32900, category: 'Chicles', stock: 80 },
  { id: 3, name: 'Caramelos masticables frutales', presentation: 'Bolsa x 1 kg', price: 6800, category: 'Caramelos', stock: 12 },
  { id: 4, name: 'Chocolate con maní', presentation: 'Pack x 12 barras', price: 14250, category: 'Chocolates', stock: 60 },
  { id: 5, name: 'Gomitas ositos', presentation: 'Bulto x 12 bolsas', price: 21600, category: 'Gomitas', stock: 9 },
  { id: 6, name: 'Turrón de maní', presentation: 'Caja x 50 unidades', price: 11900, category: 'Turrones', stock: 40 },
  { id: 7, name: 'Obleas rellenas', presentation: 'Pack x 36 unidades', price: 9700, category: 'Snacks', stock: 75 },
  { id: 8, name: 'Bombones surtidos', presentation: 'Caja x 24 unidades', price: 25800, category: 'Chocolates', stock: 0 },
];

const POINTS = [
  { id: 1, name: 'Kiosco El Sol', address: 'Av. San Martín 1250, Ciudad', distanceKm: 0.45, ...pos(60, 210), contactName: 'Lucía Fernández', phone: '261 555-0111', paymentMethod: 'EFECTIVO', items: [[1, 2], [2, 1]] },
  { id: 2, name: 'Almacén Don Pepe', address: 'Belgrano 480, Ciudad', distanceKm: 1.2, ...pos(120, 150), contactName: 'José Ruiz', phone: '261 555-0122', paymentMethod: 'TRANSFERENCIA', items: [[4, 3], [7, 2]] },
  { id: 3, name: 'Drugstore 24 hs', address: 'Las Heras 220, Ciudad de Mendoza', distanceKm: 1.8, ...pos(200, 105), contactName: 'María Gómez', phone: '261 555-0142', paymentMethod: 'EFECTIVO', items: [[1, 2], [2, 1], [5, 3]] },
  { id: 4, name: 'Autoservicio La Esquina', address: 'Av. Godoy Cruz 1830, Godoy Cruz', distanceKm: 3.4, ...pos(248, 186), contactName: 'Carlos Díaz', phone: '261 555-0163', paymentMethod: 'TRANSFERENCIA', items: [[6, 4], [3, 5]] },
  { id: 5, name: 'Kiosco Las Palmeras', address: 'Mitre 765, Guaymallén', distanceKm: 4.1, ...pos(320, 120), contactName: 'Ana Morales', phone: '261 555-0175', paymentMethod: 'BILLETERA_QR', items: [[7, 6], [4, 2]] },
  { id: 6, name: 'Maxikiosco Cuyo', address: 'Boulogne Sur Mer 90, Las Heras', distanceKm: 5.6, ...pos(352, 228), contactName: 'Pablo Sosa', phone: '261 555-0188', paymentMethod: 'EFECTIVO', items: [[2, 2], [1, 3]] },
  { id: 7, name: 'Drugstore Plaza', address: 'Perú 310, Ciudad', distanceKm: 2.2, ...pos(80, 244), contactName: 'Sofía Núñez', phone: '261 555-0191', paymentMethod: 'EFECTIVO', items: [] },
  { id: 8, name: 'Almacén Los Andes', address: 'Lavalle 905, Ciudad', distanceKm: 2.9, ...pos(168, 262), contactName: 'Raúl Vega', phone: '261 555-0199', paymentMethod: 'EFECTIVO', items: [] },
  { id: 9, name: 'Kiosco Aconcagua', address: 'Sarmiento 140, Godoy Cruz', distanceKm: 3.8, ...pos(288, 40), contactName: 'Marta Paz', phone: '261 555-0204', paymentMethod: 'EFECTIVO', items: [] },
];
const ROUTE = [1, 2, 3, 4, 5, 6];

const db = {
  visitOrder: [1, 2, 5, 3, 6, 4], // orden de visita asignado por el supervisor (sin optimizar)
  deliveryOrder: [1, 2, 6, 4, 3, 5], // idem para el reparto: entregadas primero, resto sin optimizar
  visited: new Set([1, 2]),
  delivered: new Map([[1, iso(6, '09:42')], [2, iso(6, '10:25')]]),
  seq: 1043,
  orders: [
    { id: 1042, pos: 3, state: 'EN_REPARTO', total: 121050, createdAt: iso(3, '09:12'), carrier: CARRIER, events: [['CARGADO', iso(3, '09:12')], ['PENDIENTE_STOCK', iso(3, '09:40')], ['FACTURADO', iso(3, '15:05')], ['EN_REPARTO', iso(4, '08:30')]] },
    { id: 1041, pos: 2, state: 'FACTURADO', total: 98400, createdAt: iso(3, '10:02'), carrier: null, events: [['CARGADO', iso(3, '10:02')], ['PENDIENTE_STOCK', iso(3, '10:30')], ['FACTURADO', iso(3, '16:20')]] },
    { id: 1040, pos: 4, state: 'PENDIENTE_STOCK', total: 76800, createdAt: iso(3, '11:45'), carrier: null, events: [['CARGADO', iso(3, '11:45')], ['PENDIENTE_STOCK', iso(3, '12:10')]] },
    { id: 1039, pos: 1, state: 'ENTREGADO', total: 54300, createdAt: '2026-10-30T09:10:00-03:00', carrier: CARRIER, events: [['CARGADO', '2026-10-30T09:10:00-03:00'], ['FACTURADO', '2026-10-30T14:00:00-03:00'], ['EN_REPARTO', '2026-10-31T08:15:00-03:00'], ['ENTREGADO', '2026-10-31T10:42:00-03:00']] },
    { id: 1038, pos: 5, state: 'CARGADO', total: 41200, createdAt: iso(5, '17:25'), carrier: null, events: [['CARGADO', iso(5, '17:25')]] },
    { id: 1037, pos: 6, state: 'ANULADO', total: 33900, createdAt: '2026-10-30T12:00:00-03:00', carrier: null, events: [['CARGADO', '2026-10-30T12:00:00-03:00'], ['ANULADO', '2026-10-30T13:35:00-03:00']] },
  ],
};

/** Restaura los datos de ejemplo (usuarios con clave inicial, visitas, entregas y pedidos). Útil en tests y demos. */
const SEED_ORDERS = db.orders.slice();
const SEED_POINTS = POINTS.length;
export function resetMock() {
  Object.values(USERS).forEach((u) => {
    u.password = INITIAL_PASSWORD;
    u.mustChangePassword = true;
  });
  db.visitOrder = [1, 2, 5, 3, 6, 4];
  db.deliveryOrder = [1, 2, 6, 4, 3, 5];
  db.visited = new Set([1, 2]);
  db.delivered = new Map([[1, iso(6, '09:42')], [2, iso(6, '10:25')]]);
  db.seq = 1043;
  db.orders = SEED_ORDERS.slice();
  POINTS.length = SEED_POINTS;
}

/* ---------- mappers (Entidad -> DTO) ---------- */
const product = (p) => ({ id: p.id, name: p.name, presentation: p.presentation, price: p.price, category: p.category, stockLevel: p.stock === 0 ? 'NONE' : p.stock <= 20 ? 'LOW' : 'OK' });
const pointOfSale = (p) => ({ id: p.id, name: p.name, address: p.address, lat: p.lat, lng: p.lng });
const lineItems = (items) => items.map(([pid, q]) => { const p = PRODUCTS.find((x) => x.id === pid); return { productId: pid, name: p.name, presentation: p.presentation, unitPrice: p.price, quantity: q }; });
const orderTotal = (items) => lineItems(items).reduce((a, l) => a + l.unitPrice * l.quantity, 0);
const order = (o) => ({
  id: o.id, state: o.state, total: o.total, discount: o.discount || 0, createdAt: o.createdAt,
  pointOfSale: pointOfSale(POINTS.find((p) => p.id === o.pos)),
  items: lineItems(o.items || POINTS.find((p) => p.id === o.pos).items),
  trace: { events: o.events.map(([state, at]) => ({ state, at })), carrier: o.carrier },
});
const visitStop = (id, i) => { const p = POINTS.find((x) => x.id === id); return { id, sequence: i + 1, name: p.name, address: p.address, distanceKm: p.distanceKm, lat: p.lat, lng: p.lng, contactName: p.contactName, phone: p.phone, status: db.visited.has(id) ? 'VISITADO' : 'PENDIENTE' }; };
const deliveryStop = (id, i) => { const p = POINTS.find((x) => x.id === id); return { id, sequence: i + 1, name: p.name, address: p.address, distanceKm: p.distanceKm, lat: p.lat, lng: p.lng, contactName: p.contactName, status: db.delivered.has(id) ? 'ENTREGADO' : 'PENDIENTE', deliveredAt: db.delivered.get(id) || null, paymentMethod: p.paymentMethod, total: orderTotal(p.items) }; };
const nextVisit = () => { const i = db.visitOrder.findIndex((id) => !db.visited.has(id)); return i < 0 ? null : visitStop(db.visitOrder[i], i); };
const instruction = (m) => (m === 'EFECTIVO' ? 'Cobrar en efectivo al entregar' : m === 'TRANSFERENCIA' ? `Alias: ${ALIAS}` : 'Pago con billetera virtual (QR del comercio)');

const sessionUser = (token) => (token?.startsWith('mock.') ? USERS[token.slice(5)] : null);
const userDto = (u) => ({ id: u.id, username: u.username, fullName: u.fullName, role: u.role, mustChangePassword: u.mustChangePassword });
const session = (u) => ({ token: `mock.${u.username}`, user: userDto(u) });

/**
 * `opciones.confiable`: con el backend real el login ya lo resolvió el servidor, así que lo simulado no vuelve a
 * pedir la clave ni el cambio de clave inicial.
 */
export async function mockRequest(method, path, body, token, opciones = {}) {
  await new Promise((r) => setTimeout(r, DELAY));
  const [p, qs] = path.split('?');
  const q = new URLSearchParams(qs || '');
  const key = `${method} ${p}`;

  if (key === 'POST /api/auth/login') {
    const u = USERS[body.username];
    if (!u || u.password !== body.password) throw new ApiError(401, 'Usuario o clave incorrectos.', { code: 'BAD_CREDENTIALS' });
    return session(u);
  }

  // Con el backend real, cualquier empleado válido (p. ej. preventista2.mza) puede no estar en el mock: se lo acepta igual.
  const user = sessionUser(token) ?? (opciones.confiable && token?.startsWith('mock.') ? { id: 0, username: token.slice(5), mustChangePassword: false } : null);
  if (!user) throw new ApiError(401, 'Tu sesión venció. Volvé a ingresar.', { code: 'UNAUTHORIZED' });

  if (key === 'POST /api/auth/first-login/change-password') {
    if (body.newPassword.toLowerCase() === INITIAL_PASSWORD) {
      throw new ApiError(400, 'La clave nueva no puede ser la clave inicial.', { errors: [{ field: 'newPassword', message: 'No puede ser la clave inicial' }] });
    }
    user.password = body.newPassword;
    user.mustChangePassword = false;
    return session(user);
  }
  if (user.mustChangePassword && !opciones.confiable) {
    throw new ApiError(403, 'Tenés que cambiar tu clave antes de continuar.', { code: 'PASSWORD_CHANGE_REQUIRED' });
  }

  const r = routeRoutes(key, p, body, q, { db, POINTS, orderTotal });
  if (r !== NOT_HANDLED) return r;

  let m;
  switch (key) {
    case 'GET /api/products':
      return PRODUCTS.filter((x) => x.stock > 0).map(product); // el backend oculta los ítems sin stock

    case 'GET /api/preventor/zone':
      return POINTS.map((x) => ({ ...visitStop(x.id, 0), inRoute: db.visitOrder.includes(x.id), sequence: undefined }));

    case 'POST /api/promotions/validate':
      return body.code === 'DULCE10' ? { valid: true, discountPercent: 10 } : { valid: false, discountPercent: 0 };

    case 'GET /api/orders':
      return db.orders.map(order);

    case 'POST /api/orders': {
      const pt = POINTS.find((x) => x.id === body.pointOfSaleId);
      if (!pt || !body.items?.length) throw new ApiError(400, 'Datos inválidos', { errors: [{ field: 'items', message: 'El pedido necesita al menos un producto' }] });
      const items = body.items.map((i) => [i.productId, i.quantity]);
      const subtotal = orderTotal(items);
      const discount = body.promoCode === 'DULCE10' ? Math.round(subtotal * 0.1) : 0;
      const now = new Date().toISOString();
      const o = { id: db.seq++, pos: pt.id, state: 'CARGADO', items, total: subtotal - discount, discount, createdAt: now, carrier: null, events: [['CARGADO', now]] };
      db.orders.unshift(o);
      db.visited.add(pt.id); // tomar el pedido cuenta como visita
      return { order: order(o), nextStop: nextVisit() };
    }

    case 'POST /api/points-of-sale': {
      const errors = [];
      if (!body.name?.trim()) errors.push({ field: 'name', message: 'Ingresá el nombre del comercio' });
      if (String(body.cuit || '').replace(/\D/g, '').length !== 11) errors.push({ field: 'cuit', message: 'El CUIT tiene que tener 11 dígitos' });
      if (!body.contactName?.trim()) errors.push({ field: 'contactName', message: 'Ingresá el responsable' });
      if (!/^\S+@\S+\.\S+$/.test(body.email || '')) errors.push({ field: 'email', message: 'Revisá el correo: falta el @ o el dominio' });
      if (!body.phone?.trim()) errors.push({ field: 'phone', message: 'Ingresá un teléfono' });
      if (!body.address?.trim()) errors.push({ field: 'address', message: 'Ingresá la dirección' });
      if (errors.length) throw new ApiError(400, 'Datos inválidos', { errors });
      const id = POINTS.length + 1;
      POINTS.push({ id, name: body.name, address: body.address, distanceKm: 1, lat: body.lat ?? -32.89, lng: body.lng ?? -68.846, contactName: body.contactName, phone: body.phone, paymentMethod: 'EFECTIVO', items: [] });
      return { id, name: body.name };
    }

    case 'GET /api/geo/reverse':
      return { address: 'Av. España 1020, Ciudad de Mendoza', lat: Number(q.get('lat')), lng: Number(q.get('lng')) };

    default:
  }

  if ((m = p.match(/^\/api\/orders\/(\d+)$/)) && method === 'GET') {
    const o = db.orders.find((x) => x.id === Number(m[1]));
    if (!o) throw new ApiError(404, 'No encontramos el pedido.', { code: 'NOT_FOUND' });
    return order(o);
  }

  if ((m = p.match(/^\/api\/delivery\/stops\/(\d+)$/)) && method === 'GET') {
    const id = Number(m[1]);
    const i = db.deliveryOrder.indexOf(id);
    if (i < 0) throw new ApiError(404, 'No encontramos la entrega.', { code: 'NOT_FOUND' });
    const stop = deliveryStop(id, i);
    const pt = POINTS.find((x) => x.id === id);
    const events = [['CARGADO', iso(3, '09:12')], ['FACTURADO', iso(3, '15:05')], ['EN_REPARTO', iso(6, '08:30')]];
    if (stop.deliveredAt) events.push(['ENTREGADO', stop.deliveredAt]);
    return {
      stop,
      state: stop.deliveredAt ? 'ENTREGADO' : 'EN_REPARTO',
      items: lineItems(pt.items),
      paymentInstruction: instruction(pt.paymentMethod),
      trace: { events: events.map(([state, at]) => ({ state, at })), carrier: CARRIER },
    };
  }


  throw new ApiError(404, `Ruta no encontrada: ${method} ${p}`, { code: 'NOT_FOUND' });
}