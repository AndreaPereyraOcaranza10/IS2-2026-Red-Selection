import { api, getActorId } from './http.js';

/**
 * Adapter de la API de rutas del backend (`/api/rutas/**`, DTOs en español) hacia el modelo que usan las pantallas
 * (paradas con `name`, `address`, `lat`, `status`...). Las pantallas no conocen ni las URLs ni los nombres del backend.
 * El orden de las paradas y el recorrido (OSRM) los calcula SIEMPRE el backend: acá no hay lógica de ruteo.
 */

const STOP_STATUS = {
  PREVENTA: { PENDIENTE: 'PENDIENTE', COMPLETADA: 'VISITADO', OMITIDA: 'OMITIDA' },
  REPARTO: { PENDIENTE: 'PENDIENTE', COMPLETADA: 'ENTREGADO', OMITIDA: 'OMITIDA' },
};

const stopFromDTO = (type) => (p) => ({
  id: p.id,
  sequence: p.orden,
  name: p.comercio,
  address: p.direccion,
  distanceKm: p.kmDesdeAnterior,
  lat: p.latitud,
  lng: p.longitud,
  contactName: p.responsable,
  phone: p.telefono ?? '',
  status: STOP_STATUS[type][p.estado] ?? p.estado,
  [type === 'REPARTO' ? 'deliveredAt' : 'visitedAt']: p.fechaHora,
  orderId: p.pedidoId,
  total: p.totalPedido,
});

/** RutaDTO -> { id, date, kmRemaining, stops[] } */
export const routeFromDTO = (dto) => ({
  id: dto.id,
  date: dto.fecha,
  kmRemaining: dto.kmRestantes,
  stops: dto.paradas.map(stopFromDTO(dto.tipoRuta)),
});

/** PlanRutaDTO -> { route, distanceKm, durationMin, geometry: [lat,lng][], source: 'osrm'|'estimada', origin } */
export const planFromDTO = (dto, origin) => ({
  route: routeFromDTO(dto.ruta),
  distanceKm: dto.distanciaKm,
  durationMin: dto.duracionMin,
  geometry: dto.geometria,
  source: dto.fuente === 'OSRM' ? 'osrm' : 'estimada',
  origin,
});

/** EntregaRealizadaDTO -> { delivered, nextStop, emailSent } */
const deliveryFromDTO = (dto) => {
  const toStop = stopFromDTO('REPARTO');
  return { delivered: toStop(dto.entregada), nextStop: dto.siguiente ? toStop(dto.siguiente) : null, emailSent: dto.correoEnviado };
};

/**
 * Facade de rutas para un tipo de empleado.
 * @param {'preventista'|'transportista'} kind  coincide con el segmento de la URL del backend
 */
export function createRouteApi(kind) {
  let routeId = null;
  const actorId = () => {
    const id = getActorId();
    if (id == null) throw new Error('Tu sesión no tiene un empleado asociado. Volvé a ingresar.');
    return id;
  };
  const route = async () => {
    const dto = await api.get(`/api/rutas/${kind}/${actorId()}/hoy`);
    routeId = dto.id;
    return routeFromDTO(dto);
  };
  return {
    route,
    /** Reordena las paradas pendientes en el backend (calles/OSRM, con respaldo) y devuelve el recorrido. */
    optimizeRoute: async (origin) => {
      if (!routeId) await route();
      const dto = await api.post(`/api/rutas/${routeId}/optimizar`, { latitud: origin.lat, longitud: origin.lng });
      return planFromDTO(dto, origin);
    },
    markVisited: (stopId) => api.post(`/api/rutas/paradas/${stopId}/visita`),
    deliver: async (stopId) => deliveryFromDTO(await api.post(`/api/rutas/paradas/${stopId}/entrega`)),
  };
}
