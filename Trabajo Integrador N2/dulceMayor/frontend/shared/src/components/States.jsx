import { Icon } from './Icon.jsx';

export function EmptyState({ icon = 'box', title, text, action, onAction }) {
  return (
    <div className="state">
      <div className="big">
        <Icon name={icon} size={44} />
      </div>
      <h2>{title}</h2>
      {text && (
        <p className="sec" style={{ maxWidth: 340 }}>
          {text}
        </p>
      )}
      {action && (
        <button className="btn btn-p auto" onClick={onAction}>
          {action}
        </button>
      )}
    </div>
  );
}

export function ErrorState({ error, onRetry }) {
  const offline = error?.code === 'NETWORK';
  return (
    <div className="state err">
      <div className="big">
        <Icon name="wifi" size={44} />
      </div>
      <h2>{offline ? 'Sin conexión' : 'Ocurrió un error'}</h2>
      <p className="sec" style={{ maxWidth: 340 }}>
        {error?.message || 'No pudimos cargar tus datos.'}
      </p>
      <button className="btn btn-p auto" onClick={onRetry}>
        Reintentar
      </button>
    </div>
  );
}

export function ListSkeleton({ rows = 3 }) {
  return (
    <div className="col" aria-busy="true" aria-label="Cargando">
      <div className="sk" style={{ height: 200 }} />
      <div className="sk" style={{ height: 24, width: '60%' }} />
      {Array.from({ length: rows }, (_, i) => (
        <div className="skcard" key={i}>
          <div className="sk" style={{ width: 40, height: 40, borderRadius: '50%' }} />
          <div className="grow col" style={{ gap: 8 }}>
            <div className="sk" style={{ height: 20, width: '70%' }} />
            <div className="sk" style={{ height: 16, width: '90%' }} />
            <div className="sk" style={{ height: 28, width: '40%', borderRadius: 99 }} />
          </div>
        </div>
      ))}
    </div>
  );
}

/**
 * Resuelve los tres estados de una carga (loading / error / data).
 * state: resultado de useAsync. children: (data) => JSX
 */
export function Async({ state, skeleton, children }) {
  if (state.error && !state.data) return <ErrorState error={state.error} onRetry={state.reload} />;
  if (!state.data) return skeleton || <ListSkeleton />;
  return children(state.data);
}
