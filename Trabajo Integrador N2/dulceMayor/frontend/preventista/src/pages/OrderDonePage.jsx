import { useEffect, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ActionBar, Icon, Layout, MapView, OrderStateChip, RoutePlanBanner, fmt, formatDistance, optimizeAndSave, stopDestination, useDirections } from '@dulcemayor/shared';
import { pedidoCorto, preventistaApi } from '../api.js';
import PreventistaNav from '../components/PreventistaNav.jsx';

const isPending = (s) => s.status === 'PENDIENTE';

/**
 * Tras tomar el pedido, el preventista está en el comercio recién visitado: se reordenan las paradas que le
 * quedan por el camino más corto desde ahí y se propone la primera (el siguiente punto más eficiente).
 * Si el cálculo falla se usa el siguiente punto que devolvió el backend.
 */
function useNextEfficientStop(order, serverNext) {
  const [res, setRes] = useState({ status: 'loading' });
  const cur = order?.pointOfSale;
  useEffect(() => {
    if (!order) return undefined;
    let alive = true;
    (async () => {
      try {
        // El backend reordena lo que queda desde el comercio recién visitado y devuelve la ruta y el recorrido.
        const plan = await optimizeAndSave({ lat: cur.lat, lng: cur.lng }, preventistaApi.optimizeRoute);
        const left = plan.route.stops.filter(isPending);
        if (!left.length) return alive && setRes({ status: 'done' });
        if (alive) setRes({ status: 'ok', next: left[0], left, plan });
      } catch {
        if (alive) setRes({ status: 'fallback', next: serverNext });
      }
    })();
    return () => {
      alive = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [order?.id]);
  return res;
}

/**
 * Pedido cargado + siguiente punto de venta sugerido con el recorrido en el mapa.
 * Recibe { order, nextStop } por location.state (respuesta de POST /api/orders).
 */
export default function OrderDonePage() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const directions = useDirections();
  const eff = useNextEfficientStop(state?.order, state?.nextStop);
  if (!state?.order) return <Navigate to="/ruta" replace />;

  const { order } = state;
  const cur = order.pointOfSale;
  const nextStop = eff.next;
  const loading = eff.status === 'loading';
  const go = (to) => navigate(to, { replace: true });

  return (
    <Layout
      nav={<PreventistaNav />}
      bar={
        <ActionBar>
          {nextStop && (
            <button className="btn btn-p" onClick={() => directions(stopDestination(nextStop))}>
              <Icon name="nav" size={20} />
              Cómo llegar
            </button>
          )}
          <button className={`btn ${nextStop ? 'btn-s' : 'btn-p'}`} onClick={() => go('/ruta')}>Volver a mi ruta</button>
          <button className="btn btn-g" onClick={() => go('/pedidos')}>Ver mis pedidos</button>
        </ActionBar>
      }
    >
      <div className="col" style={{ maxWidth: 560, width: '100%', margin: '0 auto' }}>
        <div className="state" style={{ padding: '16px 0' }}>
          <div className="big" style={{ background: 'var(--ok-bg)', color: 'var(--ok)', width: 112, height: 112 }}>
            <Icon name="check" size={64} />
          </div>
          <h1 style={{ fontSize: 28, lineHeight: '34px', fontWeight: 800 }}>¡Pedido #{pedidoCorto(order.id)} cargado!</h1>
          <p className="sec">{cur.name} · {fmt(order.total)}</p>
          <OrderStateChip state={order.state} />
        </div>

        {loading ? (
          <div className="state" style={{ padding: 16 }} aria-busy="true">
            <div className="sk" style={{ height: 200, width: '100%' }} />
            <p className="sec small">Calculando el siguiente punto más eficiente…</p>
          </div>
        ) : nextStop ? (
          <>
            <h2>Siguiente punto sugerido</h2>
            <MapView
              route={eff.status !== 'ok'}
              line={eff.status === 'ok' ? eff.plan.geometry : null}
              pins={[
                { id: cur.id, text: '✓', lat: cur.lat, lng: cur.lng, status: 'vis', label: cur.name },
                ...(eff.status === 'ok' ? eff.left : [nextStop]).map((s) => ({ id: s.id, text: s.sequence, lat: s.lat, lng: s.lng, status: s.id === nextStop.id ? 'next' : 'pend', label: s.name })),
              ]}
            />
            {eff.status === 'ok' && eff.left.length > 1 && <RoutePlanBanner plan={eff.plan} count={eff.left.length} title="Recorrido más eficiente" />}
            <article className="card row">
              <div className="num">{nextStop.sequence}</div>
              <div className="grow">
                <b>{nextStop.name}</b>
                <p className="sec small">{nextStop.address} · {formatDistance(nextStop.distanceKm)}</p>
              </div>
            </article>
          </>
        ) : (
          <div className="alert" style={{ background: 'var(--ok-bg)', color: 'var(--ok)' }}>
            <Icon name="done" />
            <p><b>¡Visitaste todos los puntos de hoy!</b></p>
          </div>
        )}
      </div>
    </Layout>
  );
}