import { useParams } from 'react-router-dom';
import { Async, Layout, OrderStateChip, Timeline, fmt, formatDateTime, useAsync } from '@dulcemayor/shared';
import { preventistaApi } from '../api.js';

/** Detalle del pedido con trazabilidad: carga, stock, facturación, transportista y entrega. */
export default function OrderDetailPage() {
  const { id } = useParams();
  const order = useAsync(() => preventistaApi.order(id), [id]);

  return (
    <Layout title={`Pedido #${id}`} back backTo="/pedidos">
      <Async state={order}>
        {(o) => (
          <div className="cols rev">
            <div className="col">
              <article className="card col" style={{ gap: 8 }}>
                <div className="row">
                  <b className="grow">{o.pointOfSale.name}</b>
                  <OrderStateChip state={o.state} />
                </div>
                <p className="sec">{o.pointOfSale.address}</p>
                <div className="row small muted">
                  <span className="grow">Cargado el {formatDateTime(o.createdAt)}</span>
                  <b style={{ color: 'var(--text)', fontSize: 18 }}>{fmt(o.total)}</b>
                </div>
                {o.discount > 0 && <p className="small" style={{ color: 'var(--ok)' }}>Incluye descuento de {fmt(o.discount)}</p>}
              </article>
              <h2>Trazabilidad</h2>
              <article className="card"><Timeline order={o} /></article>
            </div>
            <div className="col">
              <h2>Productos</h2>
              <article className="card col" style={{ gap: 8 }}>
                {o.items.map((i) => (
                  <div className="row" key={i.productId}>
                    <span className="grow">{i.quantity} × {i.name}</span>
                    <b>{fmt(i.unitPrice * i.quantity)}</b>
                  </div>
                ))}
              </article>
            </div>
          </div>
        )}
      </Async>
    </Layout>
  );
}
