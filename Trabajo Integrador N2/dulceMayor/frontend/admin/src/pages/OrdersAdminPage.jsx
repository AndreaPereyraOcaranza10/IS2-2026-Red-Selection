import { useMemo, useState } from 'react';
import { AlertTriangle, Ban, CheckCircle2, Clock3, RefreshCw, Search, ShoppingCart } from 'lucide-react';

const labels = {
  CARGADO: 'Recibido',
  PENDIENTE_STOCK: 'Pendiente de stock',
  PENDIENTE_PAGO: 'Esperando pago',
  FACTURADO: 'Facturado',
  EN_REPARTO: 'En reparto',
  ENTREGADO: 'Entregado',
  ANULADO: 'Anulado',
};
const paymentLabels = { EFECTIVO: 'Efectivo (se cobra al entregar)', TRANSFERENCIA: 'Transferencia', BILLETERA_VIRTUAL: 'Billetera virtual (Mercado Pago)' };
const PENDING = ['CARGADO', 'PENDIENTE_STOCK', 'PENDIENTE_PAGO'];
const currency = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 2 });
const date = (value) => value ? new Date(value).toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' }) : '—';
const idLabel = (id) => `#DM-${String(id).slice(0, 8).toUpperCase()}`;

/**
 * Pedidos y preparación. El pedido ya no se factura desde acá: al cargarse reserva el stock (o espera stock) y la
 * factura sale cuando se acredita el pago o al despachar en efectivo (ver "Facturación y pagos" y "Despacho").
 * Desde esta pantalla se reintenta la reserva de un pedido sin stock y se anula un pedido que todavía no se facturó.
 */
export function OrdersAdminPage({ orders, inventory, loading, onRefresh, onStatusChange }) {
  const [query, setQuery] = useState('');
  const [filter, setFilter] = useState('PENDIENTES');
  const [busyId, setBusyId] = useState('');
  const [error, setError] = useState('');
  const stockByProduct = useMemo(() => new Map(inventory.map((product) => [String(product.id), Number(product.stock) || 0])), [inventory]);
  const visible = useMemo(() => orders.filter((order) => {
    const matchesQuery = `${order.id} ${order.clientName} ${labels[order.status] || order.status}`.toLowerCase().includes(query.toLowerCase());
    const matchesFilter = filter === 'TODOS' || (filter === 'PENDIENTES' ? PENDING.includes(order.status) : order.status === filter);
    return matchesQuery && matchesFilter;
  }), [orders, query, filter]);

  const stockFor = (order) => {
    const requirements = new Map();
    for (const item of order.items || []) {
      const id = String(item.productId);
      requirements.set(id, (requirements.get(id) || 0) + Number(item.quantity));
    }
    const unavailable = [...requirements].flatMap(([id, quantity]) => {
      const product = inventory.find((entry) => String(entry.id) === id);
      const available = stockByProduct.get(id);
      return available == null || available < quantity
        ? [`${product?.name || order.items.find((line) => String(line.productId) === id)?.productName || 'Producto'}: ${available ?? 0} de ${quantity} u.`]
        : [];
    });
    return { ready: unavailable.length === 0, unavailable };
  };

  const change = async (order, status) => {
    if (status === 'ANULADO' && !window.confirm(`¿Anular el pedido ${idLabel(order.id)}? Se libera el stock reservado.`)) return;
    setBusyId(order.id);
    setError('');
    try {
      await onStatusChange(order, status);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo actualizar el pedido.');
    } finally {
      setBusyId('');
    }
  };

  return <section className="orders-table-card ops-resource-page">
    <header className="table-title">
      <div><h2>Pedidos y preparación</h2><p>El stock se reserva al cargar el pedido y se descuenta recién cuando se factura (al acreditarse el pago).</p></div>
      <button type="button" className="ops-secondary" onClick={onRefresh} disabled={loading} aria-label="Actualizar pedidos"><RefreshCw size={15} /> Actualizar</button>
    </header>
    <div className="ops-resource-toolbar">
      <label className="table-search"><Search size={15} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Buscar por pedido o comercio" /></label>
      <label className="ops-filter-label">Estado
        <select value={filter} onChange={(event) => setFilter(event.target.value)}>
          <option value="PENDIENTES">Pendientes (stock o pago)</option><option value="TODOS">Todos</option>
          {Object.entries(labels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
        </select>
      </label>
      <span className="ops-result-count">{visible.length} pedidos</span>
    </div>
    {error && <p className="ops-action-error" role="alert">{error}</p>}
    {loading ? <div className="ops-empty">Cargando pedidos…</div> : visible.length === 0 ? <div className="ops-empty">No hay pedidos para este filtro.</div> :
      <div className="ops-order-list">{visible.map((order) => {
        const waitingStock = order.status === 'PENDIENTE_STOCK';
        const waitingPayment = order.status === 'PENDIENTE_PAGO';
        const stock = waitingStock ? stockFor(order) : { ready: true, unavailable: [] };
        const quantity = (order.items || []).reduce((sum, item) => sum + Number(item.quantity), 0);
        const busy = busyId === order.id;
        return <article className="ops-order-card" key={order.id}>
          <header><div><b>{idLabel(order.id)}</b><span>{date(order.createdAt)}</span></div><span className={`ops-order-status ${waitingStock || waitingPayment ? 'warning' : ''}`}>{labels[order.status] || order.status}</span></header>
          <div className="ops-order-info"><div><small>Comercio</small><b>{order.clientName}</b></div><div><small>Artículos</small><b>{quantity} unidades · {order.items?.length || 0} productos</b></div><div><small>Total del pedido</small><b>{currency.format(order.total)}</b></div></div>
          {waitingStock && <div className={`ops-stock-check ${stock.ready ? 'available' : 'unavailable'}`}>
            {stock.ready ? <CheckCircle2 size={16} /> : <AlertTriangle size={16} />}
            <div><b>{stock.ready ? 'Ya hay stock: reintentá la reserva' : 'Todavía no hay stock suficiente'}</b>
              {!stock.ready && <small>{stock.unavailable.join(' · ')}</small>}
            </div>
          </div>}
          {waitingPayment && <div className="ops-stock-check available">
            <Clock3 size={16} />
            <div><b>Stock reservado · esperando el pago</b>
              <small>Medio de pago: {paymentLabels[order.paymentMethod] || 'a coordinar con el cliente'}. Se registra en "Facturación y pagos".</small>
            </div>
          </div>}
          <footer>
            <span>{order.invoiceId ? `Factura emitida · ${order.invoiceId.slice(0, 8)}` : 'Sin factura emitida'}</span>
            {waitingStock && <button type="button" className="ops-primary" disabled={!stock.ready || busy} onClick={() => change(order, 'PENDIENTE_PAGO')}>
              {busy ? <RefreshCw className="spin" size={15} /> : <CheckCircle2 size={15} />}{busy ? 'Reservando…' : 'Reintentar reserva'}
            </button>}
            {(waitingStock || waitingPayment) && <button type="button" className="ops-secondary" disabled={busy} onClick={() => change(order, 'ANULADO')}>
              <Ban size={15} /> Anular pedido
            </button>}
            {order.invoiceId && <span className="ops-success-note"><CheckCircle2 size={15} /> Facturado</span>}
          </footer>
        </article>;
      })}</div>}
    <div className="ops-resource-note"><ShoppingCart size={15} /> Un pedido sin existencias queda pendiente. Al reponer stock, actualizá esta vista y reintentá la reserva. Un pedido sin pagar por más de 48 horas se anula solo.</div>
  </section>;
}