import { useState } from 'react';
import { Icon } from './Icon.jsx';
import { useToast } from './Toast.jsx';
import { formatDistance } from '../utils/format.js';
import { optimizeStops, startingPoint } from '../utils/routing.js';

/**
 * Ordena `pending` por el camino más corto desde `origin`, guarda el orden en el backend (`saveOrder(ids)`)
 * y devuelve el recorrido { order, distanceKm, durationMin, geometry, source, origin }.
 * Con una sola parada pendiente no hay nada que ordenar: no se llama al backend.
 */
export async function optimizeAndSave(origin, pending, saveOrder) {
  const result = await optimizeStops(origin, pending);
  if (pending.length > 1) await saveOrder(result.order);
  return { ...result, origin };
}

/** Pin de "Tu ubicación" para el mapa cuando hay un recorrido optimizado. */
export const originPin = (origin) => ({ id: 'me', text: '★', lat: origin.lat, lng: origin.lng, status: 'vis', label: 'Tu ubicación' });

/**
 * Estado del botón "Optimizar ruta": calcula, guarda y deja `plan` con el recorrido para dibujarlo.
 * `reload` recarga la ruta desde la API; `clear()` descarta el trazado cuando quedó viejo (visita, refresco).
 */
export function useRouteOptimizer(saveOrder, reload) {
  const toast = useToast();
  const [plan, setPlan] = useState(null);
  const [optimizing, setOptimizing] = useState(false);

  async function optimize(pending) {
    setOptimizing(true);
    try {
      const origin = await startingPoint();
      const result = await optimizeAndSave(origin, pending, saveOrder);
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
