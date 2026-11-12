import { useMemo, useState } from 'react';
import { AlertTriangle, CheckCircle2, FileText, RefreshCw, Search, ShoppingCart } from 'lucide-react';

const labels = {
  CARGADO: 'Recibido',
  PENDIENTE_STOCK: 'Pendiente de stock',
  FACTURADO: 'Facturado',
  EN_REPARTO: 'En reparto',
  ENTREGADO: 'Entregado',
  ANULADO: 'Anulado',
};
const currency = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 2 });
const date = (value) => value ? new Date(value).toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' }) : '—';
const idLabel = (id) => `#DM-${String(id).slice(0, 8).toUpperCase()}`;

export function OrdersAdminPage({ orders, inventory, loading, onRefresh, onStatusChange }) {
  const [query, setQuery] = useState('');
  const [filter, setFilter] = useState('PENDIENTES');
  const [busyId, setBusyId] = useState('');
  const [error, setError] = useState('');
  const stockByProduct = useMemo(() => new Map(inventory.map((product) => [String(product.id), Number(product.stock) || 0])), [inventory]);
  const visible = useMemo(() => orders.filter((order) => {
    const matchesQuery = `${order.id} ${order.clientName} ${labels[order.status] || order.status}`.toLowerCase().includes(query.toLowerCase());
    const matchesFilter = filter === 'TODOS' || (filter === 'PENDIENTES'
      ? ['CARGADO', 'PENDIENTE_STOCK'].includes(order.status)
      : order.status === filter);
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

  const process = async (order) => {
    setBusyId(order.id);
    setError('');
    try {
      await onStatusChange(order, 'FACTURADO');
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo procesar el pedido.');
    } finally {
      setBusyId('');
    }
  };

  return <section className="orders-table-card ops-resource-page">
    <header className="table-title">
      <div><h2>Pedidos y preparación</h2><p>Verificá existencias antes de facturar. La facturación descuenta el stock.</p></div>
      <button type="button" className="ops-secondary" onClick={onRefresh} disabled={loading} aria-label="Actualizar pedidos"><RefreshCw size={15} /> Actualizar</button>
    </header>
    <div className="ops-resource-toolbar">
      <label className="table-search"><Search size={15} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Buscar por pedido o comercio" /></label>
      <label className="ops-filter-label">Estado
        <select value={filter} onChange={(event) => setFilter(event.target.value)}>
          <option value="PENDIENTES">Pendientes de procesar</option><option value="TODOS">Todos</option>
          {Object.entries(labels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
        </select>
      </label>
      <span className="ops-result-count">{visible.length} pedidos</span>
    </div>
    {error && <p className="ops-action-error" role="alert">{error}</p>}
    {loading ? <div className="ops-empty">Cargando pedidos…</div> : visible.length === 0 ? <div className="ops-empty">No hay pedidos para este filtro.</div> :
      <div className="ops-order-list">{visible.map((order) => {
        const canInvoice = ['CARGADO', 'PENDIENTE_STOCK'].includes(order.status) && !order.invoiceId;
        const stock = canInvoice ? stockFor(order) : { ready: true, unavailable: [] };
        const quantity = (order.items || []).reduce((sum, item) => sum + Number(item.quantity), 0);
        return <article className="ops-order-card" key={order.id}>
          <header><div><b>{idLabel(order.id)}</b><span>{date(order.createdAt)}</span></div><span className={`ops-order-status ${order.status === 'PENDIENTE_STOCK' ? 'warning' : ''}`}>{labels[order.status] || order.status}</span></header>
          <div className="ops-order-info"><div><small>Comercio</small><b>{order.clientName}</b></div><div><small>Artículos</small><b>{quantity} unidades · {order.items?.length || 0} productos</b></div><div><small>Total del pedido</small><b>{currency.format(order.total)}</b></div></div>
          {canInvoice && <div className={`ops-stock-check ${stock.ready ? 'available' : 'unavailable'}`}>
            {stock.ready ? <CheckCircle2 size={16} /> : <AlertTriangle size={16} />}
            <div><b>{stock.ready ? 'Stock suficiente para todas las líneas' : 'No hay stock suficiente'}</b>
              {!stock.ready && <small>{stock.unavailable.join(' · ')}</small>}
            </div>
          </div>}
          <footer>
            <span>{order.invoiceId ? `Factura emitida · ${order.invoiceId.slice(0, 8)}` : 'Sin factura emitida'}</span>
            {canInvoice && <button type="button" className="ops-primary" disabled={!stock.ready || busyId === order.id} onClick={() => process(order)}>
              {busyId === order.id ? <RefreshCw className="spin" size={15} /> : <FileText size={15} />}{busyId === order.id ? 'Procesando…' : 'Procesar y facturar'}
            </button>}
            {order.invoiceId && <span className="ops-success-note"><CheckCircle2 size={15} /> Facturado</span>}
          </footer>
        </article>;
      })}</div>}
    <div className="ops-resource-note"><ShoppingCart size={15} /> Los pedidos sin existencias quedan pendientes. Al reponer stock, actualizá esta vista y volvé a procesarlos.</div>
  </section>;
}
