import { useEffect, useState } from 'react';

/**
 * Carga una lista que depende de otro valor (desplegables en cascada, búsquedas).
 * `loader(parentId)` tiene que ser una función estable (definida fuera del componente).
 * Con `enabled: false` devuelve una lista vacía y no consulta.
 */
export function useOptions(loader, parentId, { enabled = true } = {}) {
  const [state, setState] = useState({ options: [], loading: enabled, error: null });

  useEffect(() => {
    if (!enabled) {
      setState({ options: [], loading: false, error: null });
      return undefined;
    }
    let alive = true;
    setState((s) => ({ ...s, loading: true, error: null }));
    loader(parentId)
      .then((options) => alive && setState({ options, loading: false, error: null }))
      .catch((error) => alive && setState({ options: [], loading: false, error }));
    return () => {
      alive = false;
    };
  }, [loader, parentId, enabled]);

  return state;
}

/** El valor se actualiza recién cuando el usuario deja de tipear `ms` milisegundos. */
export function useDebounced(value, ms = 300) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => setDebounced(value), ms);
    return () => clearTimeout(t);
  }, [value, ms]);
  return debounced;
}