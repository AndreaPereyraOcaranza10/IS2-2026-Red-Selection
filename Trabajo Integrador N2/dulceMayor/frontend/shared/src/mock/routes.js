/**
 * Simula en memoria los endpoints de rutas del backend (`/api/rutas/**` y `/api/ruteo/trayecto`) con los mismos
 * DTOs en español. En producción el orden y el trazado los calcula el backend con OSRM; acá, sin red, se usa una
 * estimación en línea recta y el plan sale marcado como "ESTIMADA" (igual que cuando OSRM no responde).
 */
import { ApiError } from '../api/ApiError.js';
/** Marca interna: "esta ruta no es mía, seguí buscando". */
export const NOT_HANDLED = Symbol('not-handled');

const DEMO_ORIGIN = { lat: -32.8895, lng: -68.8458 };
const RUTA_ID = { PREVENTA: 'ruta-preventa', REPARTO: 'ruta-reparto' };

const haversineKm = (a, b) => {
  const rad = (d) => (d * Math.PI) / 180;
  const dLat = rad(b.lat - a.lat);
  const dLng = rad(b.lng - a.lng);
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLng / 2) ** 2;
  return 6371 * 2 * Math.asin(Math.sqrt(h));
};
const pathKm = (pts) => pts.slice(1).reduce((sum, p, i) => sum + haversineKm(pts[i], p), 0);
const round1 = (n) => Math.round(n * 10) / 10;

/** Vecino más cercano + 2-opt (camino abierto, origen fijo): el respaldo en línea recta del backend. */
function orderByProximity(origin, stops) {
  const left = stops.slice();
  const order = [];
  let cur = origin;
  while (left.length) {
    let k = 0;
    left.forEach((s, i) => {
      if (haversineKm(cur, s) < haversineKm(cur, left[k])) k = i;
    });
    cur = left.splice(k, 1)[0];
    order.push(cur);
  }
  let improved = true;
  while (improved) {
    improved = false;
    for (let i = 0; i < order.length - 1; i++) {
      for (let j = i + 1; j < order.length; j++) {
        const cand = order.slice(0, i).concat(order.slice(i, j + 1).reverse(), order.slice(j + 1));
        if (pathKm([origin, ...cand]) + 1e-9 < pathKm([origin, ...order])) {
          order.splice(0, order.length, ...cand);
          improved = true;
        }
      }
    }
  }
  return order;
}

