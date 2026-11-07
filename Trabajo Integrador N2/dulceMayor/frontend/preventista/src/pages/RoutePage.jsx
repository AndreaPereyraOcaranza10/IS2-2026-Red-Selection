import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Async, EmptyState, Fab, IconButton, Layout, MapView, OptimizeRouteButton, RoutePlanBanner, StopCard, SwipeRow,
  capitalize, originPin, stopDestination, useAsync, useDirections, useRefresh, useRouteOptimizer,
} from '@dulcemayor/shared';
import { preventistaApi } from '../api.js';
import PreventistaNav from '../components/PreventistaNav.jsx';
import StopSheet from '../components/StopSheet.jsx';

/** Mi ruta de hoy: mapa + siguiente punto sugerido + resto de la ruta (con swipe para llamar / navegar). */
export default function RoutePage() {
  const navigate = useNavigate();
  const directions = useDirections();
  const route = useAsync(preventistaApi.route, []);
  const { plan, optimizing, optimize, clear } = useRouteOptimizer(preventistaApi.saveRouteOrder, route.reload);
  // Cualquier cambio externo (visita, refresco) deja viejo el trazado: se descarta y se puede volver a optimizar.
  const reload = () => {
    clear();
    return route.reload();
  };
  const { refresh, busy } = useRefresh(reload);
  const [selected, setSelected] = useState(null);

  const today = capitalize(new Date().toLocaleDateString('es-AR', { weekday: 'long', day: 'numeric', month: 'long' }));

  return (
    <Layout
      title="Mi ruta de hoy"
      onRefresh={refresh}
      refreshing={busy}
      nav={<PreventistaNav />}
      actions={
        <>
          <IconButton icon="map" label="Ver mapa de mi zona" onClick={() => navigate('/zona')} />
          <IconButton icon="refresh" label="Actualizar" onClick={refresh} />
        </>
      }
      fab={<Fab label="Nuevo punto de venta" onClick={() => navigate('/alta')} />}
    >
      <Async state={route}>
        {(r) => {
          if (r.stops.length === 0) {
            return <EmptyState icon="store" title="No tenés una ruta asignada" text="Cuando tu supervisor te asigne una ruta para hoy, la vas a ver acá." action="Actualizar" onAction={refresh} />;
          }
          const next = r.stops.find((s) => s.status === 'PENDIENTE');
          const done = r.stops.filter((s) => s.status === 'VISITADO').length;
          const pending = r.stops.filter((s) => s.status === 'PENDIENTE');
          const stopPins = r.stops.map((s) => ({
            id: s.id, text: s.sequence, lat: s.lat, lng: s.lng, label: s.name,
            status: s === next ? 'next' : s.status === 'VISITADO' ? 'vis' : 'pend',
          }));
          const pins = plan ? [originPin(plan.origin), ...stopPins] : stopPins;
          const others = r.stops.filter((s) => s !== next);
          return (
            <div className="cols sticky">
              <div className="side">
                <MapView pins={pins} route={!plan} line={plan ? plan.geometry : null} onPinClick={(id) => setSelected(r.stops.find((s) => s.id === id))} />
              </div>
              <div className="col">
                <div>
                  <h2>{today}</h2>
                  <p className="small sec">
                    {done} de {r.stops.length} visitados · {next ? `${r.kmRemaining.toLocaleString('es-AR')} km restantes` : 'Ruta completa'}
                  </p>
                  <div style={{ height: 8, borderRadius: 4, background: 'var(--border)', marginTop: 8, overflow: 'hidden' }}>
                    <div style={{ height: '100%', width: `${(done / r.stops.length) * 100}%`, background: 'var(--ok)' }} />
                  </div>
                </div>
                {pending.length > 1 && <OptimizeRouteButton optimizing={optimizing} optimized={!!plan} onClick={() => optimize(pending)} />}
                {plan && <RoutePlanBanner plan={plan} count={pending.length} />}
                {next ? (
                  <StopCard stop={next} highlight actionLabel="Cómo llegar" onAction={() => directions(stopDestination(next))} onSelect={() => setSelected(next)} />
                ) : (
                  <div className="alert" style={{ background: 'var(--ok-bg)', color: 'var(--ok)' }}>
                    <p><b>¡Visitaste todos los puntos de hoy!</b></p>
                  </div>
                )}
                <h2>{next ? 'Resto de la ruta' : 'Paradas de hoy'}</h2>
                <div className="col">
                  {others.map((s) =>
                    s.status === 'PENDIENTE' ? (
                      <SwipeRow
                        key={s.id}
                        actions={[
                          { key: 'call', label: 'Llamar', icon: 'phone', color: 'var(--secondary)', href: `tel:${s.phone.replace(/\s/g, '')}` },
                          { key: 'nav', label: 'Cómo llegar', icon: 'nav', color: 'var(--primary)', onClick: () => directions(stopDestination(s)) },
                        ]}
                      >
                        <StopCard stop={s} onSelect={() => setSelected(s)} />
                      </SwipeRow>
                    ) : (
                      <StopCard key={s.id} stop={s} onSelect={() => setSelected(s)} />
                    ),
                  )}
                </div>
                <p className="hint">Deslizá una tarjeta hacia la izquierda para llamar o abrir la navegación.</p>
              </div>
            </div>
          );
        }}
      </Async>
      <StopSheet stop={selected} onClose={() => setSelected(null)} onChanged={reload} />
    </Layout>
  );
}