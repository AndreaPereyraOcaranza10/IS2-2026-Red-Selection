import { useNavigate } from 'react-router-dom';
import { Async, EmptyState, Icon, Layout, PAYMENT_LABEL, StopStatusChip, fmt, formatDateTime, useAsync, useRefresh } from '@dulcemayor/shared';
import { deliveryApi } from '../api.js';
import DeliveryNav from '../components/DeliveryNav.jsx';

/** Historial de entregas realizadas (últimos 30 días), con fecha, medio de pago y total. */
export default function DeliveriesDonePage() {
  const navigate = useNavigate();
  const history = useAsync(() => deliveryApi.history(), []);
  const { refresh, busy } = useRefresh(history.reload);

  return (
    <Layout title="Mis entregas" onRefresh={refresh} refreshing={busy} nav={<DeliveryNav />}>
      <Async state={history}>
        {(list) => (
          <>
            <h2>{list.length} {list.length === 1 ? 'entrega realizada' : 'entregas realizadas'}</h2>
            {list.length ? (
              <div className="grid g2">
                {list.map((h) => (
                  <article
                    key={h.orderId}
                    className="card tap col"
                    style={{ gap: 8 }}
                    role="button"
                    tabIndex={0}
                    onClick={() => navigate(`/pedido/${h.orderId}`)}
                    onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && navigate(`/pedido/${h.orderId}`)}
                  >
                    <div className="row">
                      <b className="grow">{h.name}</b>
                      <StopStatusChip status="ENTREGADO" />
                    </div>
                    <p className="sec small">{h.address}</p>
                    <div className="row small muted">
                      <span className="grow" style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
                        <Icon name="clock" size={14} /> {formatDateTime(h.deliveredAt)} hs{h.paymentMethod ? ` · ${PAYMENT_LABEL[h.paymentMethod]}` : ''}
                      </span>
                      <b style={{ color: 'var(--text)', fontSize: 16 }}>{fmt(h.total)}</b>
                    </div>
                  </article>
                ))}
              </div>
            ) : (
              <EmptyState icon="box" title="Todavía no hiciste entregas" text="Cuando marques una entrega como entregada, aparece acá." />
            )}
          </>
        )}
      </Async>
    </Layout>
  );
}