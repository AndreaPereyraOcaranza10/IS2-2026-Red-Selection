import { Icon, PAYMENT_LABEL, fmt } from '@dulcemayor/shared';

/**
 * Cómo se cobra la entrega. El texto (alias, QR, etc.) lo arma el backend según la estrategia de pago
 * (Strategy: Efectivo / Transferencia / Billetera QR), así que acá no hay alias hardcodeado.
 */
export default function PaymentNotice({ method, total, instruction }) {
  if (method === 'EFECTIVO') {
    return (
      <div className="cash">
        <Icon name="cash" size={32} />
        <div>
          <p className="small">Cobrar en efectivo</p>
          <b>{fmt(total)}</b>
        </div>
      </div>
    );
  }
  return (
    <div className="alert" style={{ background: 'var(--info-bg)', color: 'var(--info)' }}>
      <Icon name="check" />
      <p>
        <b>Pago por {PAYMENT_LABEL[method].toLowerCase()}.</b> {instruction}. No cobres en mano.
      </p>
    </div>
  );
}
