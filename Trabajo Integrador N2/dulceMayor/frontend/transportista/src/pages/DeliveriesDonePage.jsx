import { useNavigate } from 'react-router-dom';
import { Async, EmptyState, Icon, Layout, PAYMENT_LABEL, StopStatusChip, fmt, formatTime, useAsync, useRefresh } from '@dulcemayor/shared';
import { deliveryApi } from '../api.js';
import DeliveryNav from '../components/DeliveryNav.jsx';

/** Entregas de hoy ya realizadas, con hora y medio de pago. */
export default function DeliveriesDonePage() {
  const navigate = useNavigate();
  const route = useAsync(deliveryApi.route, []);
  const { refresh, busy } = useRefresh(route.reload);

  return (
    <Layout title="Entregas de hoy" onRefresh={refresh} refreshing={busy} nav={<DeliveryNav />}>
      <Async state={route}>
        {(r) => {
          const done = r.stops.filter((s) => s.status === 'ENTREGADO');
          return (
            <>
              <h2>{done.length} de {r.stops.length} completadas</h2>
              {done.length ? (
                <div className="grid g2">
                  {done.map((s) => (
                    <article
                      key={s.id}
                      className="card tap col"
                      style={{ gap: 8 }}
                      role="button"
                      tabIndex={0}
                      onClick={() => navigate(`/reparto/${s.id}`)}
                      onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && navigate(`/reparto/${s.id}`)}
                    >
                      <div className="row">
                        <b className="grow">{s.name}</b>
                        <StopStatusChip status="ENTREGADO" />
                      </div>
                      <p className="sec small">{s.address}</p>
                      <div className="row small muted">
                        <span className="grow" style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
                          <Icon name="clock" size={14} /> {formatTime(s.deliveredAt)} hs{s.paymentMethod ? ` · ${PAYMENT_LABEL[s.paymentMethod]}` : ''}
                        </span>
                        <b style={{ color: 'var(--text)', fontSize: 16 }}>{fmt(s.total)}</b>
                      </div>
                    </article>
                  ))}
                </div>
              ) : (
                <EmptyState icon="box" title="Todavía no hiciste entregas" text="Cuando marques una entrega como entregada, aparece acá." />
              )}
            </>
          );
        }}
      </Async>
    </Layout>
  );
}