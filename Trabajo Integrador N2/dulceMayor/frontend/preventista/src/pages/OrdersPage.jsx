import { useNavigate } from 'react-router-dom';
import { Async, EmptyState, Fab, Layout, OrderStateChip, fmt, formatShortDate, useAsync, useRefresh } from '@dulcemayor/shared';
import { preventistaApi } from '../api.js';
import PreventistaNav from '../components/PreventistaNav.jsx';

/** Mis pedidos: listado con estado; al tocar uno se ve la trazabilidad. */
export default function OrdersPage() {
  const navigate = useNavigate();
  const orders = useAsync(preventistaApi.orders, []);
  const { refresh, busy } = useRefresh(orders.reload);

  return (
    <Layout
      title="Mis pedidos"
      nav={<PreventistaNav />}
      onRefresh={refresh}
      refreshing={busy}
      fab={<Fab label="Nuevo pedido" onClick={() => navigate('/catalogo', { replace: true })} />}
    >
      <Async state={orders}>
        {(list) =>
          list.length ? (
            <div className="grid g2">
              {list.map((o) => (
                <article
                  key={o.id}
                  className="card tap col"
                  style={{ gap: 8 }}
                  role="button"
                  tabIndex={0}
                  onClick={() => navigate(`/pedidos/${o.id}`)}
                  onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && navigate(`/pedidos/${o.id}`)}
                >
                  <div className="row">
                    <b className="grow">Pedido #{o.id}</b>
                    <OrderStateChip state={o.state} />
                  </div>
                  <div className="row"><span className="grow sec">{o.pointOfSale.name}</span></div>
                  <div className="row small muted">
                    <span className="grow">{formatShortDate(o.createdAt)}</span>
                    <b style={{ color: 'var(--text)', fontSize: 16 }}>{fmt(o.total)}</b>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <EmptyState icon="list" title="Todavía no cargaste pedidos" action="Ir al catálogo" onAction={() => navigate('/catalogo', { replace: true })} />
          )
        }
      </Async>
    </Layout>
  );
}
