import { USE_MOCK, api } from '../api/http.js';

/**
 * El front NO calcula rutas: el trazado ("Cómo llegar") y la optimización del orden los resuelve el backend
 * (Adapter de OSRM). Acá solo queda lo propio del dispositivo: la ubicación actual.
 */

/** Punto de partida de ejemplo (centro de Mendoza) cuando la demo no puede obtener la ubicación real. */
export const DEMO_ORIGIN = { lat: -32.8895, lng: -68.8458 };

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

/** Punto de partida del preventista: su ubicación; en modo demo, un punto del centro de Mendoza si no hay permiso. */
export const startingPoint = () =>
  currentPosition().catch((e) => {
    if (USE_MOCK) return DEMO_ORIGIN;
    throw e;
  });

/**
 * Camino en auto entre dos puntos, calculado por el backend (`GET /api/ruteo/trayecto`).
 * @returns {{ distanceKm: number, durationMin: number, geometry: [number, number][] }}  geometry = [lat, lng][]
 */
export async function fetchRoute(from, to) {
  const q = new URLSearchParams({ desdeLat: from.lat, desdeLng: from.lng, hastaLat: to.lat, hastaLng: to.lng });
  const dto = await api.get(`/api/ruteo/trayecto?${q}`);
  return { distanceKm: dto.distanciaKm, durationMin: dto.duracionMin, geometry: dto.geometria };
}