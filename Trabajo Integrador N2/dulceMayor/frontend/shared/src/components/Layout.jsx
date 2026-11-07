import { useMemo, useRef } from 'react';
import { useLocation, useNavigate, useNavigationType } from 'react-router-dom';
import { Icon } from './Icon.jsx';
import { ToastProvider } from './Toast.jsx';
import { DirectionsProvider } from './Directions.jsx';
import { usePullToRefresh } from '../hooks/hooks.js';

/** Contenedor raíz de la app: columna centrada de 1040px + host de snackbars. */
export function AppRoot({ children }) {
  return (
    <div className="app-root" aria-live="polite">
      <ToastProvider>
        <DirectionsProvider>{children}</DirectionsProvider>
      </ToastProvider>
    </div>
  );
}

/** Transición entre pantallas: entra por la derecha (push), por la izquierda (volver) o fade (tabs). */
export function Screens({ children }) {
  const { pathname } = useLocation();
  const type = useNavigationType();
  const first = useRef(true);
  const cls = useMemo(() => {
    if (first.current) return 'in-fade';
    return type === 'PUSH' ? 'in-right' : type === 'POP' ? 'in-left' : 'in-fade';
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pathname]);
  first.current = false;
  return (
    <div className={`screen ${cls}`} key={pathname}>
      {children}
    </div>
  );
}

export function IconButton({ icon, label, onClick, badge, primary = true, size = 24 }) {
  return (
    <button className={`icon-btn ${primary ? 'pri' : ''}`} onClick={onClick} aria-label={label}>
      <Icon name={icon} size={size} />
      {badge ? (
        <span className="badge" style={{ transform: 'translate(10px,-12px)' }}>
          {badge}
        </span>
      ) : null}
    </button>
  );
}

/**
 * Plantilla de pantalla (Template Method de UI): barra superior + contenido scrolleable +
 * barra de acción fija + navegación inferior + FAB. Cada página solo completa los "slots".
 */
export function Layout({ title, back, backTo = '/', actions, bar, nav, fab, flush, onRefresh, refreshing, children }) {
  const navigate = useNavigate();
  const mainRef = useRef(null);
  usePullToRefresh(mainRef, onRefresh);

  const goBack = () => {
    if (window.history.state && window.history.state.idx > 0) navigate(-1);
    else navigate(backTo, { replace: true });
  };

  return (
    <>
      {title ? (
        <header className={`topbar ${back ? 'has-back' : ''}`}>
          {back && (
            <button className="icon-btn" onClick={goBack} aria-label="Volver">
              <Icon name="back" />
            </button>
          )}
          <h1>{title}</h1>
          {actions}
        </header>
      ) : null}
      <main className={`main ${flush ? 'flush' : ''}`} ref={mainRef}>
        {onRefresh && (
          <div className={`ptr ${refreshing ? 'show' : ''}`}>
            <Icon name="refresh" size={20} />
            &nbsp;Actualizando…
          </div>
        )}
        {children}
      </main>
      {bar}
      {nav}
      {fab}
    </>
  );
}

/** Barra de navegación inferior. `match` lista los prefijos de ruta que marcan el ítem como activo. */
export function BottomNav({ items }) {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  return (
    <nav className="nav" aria-label="Navegación principal">
      {items.map((it) => {
        const active = (it.match || [it.to]).some((m) => pathname === m || pathname.startsWith(m + '/'));
        return (
          <button key={it.to} onClick={() => navigate(it.to, { replace: true })} aria-current={active ? 'page' : undefined}>
            <span className="pill">
              <Icon name={it.icon} />
              {it.badge ? <span className="badge">{it.badge}</span> : null}
            </span>
            {it.label}
          </button>
        );
      })}
    </nav>
  );
}

export function Fab({ icon = 'plus', label, onClick }) {
  return (
    <button className="fab" onClick={onClick}>
      <Icon name={icon} />
      {label}
    </button>
  );
}

/** Barra de acción fija inferior (botón principal de la pantalla). */
export function ActionBar({ children, maxWidth = 560 }) {
  return (
    <div className="bar" style={{ alignItems: 'center' }}>
      <div className="col" style={{ width: '100%', maxWidth, gap: 8 }}>
        {children}
      </div>
    </div>
  );
}