import { createContext, useContext, useMemo, useReducer } from 'react';
import { useAsync } from '@dulcemayor/shared';
import { preventistaApi } from '../api.js';

const Ctx = createContext(null);
const NO_PROMO = { code: '', status: 'none', percent: 0 };
const INITIAL = { items: {}, pointOfSale: null, promo: NO_PROMO };

function reducer(s, a) {
  switch (a.type) {
    case 'add':
      return { ...s, items: { ...s.items, [a.id]: (s.items[a.id] || 0) + 1 } };
    case 'sub':
      return { ...s, items: { ...s.items, [a.id]: Math.max(0, (s.items[a.id] || 0) - 1) } };
    case 'del':
      return { ...s, items: { ...s.items, [a.id]: 0 } };
    case 'pos':
      return { ...s, pointOfSale: a.pointOfSale };
    case 'promo':
      return { ...s, promo: a.promo };
    case 'clear':
      return INITIAL;
    default:
      return s;
  }
}

/** Estado del pedido en armado: punto de venta, líneas, promoción y totales. */
export function CartProvider({ children }) {
  const [state, dispatch] = useReducer(reducer, INITIAL);
  const catalog = useAsync(preventistaApi.products, []);

  const value = useMemo(() => {
    const byId = Object.fromEntries((catalog.data || []).map((p) => [p.id, p]));
    const lines = Object.entries(state.items)
      .filter(([, q]) => q > 0)
      .map(([id, quantity]) => ({ product: byId[id], quantity }))
      .filter((l) => l.product);
    const count = lines.reduce((a, l) => a + l.quantity, 0);
    const subtotal = lines.reduce((a, l) => a + l.product.price * l.quantity, 0);
    const discount = state.promo.status === 'ok' ? Math.round((subtotal * state.promo.percent) / 100) : 0;
    return {
      catalog,
      lines,
      count,
      subtotal,
      discount,
      total: subtotal - discount,
      quantityOf: (id) => state.items[id] || 0,
      pointOfSale: state.pointOfSale,
      promo: state.promo,
      add: (id) => dispatch({ type: 'add', id }),
      sub: (id) => dispatch({ type: 'sub', id }),
      remove: (id) => dispatch({ type: 'del', id }),
      setPointOfSale: (pointOfSale) => dispatch({ type: 'pos', pointOfSale }),
      clear: () => dispatch({ type: 'clear' }),
      /** Valida el código contra el backend (Strategy de promociones). Devuelve 'ok' | 'bad' | 'none'. */
      applyPromo: async (raw) => {
        const code = raw.trim().toUpperCase();
        if (!code) {
          dispatch({ type: 'promo', promo: NO_PROMO });
          return 'none';
        }
        const r = await preventistaApi.validatePromo(code);
        const promo = r.valid ? { code, status: 'ok', percent: r.discountPercent } : { code, status: 'bad', percent: 0 };
        dispatch({ type: 'promo', promo });
        return promo.status;
      },
    };
  }, [state, catalog]);

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export const useCart = () => useContext(Ctx);
