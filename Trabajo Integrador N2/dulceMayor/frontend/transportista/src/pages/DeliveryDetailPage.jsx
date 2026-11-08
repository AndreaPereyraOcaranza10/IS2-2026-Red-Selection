import { useParams } from 'react-router-dom';
import { Async, Icon, Layout, PAYMENT_LABEL, StopStatusChip, Timeline, fmt, useAsync } from '@dulcemayor/shared';
import { deliveryApi } from '../api.js';

/** Detalle de una entrega: productos, medio de pago y trazabilidad (incluye al transportista). */
export default function DeliveryDetailPage() {
  const { id } = useParams();
  const detail = useAsync(() => deliveryApi.stop(id), [id]);

  return (
    <Layout title="Detalle de la entrega" back backTo="/reparto">
      <Async state={detail}>
        {(d) => {
          const s = d.stop;
          return (
            <div className="cols rev">
              <div className="col">
                <article className="card col" style={{ gap: 8 }}>
                  <div className="row">
                    <b className="grow">{s.name}</b>
                    <StopStatusChip status={s.status} />
                  </div>
                  <p className="sec">{s.address}</p>
                  <p className="small">{s.contactName}</p>
                </article>
                <h2>Productos</h2>
                <article className="card col" style={{ gap: 8 }}>
                  {d.items.map((i) => (
                    <div className="row" key={i.productId}>
                      <span className="grow">{i.quantity} × {i.name}</span>
                      <b>{fmt(i.unitPrice * i.quantity)}</b>
                    </div>
                  ))}
                  <hr style={{ border: 0, borderTop: '1px solid var(--border)', width: '100%' }} />
                  <div className="row">
                    <b className="grow">Total</b>
                    <span className="price">{fmt(s.total)}</span>
                  </div>
                </article>
              </div>
              <div className="col">
                {s.paymentMethod && (
                  <>
                    <h2>Medio de pago</h2>
                    <article className="card row">
                      <Icon name={s.paymentMethod === 'EFECTIVO' ? 'cash' : 'tag'} size={28} />
                      <div className="grow">
                        <b>{PAYMENT_LABEL[s.paymentMethod]}</b>
                        <p className="small sec">{d.paymentInstruction}</p>
                      </div>
                    </article>
                  </>
                )}
                <h2>Trazabilidad</h2>
                <article className="card"><Timeline order={{ state: d.state, trace: d.trace }} /></article>
              </div>
            </div>
          );
        }}
      </Async>
    </Layout>
  );
}