import { useCallback, useEffect, useState } from 'react';
import { Banknote, CheckCircle2, Clock3, CreditCard, Landmark, RefreshCw } from 'lucide-react';
import { apiAdmin } from '../api';

const currency = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 2 });
const paymentTypes = { EFECTIVO: 'Efectivo', TRANSFERENCIA: 'Transferencia', BILLETERA_VIRTUAL: 'Billetera virtual' };
const invoiceStates = { EMITIDA: 'Pendiente de pago', PAGADA: 'Pagada', ANULADA: 'Anulada' };
const paymentStates = { PENDIENTE: 'Pendiente', CONFIRMADO: 'Confirmado', ANULADO: 'Anulado' };
const idLabel = (id) => `#DM-${String(id).slice(0, 8).toUpperCase()}`;
const date = (value) => value ? new Date(value).toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' }) : '—';

/**
 * Facturación y pagos (BK-15). Dos listas:
 * - "Esperando pago": pedidos con el stock reservado y sin factura. Acá se acuerda el medio con el cliente:
 *   efectivo (se factura al despachar y cobra el transportista), transferencia (se factura al acreditarse) o
 *   Mercado Pago (el cliente paga desde su portal y la factura sale sola).
 * - "Facturas": pedidos ya facturados con el estado de su pago.
 * `orders` son todos los pedidos; cada lista filtra lo suyo.
 */
