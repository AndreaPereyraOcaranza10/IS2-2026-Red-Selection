import { useRef, useState } from 'react';
import { Icon } from './Icon.jsx';

const ACTION_W = 96;

/**
 * Fila deslizable hacia la izquierda que revela acciones.
 * actions: [{ key, label, icon, color, onClick?, href? }]  (con `href` se renderiza un <a>, ej. tel:)
 */
export function SwipeRow({ actions, children }) {
  const total = actions.length * ACTION_W;
  const [x, setX] = useState(0);
  const [drag, setDrag] = useState(false);
  const st = useRef({ x0: 0, dx: 0, base: 0, moved: false, block: false });

  const onDown = (e) => {
    if (e.button > 0) return;
    const s = st.current;
    s.x0 = e.clientX;
    s.dx = 0;
    s.moved = false;
    setDrag(true);
    const move = (ev) => {
      s.dx = ev.clientX - s.x0;
      if (Math.abs(s.dx) > 6) s.moved = true;
      setX(Math.max(-total, Math.min(0, s.base + s.dx)));
    };
    const up = () => {
      window.removeEventListener('pointermove', move);
      window.removeEventListener('pointerup', up);
      window.removeEventListener('pointercancel', up);
      s.base = s.base + s.dx < -total / 2 ? -total : 0;
      setX(s.base);
      setDrag(false);
      if (s.moved) {
        s.block = true;
        setTimeout(() => (s.block = false), 50);
      }
    };
    window.addEventListener('pointermove', move);
    window.addEventListener('pointerup', up);
    window.addEventListener('pointercancel', up);
  };

  const close = () => {
    st.current.base = 0;
    setX(0);
  };

  const inner = (a) => (
    <>
      <Icon name={a.icon} size={22} />
      {a.label}
    </>
  );

  return (
    <div
      className={`swipe ${drag ? 'drag' : ''}`}
      onClickCapture={(e) => {
        if (st.current.block) {
          e.stopPropagation();
          e.preventDefault();
        }
      }}
    >
      <div className="under">
        {actions.map((a) =>
          a.href ? (
            <a key={a.key} href={a.href} style={{ background: a.color }} onClick={close}>
              {inner(a)}
            </a>
          ) : (
            <button
              key={a.key}
              style={{ background: a.color }}
              onClick={() => {
                close();
                a.onClick?.();
              }}
            >
              {inner(a)}
            </button>
          ),
        )}
      </div>
      <div className="top" style={{ transform: `translateX(${x}px)` }} onPointerDown={onDown}>
        {children}
      </div>
    </div>
  );
}
