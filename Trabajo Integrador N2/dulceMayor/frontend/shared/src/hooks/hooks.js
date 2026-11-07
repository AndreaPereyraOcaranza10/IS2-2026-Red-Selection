import { useCallback, useEffect, useRef, useState } from 'react';
import { useToast } from '../components/Toast.jsx';
import { formatTime } from '../utils/format.js';

/** Carga asíncrona con estados loading / error y `reload` (sin pisar `data` mientras recarga). */
export function useAsync(fn, deps = []) {
  const [state, setState] = useState({ data: null, loading: true, error: null });
  const seq = useRef(0);

  const reload = useCallback(async () => {
    const id = ++seq.current;
    setState((s) => ({ ...s, loading: true, error: null }));
    try {
      const data = await fn();
      if (id === seq.current) setState({ data, loading: false, error: null });
      return data;
    } catch (error) {
      if (id === seq.current) setState((s) => ({ ...s, loading: false, error }));
      return null;
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => {
    reload();
  }, [reload]);

  return { ...state, reload };
}

/** Botón de actualizar + pull-to-refresh comparten esta acción. */
export function useRefresh(reload) {
  const toast = useToast();
  const [busy, setBusy] = useState(false);
  const refresh = useCallback(async () => {
    if (busy) return;
    setBusy(true);
    try {
      await reload();
      toast('Actualizado a las ' + formatTime(new Date().toISOString()));
    } finally {
      setBusy(false);
    }
  }, [busy, reload, toast]);
  return { refresh, busy };
}

/** Gesto "tirar para actualizar" sobre un contenedor scrolleable. */
export function usePullToRefresh(ref, onRefresh) {
  useEffect(() => {
    const el = ref.current;
    if (!el || !onRefresh) return undefined;
    let y0 = null;
    let pulled = false;
    const start = (e) => {
      y0 = el.scrollTop === 0 ? e.touches[0].clientY : null;
      pulled = false;
    };
    const move = (e) => {
      if (y0 === null) return;
      if (e.touches[0].clientY - y0 > 80 && !pulled) {
        pulled = true;
        onRefresh();
      }
    };
    el.addEventListener('touchstart', start, { passive: true });
    el.addEventListener('touchmove', move, { passive: true });
    return () => {
      el.removeEventListener('touchstart', start);
      el.removeEventListener('touchmove', move);
    };
  }, [ref, onRefresh]);
}
