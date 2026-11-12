import { useEffect, useMemo, useState } from 'react';
import { LoaderCircle, Truck, X } from 'lucide-react';
import { apiAdmin as api } from '../api';

/**
 * Despacho de pedidos (administración). Un pedido pago (FACTURADO) o acordado en efectivo, sin transportista, está "pendiente de despacho":
 * se elige un transportista y el backend lo pasa a EN_REPARTO y lo suma a su ruta de hoy (POST /api/despachos).
 * Se despachan todos los pedidos elegidos o ninguno.
 */
const currency = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 });
const orderId = (id) => `#DM-${String(id).slice(0, 5).toUpperCase()}`;
const orderDate = (date) => new Date(date).toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' });

/** Comercios que aparecen más de una vez: el backend los rechaza (una parada lleva un solo pedido). */
export function repeatedClients(orders) {
  const counts = new Map();
  orders.forEach((order) => counts.set(order.clientName, (counts.get(order.clientName) || 0) + 1));
  return [...counts].filter(([, count]) => count > 1).map(([name]) => name);
}

export function DispatchDialog({ orders, onClose, onDone }) {
  const [carriers, setCarriers] = useState(null);
  const [carrierId, setCarrierId] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const repeated = useMemo(() => repeatedClients(orders), [orders]);

  useEffect(() => {
    let active = true;
    api.dispatchCarriers()
      .then((list) => { if (active) setCarriers(list); })
      .catch((cause) => {
        if (!active) return;
        setCarriers([]);
        setError(cause instanceof Error ? cause.message : 'No se pudieron cargar los transportistas.');
      });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    const onKey = (event) => { if (event.key === 'Escape' && !saving) onClose(); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose, saving]);

  const repeatedError = repeated.length
    ? `${repeated.join(', ')} tiene más de un pedido: asignalos en despachos separados.` : '';

  const submit = async (event) => {
    event.preventDefault();
    if (!carrierId || repeatedError) return;
    setSaving(true);
    setError('');
    try {
      const result = await api.createDispatch(carrierId, orders.map((order) => order.id));
      onDone(result);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo asignar el despacho.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="manual-modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget && !saving) onClose(); }}>
      <section className="manual-modal" role="dialog" aria-modal="true" aria-labelledby="dispatch-title">
        <header>
          <div>
            <span className="section-kicker">DESPACHO</span>
            <h2 id="dispatch-title">{orders.length === 1 ? 'Asignar pedido a un transportista' : `Asignar ${orders.length} pedidos a un transportista`}</h2>
          </div>
          <button type="button" onClick={onClose} aria-label="Cerrar" disabled={saving}><X /></button>
        </header>
        <form onSubmit={submit}>
          <label>
            Transportista
            <select value={carrierId} onChange={(event) => setCarrierId(event.target.value)} required disabled={carriers === null}>
              <option value="">{carriers === null ? 'Cargando transportistas…' : 'Seleccioná un transportista'}</option>
              {(carriers || []).map((carrier) => (
                <option key={carrier.id} value={carrier.id}>
                  {carrier.nombre}{carrier.patenteVehiculo ? ` · ${carrier.patenteVehiculo}` : ''}
                </option>
              ))}
            </select>
          </label>
          {carriers?.length === 0 && !error && <p className="manual-error" role="alert">No hay transportistas activos. Dalo de alta en Usuarios internos.</p>}

          <div className="manual-lines-heading"><b>Pedidos a despachar</b></div>
          <ul className="dispatch-orders">
            {orders.map((order) => (
              <li key={order.id}>
                <span><b>{orderId(order.id)}</b> · {order.clientName}</span>
                <small>{currency.format(order.total)}</small>
              </li>
            ))}
          </ul>

          {(repeatedError || error) && <p className="manual-error" role="alert">{repeatedError || error}</p>}
          <footer>
            <button type="button" className="ops-secondary" onClick={onClose} disabled={saving}>Cancelar</button>
            <button className="ops-primary" disabled={saving || !carrierId || Boolean(repeatedError)}>
              {saving ? <LoaderCircle className="spin" /> : <Truck />}
              {saving ? 'Asignando…' : 'Despachar'}
            </button>
          </footer>
        </form>
      </section>
    </div>
  );
}

/** Pedidos pagos (o en efectivo) que todavía no tienen transportista. Se eligen uno o varios y se asignan juntos. */
export function PendingDispatchCard({ orders, onAssign }) {
  const [selected, setSelected] = useState([]);

  // Si un pedido deja de estar pendiente (se despachó, se anuló), se saca de la selección.
  useEffect(() => {
    setSelected((previous) => previous.filter((id) => orders.some((order) => order.id === id)));
  }, [orders]);

  const toggle = (id) => setSelected((previous) => (previous.includes(id) ? previous.filter((x) => x !== id) : [...previous, id]));
  const allSelected = orders.length > 0 && selected.length === orders.length;
  const chosen = orders.filter((order) => selected.includes(order.id));

  return (
    <section className="orders-table-card ops-dispatch-pending">
      <header className="table-title">
        <div>
          <h2>Pendientes de despacho</h2>
          <p>Pedidos pagos o acordados en efectivo, sin transportista. Elegí uno o varios y asignalos.</p>
        </div>
        <button type="button" className="ops-primary" disabled={chosen.length === 0} onClick={() => onAssign(chosen)}>
          <Truck /> Asignar seleccionados{chosen.length > 0 ? ` (${chosen.length})` : ''}
        </button>
      </header>
      <div className="ops-table-wrap">
        <table className="ops-table">
          <thead>
            <tr>
              <th className="dispatch-check">
                <input type="checkbox" aria-label="Seleccionar todos" checked={allSelected} disabled={orders.length === 0}
                  onChange={() => setSelected(allSelected ? [] : orders.map((order) => order.id))} />
              </th>
              <th>Pedido</th>
              <th>Cliente</th>
              <th>Pago</th>
              <th>Total</th>
              <th>Acción</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td className="dispatch-check">
                  <input type="checkbox" aria-label={`Seleccionar ${orderId(order.id)}`} checked={selected.includes(order.id)} onChange={() => toggle(order.id)} />
                </td>
                <td><b>{orderId(order.id)}</b><small>{order.items.length} artículos</small></td>
                <td>{order.clientName}</td>
                <td>{order.status === 'FACTURADO'
                  ? <>Facturado<small>{orderDate(order.invoicedAt || order.createdAt)}</small></>
                  : <>Efectivo<small>se factura al despachar y se cobra al entregar</small></>}</td>
                <td><b>{currency.format(order.total)}</b></td>
                <td>
                  <button type="button" className="row-action" onClick={() => onAssign([order])}>Asignar →</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {orders.length === 0 && <div className="ops-empty">No hay pedidos pendientes de despacho.</div>}
      </div>
    </section>
  );
}