import { useState } from 'react';
import { Icon } from './Icon.jsx';
import { useToast } from './Toast.jsx';
import { formatDistance } from '../utils/format.js';
import { startingPoint } from '../utils/routing.js';

/**
 * Pide al backend ordenar las paradas pendientes por el camino más corto desde `origin` (`optimize(origin)`, de la
 * Facade de la app). El backend guarda el nuevo orden y devuelve el recorrido:
 * { route, distanceKm, durationMin, geometry, source, origin }.
 */
export const optimizeAndSave = (origin, optimize) => optimize(origin);

/** Pin de "Tu ubicación" para el mapa cuando hay un recorrido optimizado. */
export const originPin = (origin) => ({ id: 'me', text: '★', lat: origin.lat, lng: origin.lng, status: 'vis', label: 'Tu ubicación' });

/**
 * Estado del botón "Optimizar ruta": pide el cálculo al backend y deja `plan` con el recorrido para dibujarlo.
 * `optimize` es la operación de la Facade; `reload` recarga la ruta desde la API; `clear()` descarta el trazado cuando quedó viejo (visita, refresco).
 */
export function useRouteOptimizer(optimizeRoute, reload) {
  const toast = useToast();
  const [plan, setPlan] = useState(null);
  const [optimizing, setOptimizing] = useState(false);

  async function optimize() {
    setOptimizing(true);
    try {
      const origin = await startingPoint();
      const result = await optimizeAndSave(origin, optimizeRoute);
      await reload();
      setPlan(result);
      toast('Ruta optimizada: ya tenés las paradas en el mejor orden');
    } catch (e) {
      toast(e.message, { error: true });
    } finally {
      setOptimizing(false);
    }
  }
  return { plan, optimizing, optimize, clear: () => setPlan(null) };
}

export function OptimizeRouteButton({ optimizing, optimized, onClick, label = 'Optimizar mi ruta' }) {
  return (
    <button className="btn btn-s" disabled={optimizing} aria-busy={optimizing} onClick={onClick}>
      <Icon name="ruta" size={20} />
      {optimizing ? 'Calculando el mejor orden…' : optimized ? 'Volver a optimizar' : label}
    </button>
  );
}

/** Resumen del recorrido: paradas, distancia y tiempo. Avisa si fue una estimación sin servicio de rutas. */
export function RoutePlanBanner({ plan, count, title = 'Ruta optimizada' }) {
  return (
    <div className="alert" style={{ background: 'var(--ok-bg)', color: 'var(--ok)' }} role="status">
      <Icon name="check" />
      <p>
        <b>{title}</b> · {count} {count === 1 ? 'parada' : 'paradas'} · {formatDistance(plan.distanceKm)} · {plan.durationMin} min en auto
        {plan.source === 'estimada' && <span className="small"> (estimado: no pudimos consultar el servicio de rutas)</span>}
      </p>
    </div>
  );
}