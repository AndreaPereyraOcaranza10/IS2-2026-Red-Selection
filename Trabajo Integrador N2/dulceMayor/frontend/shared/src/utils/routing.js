import { USE_MOCK } from '../api/http.js';

/**
 * Servicio de rutas compatible con OSRM. El servidor público es solo para demos y pruebas:
 * para producción conviene uno propio u otro proveedor (OpenRouteService, GraphHopper, MapTiler) vía VITE_ROUTING_URL.
 */
const ROUTING_URL = import.meta.env.VITE_ROUTING_URL || 'https://router.project-osrm.org';

/** Punto de partida de ejemplo (centro de Mendoza) cuando la demo no puede obtener la ubicación real. */
export const DEMO_ORIGIN = { lat: -32.8895, lng: -68.8458 };

export function haversineKm(a, b) {
  const rad = (d) => (d * Math.PI) / 180;
  const dLat = rad(b.lat - a.lat);
  const dLng = rad(b.lng - a.lng);
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLng / 2) ** 2;
  return 6371 * 2 * Math.asin(Math.sqrt(h));
}

/** Ubicación actual del dispositivo (pide permiso al usuario). */
export function currentPosition() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) return reject(new Error('Tu navegador no permite obtener la ubicación.'));
    navigator.geolocation.getCurrentPosition(
      (p) => resolve({ lat: p.coords.latitude, lng: p.coords.longitude }),
      () => reject(new Error('No pudimos obtener tu ubicación. Revisá el permiso de ubicación del navegador.')),
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 30000 },
    );
  });
}

/**
 * Ruta en auto entre dos puntos.
 * @returns {{ distanceKm: number, durationMin: number, geometry: [number, number][] }}  geometry = [lat, lng][]
 */
export async function fetchRoute(from, to) {
  try {
    return await fetchOsrmRoute(from, to);
  } catch (e) {
    if (!USE_MOCK) throw e;
    // Demo sin internet o sin servicio de ruteo: línea recta con distancia y tiempo estimados.
    const km = haversineKm(from, to) * 1.3;
    return { distanceKm: km, durationMin: Math.max(1, Math.round((km / 30) * 60)), geometry: [[from.lat, from.lng], [to.lat, to.lng]] };
  }
}

/** Ruta más rápida por calles (OSRM). `VITE_ROUTING_URL=off` la desactiva (se usa en los tests). */
async function fetchOsrmRoute(from, to) {
  if (ROUTING_URL === 'off') throw new Error('Ruteo desactivado.');
  const url = `${ROUTING_URL}/route/v1/driving/${from.lng},${from.lat};${to.lng},${to.lat}?overview=full&geometries=geojson`;
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), 8000);
  let res;
  try {
    res = await fetch(url, { signal: ctrl.signal });
  } catch {
    throw new Error('No pudimos calcular la ruta. Revisá tu conexión a internet.');
  } finally {
    clearTimeout(timer);
  }
  const data = await res.json().catch(() => null);
  if (!res.ok || data?.code !== 'Ok' || !data.routes?.length) throw new Error('No encontramos una ruta hasta ese punto.');
  const r = data.routes[0];
  return {
    distanceKm: r.distance / 1000,
    durationMin: Math.max(1, Math.round(r.duration / 60)),
    geometry: r.geometry.coordinates.map(([lng, lat]) => [lat, lng]),
  };
}

/** Punto de partida del preventista: su ubicación; en modo demo, un punto del centro de Mendoza si no hay permiso. */
export const startingPoint = () =>
  currentPosition().catch((e) => {
    if (USE_MOCK) return DEMO_ORIGIN;
    throw e;
  });

const pathKm = (pts) => pts.slice(1).reduce((sum, p, i) => sum + haversineKm(pts[i], p), 0);

/**
 * Orden estimado sin servicio de ruteo: vecino más cercano desde el origen y mejora 2-opt (camino abierto,
 * el origen queda fijo). Para las ~10-30 paradas de una ruta diaria el resultado es muy cercano al óptimo.
 */
export function orderByProximity(origin, stops) {
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

/** Orden óptimo real por calles (OSRM /trip: problema del viajante con origen fijo y fin libre). */
async function optimizeWithOsrm(origin, stops) {
  if (ROUTING_URL === 'off') throw new Error('Ruteo desactivado.');
  const coords = [origin, ...stops].map((p) => `${p.lng},${p.lat}`).join(';');
  const url = `${ROUTING_URL}/trip/v1/driving/${coords}?source=first&roundtrip=false&overview=full&geometries=geojson`;
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), 10000);
  let data;
  try {
    const res = await fetch(url, { signal: ctrl.signal });
    data = await res.json();
  } finally {
    clearTimeout(timer);
  }
  if (data?.code !== 'Ok' || !data.trips?.length) throw new Error('El servicio de rutas no pudo ordenar las paradas.');
  const trip = data.trips[0];
  const ordered = stops
    .map((s, i) => ({ s, pos: data.waypoints[i + 1].waypoint_index }))
    .sort((a, b) => a.pos - b.pos)
    .map((x) => x.s);
  return {
    ordered,
    distanceKm: trip.distance / 1000,
    durationMin: Math.max(1, Math.round(trip.duration / 60)),
    geometry: trip.geometry.coordinates.map(([lng, lat]) => [lat, lng]),
    source: 'osrm',
  };
}

/**
 * Ordena las paradas pendientes para recorrerlas en el menor camino posible, partiendo de `origin`.
 * @param {{lat:number,lng:number}} origin
 * @param {{id:number,lat:number,lng:number}[]} stops
 * @returns {{ order:number[], distanceKm:number, durationMin:number, geometry:[number,number][], source:'osrm'|'estimada' }}
 *   `order` = ids en el orden a visitar; `source` indica si salió del servicio de rutas o es una estimación.
 */
export async function optimizeStops(origin, stops) {
  try {
    const r = await optimizeWithOsrm(origin, stops);
    return { order: r.ordered.map((s) => s.id), ...r, ordered: undefined };
  } catch {
    const ordered = orderByProximity(origin, stops);
    const pts = [origin, ...ordered];
    const km = pathKm(pts) * 1.3;
    return {
      order: ordered.map((s) => s.id),
      distanceKm: km,
      durationMin: Math.max(1, Math.round((km / 30) * 60)),
      geometry: pts.map((p) => [p.lat, p.lng]),
      source: 'estimada',
    };
  }
}