export function routeRoutes(key, p, body, q, { db, POINTS, orderTotal }) {
  const orderOf = (type) => (type === 'PREVENTA' ? db.visitOrder : db.deliveryOrder);
  const doneOf = (type) => (type === 'PREVENTA' ? db.visited : db.delivered); // Set | Map: ambos tienen has()
  const setOrder = (type, ids) => {
    if (type === 'PREVENTA') db.visitOrder = ids;
    else db.deliveryOrder = ids;
  };
  const typeOfStop = (id) => (db.visitOrder.includes(id) ? 'PREVENTA' : 'REPARTO');

  const parada = (type, id, i) => {
    const pt = POINTS.find((x) => x.id === id);
    const order = type === 'REPARTO' ? db.orders.find((o) => o.pos === id && ['EN_REPARTO', 'ENTREGADO'].includes(o.state)) : null;
    return {
      id: String(id),
      orden: i + 1,
      clienteId: String(id),
      comercio: pt.name,
      responsable: pt.contactName,
      telefono: pt.phone,
      direccion: pt.address,
      latitud: pt.lat,
      longitud: pt.lng,
      estado: doneOf(type).has(id) ? 'COMPLETADA' : 'PENDIENTE',
      pedidoId: order ? String(order.id) : null,
      fechaHora: type === 'REPARTO' ? db.delivered.get(id) || null : null,
      kmDesdeAnterior: pt.distanceKm,
      totalPedido: type === 'REPARTO' ? orderTotal(pt.items) : null,
    };
  };
  const ruta = (type) => {
    const ids = orderOf(type);
    const pendientes = ids.filter((id) => !doneOf(type).has(id));
    const hechas = ids.length - pendientes.length;
    return {
      id: RUTA_ID[type],
      fecha: '2026-11-06',
      tipoRuta: type,
      estado: !pendientes.length ? 'FINALIZADA' : hechas ? 'EN_CURSO' : 'PLANIFICADA',
      kmRestantes: round1(pendientes.reduce((sum, id) => sum + POINTS.find((x) => x.id === id).distanceKm, 0)),
      paradas: ids.map((id, i) => parada(type, id, i)),
    };
  };
  const stopId = (m) => {
    const id = Number(m[1]);
    if (!POINTS.some((x) => x.id === id) || !(db.visitOrder.includes(id) || db.deliveryOrder.includes(id))) {
      throw new ApiError(404, `Parada no encontrado: ${m[1]}`, { code: 'NOT_FOUND' });
    }
    return id;
  };
  let m;

  if ((m = p.match(/^\/api\/rutas\/(preventista|transportista)\/[^/]+\/hoy$/)) && key.startsWith('GET')) {
    return ruta(m[1] === 'preventista' ? 'PREVENTA' : 'REPARTO');
  }

  if ((m = p.match(/^\/api\/rutas\/paradas\/(\w+)\/visita$/)) && key.startsWith('POST')) {
    const id = stopId(m);
    if (typeOfStop(id) !== 'PREVENTA') throw new ApiError(422, 'Esta parada es de reparto: se registra como entrega.');
    if (db.visited.has(id)) throw new ApiError(422, 'La parada ya fue registrada.');
    db.visited.add(id);
    return parada('PREVENTA', id, db.visitOrder.indexOf(id));
  }

  if ((m = p.match(/^\/api\/rutas\/paradas\/(\w+)\/entrega$/)) && key.startsWith('POST')) {
    const id = stopId(m);
    if (!db.deliveryOrder.includes(id)) throw new ApiError(422, 'Esta parada es de preventa: se registra como visita.');
    if (db.delivered.has(id)) throw new ApiError(422, 'La parada ya fue registrada.');
    db.delivered.set(id, new Date().toISOString());
    const nextIdx = db.deliveryOrder.findIndex((x) => !db.delivered.has(x));
    return {
      entregada: parada('REPARTO', id, db.deliveryOrder.indexOf(id)),
      siguiente: nextIdx < 0 ? null : parada('REPARTO', db.deliveryOrder[nextIdx], nextIdx),
      correoEnviado: nextIdx >= 0, // el backend avisa por correo al comercio que sigue
    };
  }

  if ((m = p.match(/^\/api\/rutas\/ruta-(preventa|reparto)\/orden$/)) && key.startsWith('PUT')) {
    const type = m[1].toUpperCase();
    const pending = orderOf(type).filter((id) => !doneOf(type).has(id));
    const ids = (body.paradaIds || []).map(Number);
    if (ids.length !== pending.length || new Set(ids).size !== ids.length || !pending.every((id) => ids.includes(id))) {
      throw new ApiError(422, 'Hay que enviar todas las paradas pendientes, sin repetir ni agregar otras.');
    }
    setOrder(
      type,
      orderOf(type)
        .filter((id) => doneOf(type).has(id))
        .concat(ids),
    );
    return ruta(type);
  }

  if ((m = p.match(/^\/api\/rutas\/ruta-(preventa|reparto)\/optimizar$/)) && key.startsWith('POST')) {
    const type = m[1].toUpperCase();
    const origin = body?.latitud != null && body?.longitud != null ? { lat: body.latitud, lng: body.longitud } : DEMO_ORIGIN;
    const pending = orderOf(type)
      .filter((id) => !doneOf(type).has(id))
      .map((id) => ({ id, ...POINTS.find((x) => x.id === id) }));
    const ordered = orderByProximity(origin, pending);
    setOrder(
      type,
      orderOf(type)
        .filter((id) => doneOf(type).has(id))
        .concat(ordered.map((s) => s.id)),
    );
    const path = [origin, ...ordered];
    const km = pathKm(path) * 1.3;
    return {
      ruta: ruta(type),
      distanciaKm: round1(km),
      duracionMin: ordered.length ? Math.max(1, Math.round((km / 30) * 60)) : 0,
      geometria: path.map((pt) => [pt.lat, pt.lng]),
      fuente: 'ESTIMADA',
    };
  }

  if (key === 'GET /api/ruteo/trayecto') {
    const from = { lat: Number(q.get('desdeLat')), lng: Number(q.get('desdeLng')) };
    const to = { lat: Number(q.get('hastaLat')), lng: Number(q.get('hastaLng')) };
    const km = haversineKm(from, to) * 1.3;
    return {
      distanciaKm: km,
      duracionMin: Math.max(1, Math.round((km / 30) * 60)),
      geometria: [
        [from.lat, from.lng],
        [to.lat, to.lng],
      ],
    };
  }

  return NOT_HANDLED;
}