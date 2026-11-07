import { Icon } from './Icon.jsx';
import { ORDER_STAGES, ORDER_STATE } from '../utils/domain.js';
import { formatDateTime } from '../utils/format.js';

/**
 * Trazabilidad del pedido: Cargado -> Stock -> Facturado -> En reparto (con transportista) -> Entregado.
 * order: { state, trace: { events: [{ state, at }], carrier: { name, vehicle } | null } }
 */
export function Timeline({ order }) {
  const events = Object.fromEntries(order.trace.events.map((e) => [e.state, e.at]));
  const cancelled = order.state === 'ANULADO';
  const seq = cancelled ? ['CARGADO', 'ANULADO'] : ORDER_STAGES;
  const idx = seq.indexOf(order.state);

  return (
    <div className="tl">
      {seq.map((st, i) => {
        const cur = i === idx;
        const off = i > idx;
        const last = i === seq.length - 1;
        const at = events[st];
        const sub = at
          ? formatDateTime(at)
          : st === 'PENDIENTE_STOCK' && i < idx
            ? 'Sin faltantes de stock'
            : 'Pendiente';
        return (
          <div className="it" key={st}>
            <div className="dotc">
              <div
                className={`dot ${cur ? 'cur' : off ? 'off' : ''}`}
                style={st === 'ANULADO' ? { background: 'var(--err)', boxShadow: '0 0 0 4px var(--err-bg)' } : undefined}
              >
                {off ? null : <Icon name={st === 'ANULADO' ? 'x' : 'check'} size={14} />}
              </div>
              {last ? null : <div className={`ln ${i >= idx ? 'off' : ''}`} />}
            </div>
            <div className="tx">
              <div style={{ fontWeight: 700 }}>{ORDER_STATE[st].label}</div>
              <div className="small muted">{sub}</div>
              {st === 'EN_REPARTO' && at && order.trace.carrier && (
                <div className="small sec" style={{ display: 'flex', gap: 6, alignItems: 'center', marginTop: 2 }}>
                  <Icon name="truck" size={14} />
                  {order.trace.carrier.name} · {order.trace.carrier.vehicle}
                </div>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}
