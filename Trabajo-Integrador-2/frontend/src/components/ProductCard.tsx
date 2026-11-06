import { Check, Plus } from 'lucide-react';
import type { Product } from '../data/products';

const currency = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 });
export function ProductCard({ product, quantity, onAdd, onDetails }: { product: Product; quantity: number; onAdd: () => void; onDetails: () => void }) {
  const inStock = product.inStock ?? (typeof product.stock === 'number' ? product.stock > 0 : product.stock === 'Disponible');
  const lowStock = typeof product.stock === 'number' ? product.stock <= (product.stockMinimum ?? 5) : product.stock === 'Últimos bultos';
  return <article className="product-card"><div className="product-image" style={{ backgroundColor: product.color }}>
    {product.tag && <span className={product.tag.includes('%') ? 'product-tag discount' : 'product-tag'}>{product.tag}</span>}
    <img src={product.image} alt={product.name} loading="lazy" />
    <span className={inStock ? lowStock ? 'stock stock-low' : 'stock stock-ok' : 'stock stock-low'}><i />{!inStock ? 'Sin stock' : lowStock ? 'Últimos bultos' : 'Disponible'}</span>
  </div><div className="product-info"><div className="product-meta"><span>{product.brand}</span><span>{product.category}</span></div><button className="product-title-button" onClick={onDetails}><h3>{product.name}</h3></button><p className="pack-size">{product.pack}</p><div className="product-price"><strong>{currency.format(product.price)}</strong><small>{currency.format(product.unitPrice)} c/u</small></div><button className={quantity ? 'add-button added' : 'add-button'} onClick={onAdd}>{quantity ? <Check size={17} /> : <Plus size={17} />}{quantity ? `Agregado · ${quantity}` : 'Agregar al presupuesto'}</button></div></article>;
}
