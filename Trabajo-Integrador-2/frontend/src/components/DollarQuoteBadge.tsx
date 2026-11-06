import { useDollarQuote } from '../hooks/useDollarQuote';

const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 2, maximumFractionDigits: 2 });

export function DollarQuoteBadge({ compact = false }: { compact?: boolean }) {
  const { quote, error } = useDollarQuote();
  const timestamp = quote?.lastScrapedAt || quote?.updatedAt;
  const parsedTimestamp = timestamp ? Date.parse(timestamp) : Number.NaN;
  const time = Number.isFinite(parsedTimestamp) ? new Intl.DateTimeFormat('es-AR', { hour: '2-digit', minute: '2-digit' }).format(new Date(parsedTimestamp)) : '';
  const label = quote
    ? `${quote.source}: cotización de venta ${money.format(quote.sell)}${time ? `, confirmada a las ${time}` : ''}${quote.stale ? ', dato anterior por falta de conexión' : ''}`
    : error ? 'Cotización del Banco Nación no disponible' : 'Consultando cotización del Banco Nación';

  return <span className={`usd usd-quote${compact ? ' usd-compact' : ''}${quote?.stale ? ' usd-stale' : ''}`} title={label} aria-live="polite">
    <i />
    <span>USD BNA</span>
    <b>{quote ? money.format(quote.sell) : '—'}</b>
    <small>{quote ? `${quote.stale ? 'Dato previo' : 'Venta'}${time ? ` · ${time}` : ''}` : error ? 'Sin cotización' : 'Actualizando…'}</small>
  </span>;
}