export function BillingPaymentsAdminPage({ orders, loading, onRefresh }) {
  const [payments, setPayments] = useState({});
  const [busyInvoice, setBusyInvoice] = useState('');
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [confirming, setConfirming] = useState(null);

  const loadPayments = useCallback(async () => {
    const billed = orders.filter((order) => order.invoiceId && order.status !== 'ANULADO');
    const results = await Promise.allSettled(billed.map(async (order) => [order.invoiceId, await apiAdmin.payments(order.invoiceId)]));
    const next = {};
    const failed = [];
    results.forEach((result, index) => {
      if (result.status === 'fulfilled') next[result.value[0]] = result.value[1];
      else failed.push(idLabel(billed[index].id));
    });
    setPayments(next);
    if (failed.length) setError(`No se pudo consultar el estado de pago de ${failed.join(', ')}.`);
  }, [orders]);

  useEffect(() => { void loadPayments(); }, [loadPayments]);

  const waiting = orders.filter((order) => order.status === 'PENDIENTE_PAGO');

  /** Acuerda efectivo con el cliente: el pedido queda listo para despachar y se cobra al entregar. */
  const agreeCash = async (order) => {
    setBusyInvoice(order.id);
    setError('');
    setNotice('');
    try {
      await apiAdmin.startOrderPayment(order.id, 'EFECTIVO');
      setNotice(`${idLabel(order.id)}: efectivo acordado. Ya aparece en Despacho; se factura al despachar y cobra el transportista.`);
      await onRefresh();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo acordar el pago en efectivo.');
    } finally {
      setBusyInvoice('');
    }
  };

  /** Le da el alias de la empresa al cliente: queda un pago de transferencia pendiente hasta que se acredite. */
  const showAlias = async (order) => {
    setBusyInvoice(order.id);
    setError('');
    setNotice('');
    try {
      const payment = await apiAdmin.startOrderPayment(order.id, 'TRANSFERENCIA');
      setNotice(`${idLabel(order.id)}: ${payment.instrucciones}`);
      await onRefresh();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo indicar la transferencia.');
    } finally {
      setBusyInvoice('');
    }
  };

  const confirmPayment = async () => {
    if (!confirming) return;
    const { order, type } = confirming;
    const waitingOrder = order.status === 'PENDIENTE_PAGO';
    const busyKey = waitingOrder ? order.id : order.invoiceId;
    setBusyInvoice(busyKey);
    setError('');
    setNotice('');
    try {
      if (waitingOrder) await apiAdmin.confirmOrderPayment(order.id, type);   // emite la factura
      else await apiAdmin.confirmPayment(order.invoiceId, type);
      setConfirming(null);
      setNotice(waitingOrder
        ? `Pago de ${idLabel(order.id)} confirmado: se emitió la factura y el pedido pasa a Despacho.`
        : `Pago de la factura de ${idLabel(order.id)} confirmado.`);
      await Promise.all([loadPayments(), onRefresh()]);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo registrar el pago.');
    } finally {
      setBusyInvoice('');
    }
  };

  const billedOrders = orders.filter((order) => order.invoiceId && order.status !== 'ANULADO');
  return <>
    <section className="orders-table-card ops-resource-page">
      <header className="table-title"><div><h2>Esperando pago</h2><p>Pedidos con el stock reservado. La factura se emite cuando el pago se acredita (o al despachar, si es efectivo).</p></div></header>
      {loading ? <div className="ops-empty">Cargando pedidos…</div> : waiting.length === 0 ? <div className="ops-empty">No hay pedidos esperando pago.</div> :
        <div className="ops-order-list">{waiting.map((order) => <article className="ops-order-card ops-invoice-card" key={order.id}>
          <header><div><b>Pedido {idLabel(order.id)}</b><span>{date(order.createdAt)}</span></div>
            <span className="ops-order-status warning"><Clock3 size={13} /> Esperando pago</span></header>
          <div className="ops-order-info"><div><small>Comercio</small><b>{order.clientName}</b></div>
            <div><small>Medio de pago</small><b>{paymentTypes[order.paymentMethod] || 'A coordinar'}</b></div>
            <div><small>Total a cobrar</small><b>{currency.format(order.total)}</b></div></div>
          <footer>
            <span>{order.paymentMethod === 'EFECTIVO' ? 'Efectivo acordado: se cobra al entregar' : 'Coordiná el medio de pago con el cliente'}</span>
            <div className="ops-payment-actions">
              {order.paymentMethod !== 'EFECTIVO' && <button type="button" className="ops-secondary" disabled={busyInvoice === order.id} onClick={() => agreeCash(order)}><Banknote size={15} /> Efectivo al entregar</button>}
              <button type="button" className="ops-secondary" disabled={busyInvoice === order.id} onClick={() => showAlias(order)}><Landmark size={15} /> Indicar alias</button>
              <button type="button" className="ops-primary" disabled={busyInvoice === order.id} onClick={() => setConfirming({ order, type: 'TRANSFERENCIA' })}>
                {busyInvoice === order.id ? <RefreshCw className="spin" size={15} /> : <CreditCard size={15} />} Transferencia acreditada
              </button>
            </div>
          </footer>
        </article>)}</div>}
    </section>
    <section className="orders-table-card ops-resource-page">
      <header className="table-title"><div><h2>Facturación y pagos</h2><p>Facturas emitidas. Las de efectivo se cobran al entregar; acá también podés registrar un cobro recibido.</p></div>
        <button type="button" className="ops-secondary" onClick={() => { onRefresh(); void loadPayments(); }} disabled={loading} aria-label="Actualizar facturas"><RefreshCw size={15} /> Actualizar</button>
      </header>
      {error && <p className="ops-action-error" role="alert">{error}</p>}
      {notice && <p className="ops-payment-notice" role="status">{notice}</p>}
      {loading ? <div className="ops-empty">Cargando facturas…</div> : billedOrders.length === 0 ? <div className="ops-empty">Todavía no hay facturas emitidas.</div> :
        <div className="ops-order-list">{billedOrders.map((order) => {
          const invoicePayments = payments[order.invoiceId] || [];
          const currentPayment = invoicePayments.find((payment) => payment.estado === 'CONFIRMADO')
            || invoicePayments.find((payment) => payment.estado === 'PENDIENTE');
          const invoiceState = order.status === 'ANULADO' ? 'ANULADA' : currentPayment?.estado === 'CONFIRMADO' ? 'PAGADA' : 'EMITIDA';
          return <article className="ops-order-card ops-invoice-card" key={order.invoiceId}>
            <header><div><b>Pedido {idLabel(order.id)}</b><span>Factura {String(currentPayment?.numeroFactura || order.invoiceId.slice(0, 8))} · {date(order.invoicedAt || order.createdAt)}</span></div>
              <span className={`ops-order-status ${invoiceState === 'PAGADA' ? 'paid' : 'warning'}`}>{invoiceStates[invoiceState]}</span>
            </header>
            <div className="ops-order-info"><div><small>Comercio</small><b>{order.clientName}</b></div><div><small>Medio de pago</small><b>{currentPayment ? paymentTypes[currentPayment.tipoPago] : 'A coordinar'}</b></div><div><small>Total de factura</small><b>{currency.format(order.total)}</b></div></div>
            <footer>
              <span>{currentPayment ? `Pago ${paymentStates[currentPayment.estado]?.toLowerCase() || ''}` : 'Esperando recepción del pago'}</span>
              {currentPayment?.estado === 'CONFIRMADO' ? <span className="ops-success-note"><CheckCircle2 size={15} /> Pago confirmado</span> :
                <div className="ops-payment-actions">
                  <button type="button" className="ops-secondary" disabled={busyInvoice === order.invoiceId} onClick={() => setConfirming({ order, type: 'EFECTIVO' })}>Efectivo recibido</button>
                  <button type="button" className="ops-primary" disabled={busyInvoice === order.invoiceId} onClick={() => setConfirming({ order, type: 'TRANSFERENCIA' })}>
                    {busyInvoice === order.invoiceId ? <RefreshCw className="spin" size={15} /> : <CreditCard size={15} />} Transferencia acreditada
                  </button>
                </div>}
            </footer>
          </article>;
        })}</div>}
      <div className="ops-resource-note"><CreditCard size={15} /> Confirmá la transferencia solo después de verificar que se acreditó. El pago confirmado marca la factura como pagada.</div>
    </section>
    {confirming && <div className="manual-modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget && !busyInvoice) setConfirming(null); }}>
      <section className="manual-modal ops-confirm-payment" role="dialog" aria-modal="true" aria-labelledby="confirm-payment-title">
        <header><div><span className="section-kicker">COBRANZA</span><h2 id="confirm-payment-title">Confirmar pago recibido</h2></div>
          <button type="button" onClick={() => setConfirming(null)} disabled={Boolean(busyInvoice)} aria-label="Cerrar">×</button></header>
        <p>Vas a registrar <b>{paymentTypes[confirming.type]}</b> por <b>{currency.format(confirming.order.total)}</b> para {confirming.order.clientName} ({idLabel(confirming.order.id)}).</p>
        <footer><button type="button" className="ops-secondary" onClick={() => setConfirming(null)} disabled={Boolean(busyInvoice)}>Cancelar</button>
          <button type="button" className="ops-primary" onClick={confirmPayment} disabled={Boolean(busyInvoice)}>{busyInvoice ? 'Registrando…' : 'Confirmar pago'}</button></footer>
      </section>
    </div>}
  </>;
}