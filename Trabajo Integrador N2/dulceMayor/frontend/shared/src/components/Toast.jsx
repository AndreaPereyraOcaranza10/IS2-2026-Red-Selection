import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { Icon } from './Icon.jsx';

const ToastCtx = createContext(() => {});

/** Snackbar: `toast('mensaje')` o `toast('mensaje', { error: true })`. */
export function ToastProvider({ children }) {
  const [item, setItem] = useState(null);
  const timer = useRef();

  const toast = useCallback((message, { error = false } = {}) => {
    clearTimeout(timer.current);
    setItem({ message, error });
    timer.current = setTimeout(() => setItem(null), 3200);
  }, []);

  useEffect(() => () => clearTimeout(timer.current), []);
  const value = useMemo(() => toast, [toast]);

  return (
    <ToastCtx.Provider value={value}>
      {children}
      {item && (
        <div className={`toast ${item.error ? 'err' : ''}`} role="status">
          <span className="ti">
            <Icon name={item.error ? 'x' : 'check'} size={16} />
          </span>
          <span className="grow">{item.message}</span>
        </div>
      )}
    </ToastCtx.Provider>
  );
}

export const useToast = () => useContext(ToastCtx);
