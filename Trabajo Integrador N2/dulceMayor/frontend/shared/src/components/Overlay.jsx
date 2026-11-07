import { useEffect } from 'react';

function useEscape(open, onClose) {
  useEffect(() => {
    if (!open) return undefined;
    const h = (e) => e.key === 'Escape' && onClose();
    window.addEventListener('keydown', h);
    return () => window.removeEventListener('keydown', h);
  }, [open, onClose]);
}

/** Hoja inferior (bottom sheet). Se cierra tocando el fondo o con Esc. */
export function Sheet({ open, onClose, children }) {
  useEscape(open, onClose);
  if (!open) return null;
  return (
    <div className="layer" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="sheet" role="dialog" aria-modal="true">
        <div className="handle" />
        {children}
      </div>
    </div>
  );
}

/** Diálogo de confirmación centrado. */
export function Dialog({ open, onClose, children }) {
  useEscape(open, onClose);
  if (!open) return null;
  return (
    <div className="layer center" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="dialog" role="alertdialog" aria-modal="true">
        {children}
      </div>
    </div>
  );
}
