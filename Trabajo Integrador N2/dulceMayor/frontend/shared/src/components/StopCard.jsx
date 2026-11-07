import { Icon } from './Icon.jsx';
import { StopStatusChip } from './Chip.jsx';
import { formatDistance } from '../utils/format.js';

/**
 * Tarjeta de parada (visita o entrega).
 * - onSelect: toca el cuerpo de la tarjeta (abre detalle / acciones).
 * - highlight: la marca como "siguiente punto sugerido" y muestra el botón de acción primaria.
 * El cuerpo y el botón son hermanos (no hay controles interactivos anidados: accesible con lector de pantalla).
 */
export function StopCard({ stop, highlight = false, onSelect, actionLabel, onAction }) {
  const done = stop.status === 'VISITADO' || stop.status === 'ENTREGADO';
  const interactive = onSelect
    ? {
        role: 'button',
        tabIndex: 0,
        onClick: onSelect,
        onKeyDown: (e) => (e.key === 'Enter' || e.key === ' ') && (e.preventDefault(), onSelect()),
      }
    : {};
  return (
    <article className={`card stop ${highlight ? 'next' : ''}`}>
      {highlight && (
        <div className="banner">
          <Icon name="nav" size={16} />
          Siguiente punto sugerido
        </div>
      )}
      <div className="body" {...interactive}>
        <div className={`num ${done ? 'v' : ''}`}>{stop.sequence ?? stop.id}</div>
        <div className="grow">
          <h3>{stop.name}</h3>
          <p className="sec">{stop.address}</p>
          <div className="meta">
            <StopStatusChip status={stop.status} />
            <span className="dist">
              <Icon name="pin" size={16} />
              {formatDistance(stop.distanceKm)}
            </span>
          </div>
        </div>
      </div>
      {highlight && actionLabel && (
        <div className="act">
          <button className="btn btn-p" onClick={onAction}>
            <Icon name="nav" size={20} />
            {actionLabel}
          </button>
        </div>
      )}
    </article>
  );
}
