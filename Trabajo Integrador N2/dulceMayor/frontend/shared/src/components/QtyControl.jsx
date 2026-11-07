import { Icon } from './Icon.jsx';

/** Selector de cantidad con botones de 48px. */
export function QtyControl({ value, onInc, onDec }) {
  return (
    <div className="qty">
      <button className="m" onClick={onDec} disabled={value <= 0} aria-label="Restar">
        <Icon name="minus" />
      </button>
      <span className="n" aria-live="polite">
        {value}
      </span>
      <button className="p" onClick={onInc} aria-label="Sumar">
        <Icon name="plus" />
      </button>
    </div>
  );
}
