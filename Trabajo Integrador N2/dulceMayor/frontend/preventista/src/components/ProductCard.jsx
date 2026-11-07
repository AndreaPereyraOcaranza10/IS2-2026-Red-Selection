import { Candy, Icon, fmt } from '@dulcemayor/shared';

/** Tarjeta de producto del catálogo. Con cantidad > 0 muestra el selector +/-; si no, el botón "agregar". */
export default function ProductCard({ product, quantity, onAdd, onSub }) {
  const low = product.stockLevel === 'LOW';
  return (
    <article className="card prod">
      <div className="photo">
        <Candy size={40} />
      </div>
      <div className="grow">
        <h3 style={{ fontSize: 16, lineHeight: '24px', fontWeight: 700 }}>{product.name}</h3>
        <p className="sec small">{product.presentation}</p>
        <span className={`tag ${low ? 'low' : 'ok'}`}>{low ? 'Poco stock' : 'Disponible'}</span>
        <div className="price">{fmt(product.price)}</div>
      </div>
      {quantity ? (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
          <button className="icon-btn" style={{ background: 'var(--primary)', color: '#fff' }} onClick={onAdd} aria-label={`Sumar ${product.name}`}>
            <Icon name="plus" />
          </button>
          <b>{quantity}</b>
          <button className="icon-btn pri" onClick={onSub} aria-label={`Restar ${product.name}`}>
            <Icon name="minus" />
          </button>
        </div>
      ) : (
        <button className="icon-btn" style={{ background: 'var(--primary)', color: '#fff' }} onClick={onAdd} aria-label={`Agregar ${product.name}`}>
          <Icon name="plus" />
        </button>
      )}
    </article>
  );
}
