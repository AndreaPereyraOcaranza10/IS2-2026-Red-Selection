import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ActionBar, Async, Icon, Layout, MapView, Sheet, fmt, stopDestination, useAsync, useDirections, useToast } from '@dulcemayor/shared';
import { deliveryApi } from '../api.js';
import PaymentNotice from '../components/PaymentNotice.jsx';

/** Siguiente entrega: datos del comercio, cómo se cobra, resumen y botón grande "Marcar como entregado". */
export default function NextDeliveryPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const directions = useDirections();
  const detail = useAsync(() => deliveryApi.stop(id), [id]);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [busy, setBusy] = useState(false);

  const deliver = async () => {
    setBusy(true);
    try {
      // En efectivo, primero se registra el cobro (idempotente: si falla la entrega y se reintenta, no se cobra dos veces).
      const s = detail.data.stop;
      if (s.paymentMethod === 'EFECTIVO') await deliveryApi.collectCash(s.orderId);
      const res = await deliveryApi.deliver(id);
      navigate('/entrega-registrada', { replace: true, state: res }); // { delivered, nextStop, emailSent }
    } catch (e) {
      setConfirmOpen(false);
      toast(e.message, { error: true });
    } finally {
      setBusy(false);
    }
  };

  return (
    <Layout
      title="Siguiente entrega"
      back
      backTo="/reparto"
      bar={
        <ActionBar maxWidth={640}>
          <button className="btn btn-p" style={{ minHeight: 64, fontSize: 18 }} onClick={() => setConfirmOpen(true)} disabled={!detail.data || detail.data.stop.status === 'ENTREGADO'}>
            <Icon name="check" size={24} />
            Marcar como entregado
          </button>
        </ActionBar>
      }
    >
      <Async state={detail}>
        {(d) => {
          const s = d.stop;
          return (
            <>
              <div className="cols rev" style={{ alignItems: 'start' }}>
                <div className="col">
                  <article className="card col">
                    <div className="row">
                      <div className="num">{s.sequence}</div>
                      <div className="grow">
                        <h2 style={{ fontSize: 22, lineHeight: '28px' }}>{s.name}</h2>
                        <p className="sec">{s.address}</p>
                      </div>
                    </div>
                    <MapView route={false} pins={[{ id: s.id, text: s.sequence, lat: s.lat, lng: s.lng, status: 'next', label: s.name }]} />
                    <button className="btn btn-s md" onClick={() => directions(stopDestination(s))}>
                      <Icon name="nav" size={20} />
                      Cómo llegar
                    </button>
                    <div className="row">
                      <div className="grow">
                        <p className="cap muted">Responsable</p>
                        <b>{s.contactName}</b>
                      </div>
                    </div>
                  </article>
                  <PaymentNotice method={s.paymentMethod} total={s.total} instruction={d.paymentInstruction} />
                </div>
                <div className="col">
                  <h2>Resumen del pedido</h2>
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
                  <button className="btn btn-g" onClick={() => navigate(`/reparto/${s.id}`)}>
                    Ver detalle y trazabilidad <Icon name="chev" size={20} />
                  </button>
                </div>
              </div>

              <Sheet open={confirmOpen} onClose={() => setConfirmOpen(false)}>
                <h2 style={{ fontSize: 22, lineHeight: '28px' }}>¿Marcar como entregado?</h2>
                <p className="sec">
                  {s.name} ·{' '}
                  {s.paymentMethod === 'EFECTIVO' ? `Confirmá que cobraste ${fmt(s.total)} en efectivo.` : `${d.paymentInstruction}.`} Vamos a avisar por correo al próximo comercio.
                </p>
                <button className="btn btn-p" onClick={deliver} disabled={busy}>
                  <Icon name="check" size={20} />
                  {busy ? 'Registrando…' : 'Sí, entregado'}
                </button>
                <button className="btn btn-s" onClick={() => setConfirmOpen(false)}>Cancelar</button>
              </Sheet>
            </>
          );
        }}
      </Async>
    </Layout>
  );
}