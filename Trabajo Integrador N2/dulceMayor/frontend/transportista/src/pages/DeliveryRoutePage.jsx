import { useNavigate } from 'react-router-dom';
import { Async, EmptyState, Greeting, IconButton, Layout, MapView, OptimizeRouteButton, RoutePlanBanner, StopCard, SwipeRow, originPin, stopDestination, useAsync, useDirections, useRefresh, useRouteOptimizer } from '@dulcemayor/shared';
import { deliveryApi } from '../api.js';
import DeliveryNav from '../components/DeliveryNav.jsx';

export const deliveryPins = (stops, next) =>
  stops.map((s) => ({ id: s.id, text: s.sequence, lat: s.lat, lng: s.lng, label: s.name, status: s === next ? 'next' : s.status === 'ENTREGADO' ? 'vis' : 'pend' }));

/** Reparto de hoy: mapa + siguiente entrega + resto de la ruta. Sin llamadas al cliente. */
export default function DeliveryRoutePage() {
  const navigate = useNavigate();
  const directions = useDirections();
  const route = useAsync(deliveryApi.route, []);
  const { plan, optimizing, optimize, clear } = useRouteOptimizer(deliveryApi.optimizeRoute, route.reload);
  const reload = () => {
    clear(); // el trazado queda viejo ante cualquier cambio de la ruta
    return route.reload();
  };
  const { refresh, busy } = useRefresh(reload);

  return (
    <Layout
      title="Reparto de hoy"
      onRefresh={refresh}
      refreshing={busy}
      nav={<DeliveryNav />}
      actions={<IconButton icon="refresh" label="Actualizar" onClick={refresh} />}
    >
      <Async state={route}>
        {(r) => {
          if (r.stops.length === 0) {
            return <EmptyState icon="store" title="No tenés una ruta asignada" text="Cuando tu supervisor te asigne una ruta para hoy, la vas a ver acá." action="Actualizar" onAction={refresh} />;
          }
          const next = r.stops.find((s) => s.status === 'PENDIENTE');
          const done = r.stops.filter((s) => s.status === 'ENTREGADO').length;
          const pending = r.stops.filter((s) => s.status === 'PENDIENTE');
          const others = r.stops.filter((s) => s !== next);
          const pins = deliveryPins(r.stops, next);
          return (
            <div className="cols sticky">
              <div className="side">
                <MapView pins={plan ? [originPin(plan.origin), ...pins] : pins} route={!plan} line={plan ? plan.geometry : null} onPinClick={(id) => navigate(`/reparto/${id}`)} />
              </div>
              <div className="col">
                <Greeting shortcuts={[{ to: '/mapa', label: 'Mapa', icon: 'map' }, { to: '/entregas', label: 'Entregas hechas', icon: 'done' }]} />
                <div>
                  <h2>Ruta de reparto de hoy</h2>
                  <p className="small sec">
                    {done} de {r.stops.length} entregadas · {next ? `${r.kmRemaining.toLocaleString('es-AR')} km restantes` : 'Ruta completa'}
                  </p>
                  <div style={{ height: 8, borderRadius: 4, background: 'var(--border)', marginTop: 8, overflow: 'hidden' }}>
                    <div style={{ height: '100%', width: `${(done / r.stops.length) * 100}%`, background: 'var(--ok)' }} />
                  </div>
                </div>
                {pending.length > 1 && <OptimizeRouteButton label="Optimizar mi reparto" optimizing={optimizing} optimized={!!plan} onClick={optimize} />}
                {plan && <RoutePlanBanner plan={plan} count={pending.length} title="Reparto optimizado" />}
                {next ? (
                  <StopCard stop={next} highlight actionLabel="Ir a la entrega" onAction={() => navigate(`/entrega/${next.id}`)} onSelect={() => navigate(`/reparto/${next.id}`)} />
                ) : (
                  <div className="alert" style={{ background: 'var(--ok-bg)', color: 'var(--ok)' }}>
                    <p><b>¡Terminaste el reparto de hoy!</b></p>
                  </div>
                )}
                <h2>{next ? 'Resto de la ruta' : 'Entregas de hoy'}</h2>
                <div className="col">
                  {others.map((s) =>
                    s.status === 'PENDIENTE' ? (
                      <SwipeRow key={s.id} actions={[{ key: 'nav', label: 'Cómo llegar', icon: 'nav', color: 'var(--primary)', onClick: () => directions(stopDestination(s)) }]}>
                        <StopCard stop={s} onSelect={() => navigate(`/reparto/${s.id}`)} />
                      </SwipeRow>
                    ) : (
                      <StopCard key={s.id} stop={s} onSelect={() => navigate(`/reparto/${s.id}`)} />
                    ),
                  )}
                </div>
              </div>
            </div>
          );
        }}
      </Async>
    </Layout>
  );
}