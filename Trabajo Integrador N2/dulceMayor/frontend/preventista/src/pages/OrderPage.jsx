import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ActionBar, Candy, Dialog, EmptyState, Icon, Layout, QtyControl, Sheet, SwipeRow, fmt, useAsync, useToast } from '@dulcemayor/shared';
import { preventistaApi } from '../api.js';
import { useCart } from '../context/CartContext.jsx';
import PreventistaNav from '../components/PreventistaNav.jsx';

/** Nuevo pedido: punto de venta, líneas editables, código de promoción y confirmación. */
export default function OrderPage() {
  const navigate = useNavigate();
  const toast = useToast();
  const cart = useCart();
  const zone = useAsync(preventistaApi.zone, []);
  const [pickOpen, setPickOpen] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [code, setCode] = useState(cart.promo.code);
  const [busy, setBusy] = useState(false);

  const { promo, pointOfSale: pos } = cart;
  const promoState = promo.status === 'ok' ? 'ok' : promo.status === 'bad' ? 'err' : '';
  const canConfirm = cart.lines.length > 0 && !!pos;

  const applyPromo = async () => {
    try {
      const st = await cart.applyPromo(code);
      if (st === 'ok') toast('Código aplicado');
      else if (st === 'bad') toast('El código no se puede usar', { error: true });
    } catch (e) {
      toast(e.message, { error: true });
    }
  };

  const confirm = async () => {
    setBusy(true);
    try {
      const res = await preventistaApi.createOrder({
        pointOfSaleId: pos.id,
        // Solo si aplica: un código válido que todavía no da descuento (falta cantidad o producto) el backend lo rechazaría
        promoCode: promo.status === 'ok' && cart.discount > 0 ? promo.code : null,
        items: cart.lines.map((l) => ({ productId: l.product.id, quantity: l.quantity })),
      });
      cart.clear();
      navigate('/pedido-confirmado', { replace: true, state: res }); // { order, nextStop }
    } catch (e) {
      setConfirmOpen(false);
      toast(e.message, { error: true });
    } finally {
      setBusy(false);
    }
  };

  return (
    <Layout
      title="Nuevo pedido"
      nav={<PreventistaNav />}
      bar={
        <div className="bar">
          <div className="sum">
            <span className="sec">Subtotal</span>
            <b style={{ textAlign: 'right' }}>{fmt(cart.subtotal)}</b>
            <span className="sec">Descuento</span>
            <b style={{ textAlign: 'right', color: 'var(--ok)' }}>{cart.discount ? `− ${fmt(cart.discount)}` : '$ 0'}</b>
            <b style={{ fontSize: 18 }}>Total</b>
            <span className="tot" style={{ textAlign: 'right' }}>{fmt(cart.total)}</span>
          </div>
          <button className="btn btn-p" disabled={!canConfirm} onClick={() => setConfirmOpen(true)}>
            <Icon name="check" size={20} />
            Confirmar pedido
          </button>
        </div>
      }
    >
      <div className="cols rev">
        <div className="col">
          <div className="field">
            <label>Punto de venta</label>
            <button className="input" style={{ textAlign: 'left', width: '100%' }} onClick={() => setPickOpen(true)} aria-haspopup="dialog">
              <Icon name="store" />
              <span className="grow">
                {pos ? (
                  <>
                    <b>{pos.name}</b>
                    <br />
                    <span className="cap muted">{pos.address}</span>
                  </>
                ) : (
                  <span className="muted">Elegí el punto de venta</span>
                )}
              </span>
              <Icon name="chev" />
            </button>
          </div>

          <h2>Productos ({cart.count})</h2>
          {cart.lines.length ? (
            <>
              {cart.lines.map(({ product: p, quantity }) => (
                <SwipeRow key={p.id} actions={[{ key: 'del', label: 'Quitar', icon: 'trash', color: 'var(--err)', onClick: () => { cart.remove(p.id); toast('Producto quitado del pedido'); } }]}>
                  <article className="card row">
                    <div className="photo" style={{ width: 56, height: 56 }}>
                      <Candy size={28} />
                    </div>
                    <div className="grow">
                      <b>{p.name}</b>
                      <p className="small sec">{fmt(p.price)} · {p.presentation}</p>
                      <b style={{ color: 'var(--primary)' }}>{fmt(p.price * quantity)}</b>
                    </div>
                    <QtyControl value={quantity} onInc={() => cart.add(p.id)} onDec={() => cart.sub(p.id)} />
                  </article>
                </SwipeRow>
              ))}
              <p className="hint">Deslizá un producto hacia la izquierda para quitarlo.</p>
            </>
          ) : (
            <EmptyState icon="cart" title="Todavía no cargaste productos" action="Ir al catálogo" onAction={() => navigate('/catalogo', { replace: true })} />
          )}
        </div>

        <div className="col">
          <div className={`field ${promoState}`}>
            <label htmlFor="pc">Código de promoción</label>
            <div className="input">
              <Icon name="tag" />
              <input id="pc" value={code} placeholder="Ej.: DULCE10" autoCapitalize="characters" onChange={(e) => setCode(e.target.value)} />
              <button className="linkbtn small" style={{ marginRight: -8 }} onClick={applyPromo}>Aplicar</button>
            </div>
            <span className="help">
              {promo.status === 'ok'
                ? cart.promoHint ?? `Código aplicado: ${promo.percent}% de descuento${promo.type === 'PRODUCTO_DESTACADO' ? ` en ${promo.productName}` : ''}`
                : promo.status === 'bad'
                  ? promo.message ?? 'El código no es válido o venció. Revisalo e intentá de nuevo.'
                  : 'Probá con DULCE10'}
            </span>
          </div>
        </div>
      </div>

      <Sheet open={pickOpen} onClose={() => setPickOpen(false)}>
        <h2 style={{ fontSize: 22, lineHeight: '28px' }}>Elegí el punto de venta</h2>
        <div className="col" style={{ gap: 8 }} role="radiogroup">
          {(zone.data || []).map((s) => (
            <button
              key={s.id}
              className="opt"
              role="radio"
              aria-checked={pos?.id === (s.clientId ?? s.id)}
              // El pedido va al comercio (clientId), no a la parada de la ruta (id)
              onClick={() => { cart.setPointOfSale({ id: s.clientId ?? s.id, name: s.name, address: s.address }); setPickOpen(false); }}
            >
              <span className="radio" />
              <span className="grow">
                <b>{s.name}</b>
                <br />
                <span className="small sec">{s.address}</span>
              </span>
            </button>
          ))}
        </div>
      </Sheet>

      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <h2 style={{ fontSize: 22, lineHeight: '28px' }}>¿Confirmar el pedido?</h2>
        <p className="sec">
          Se carga para <b>{pos?.name}</b> por <b>{fmt(cart.total)}</b>. Después no vas a poder editarlo.
        </p>
        <div className="col" style={{ gap: 8, marginTop: 8 }}>
          <button className="btn btn-p" onClick={confirm} disabled={busy}>
            <Icon name="check" size={20} />
            {busy ? 'Cargando…' : 'Sí, confirmar'}
          </button>
          <button className="btn btn-s" onClick={() => setConfirmOpen(false)}>Seguir editando</button>
        </div>
      </Dialog>
    </Layout>
  );
}
