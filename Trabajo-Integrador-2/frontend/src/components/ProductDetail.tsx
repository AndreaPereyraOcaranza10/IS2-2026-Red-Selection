import { useState } from 'react';
import { ArrowLeft, BadgeCheck, Check, ChevronRight, Package, ShieldCheck, ShoppingBag, Store, Truck } from 'lucide-react';
import type { Product } from '../data/products';

const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 });
export function ProductDetail({ product, onBack, onAdd }: { product: Product; onBack: () => void; onAdd: (quantity: number) => void }) {
  const [tier, setTier] = useState(2);
  const [quantity, setQuantity] = useState(1);
  const tiers = [
    { name: 'Unidad', detail: '1 unidad', price: Math.round(product.unitPrice), saving: '' },
    { name: 'Bulto cerrado', detail: product.pack, price: product.price, saving: '' },
    { name: 'Bulto pallet', detail: '6 bultos', price: Math.round(product.price * 6 * 0.9), saving: '-10%' },
  ];
  const selected = tiers[tier];
  const total = selected.price * quantity;
  return <main className="detail-page"><button className="back-link" onClick={onBack}><ArrowLeft size={16} /> Volver al catálogo</button><div className="breadcrumbs">Inicio <ChevronRight /> Catálogo <ChevronRight /> {product.category} <ChevronRight /> <b>{product.name}</b></div><div className="detail-layout"><div className="detail-gallery"><div className="detail-image" style={{ background: product.color }}><span className="detail-badge"><BadgeCheck size={14} /> Precio mayorista</span><img src={product.image} alt={product.name} /></div><div className="detail-thumbs"><button className="thumb-active"><img src={product.image} alt="Vista principal" /></button><button><Package /></button><button><ShoppingBag /></button></div></div><section className="detail-info"><div className="detail-brand">{product.brand.toUpperCase()} <span>·</span> {product.category.toUpperCase()}</div><h1>{product.name}</h1><p className="detail-subtitle">{product.pack}</p><div className="detail-rating"><span>★★★★★</span> <b>4.8</b> <small>(32 reseñas de comercios)</small></div><div className="detail-divider"/><div className="tier-heading"><b>Presentación de venta</b><span>Mayor volumen = mayor margen</span></div><div className="tier-grid">{tiers.map((option, index) => <button key={option.name} className={tier === index ? 'tier-option selected' : 'tier-option'} onClick={() => setTier(index)}><span className="tier-radio">{tier === index && <i />}</span><span className="tier-name">{option.name}{option.saving && <small>{option.saving}</small>}<em>{option.detail}</em></span><strong>{money.format(option.price)}</strong></button>)}</div><div className="quantity-total"><div><span>Cantidad:</span><div className="quantity-stepper"><button aria-label="Restar" onClick={() => setQuantity(Math.max(1, quantity - 1))}>−</button><b>{quantity}</b><button aria-label="Sumar" onClick={() => setQuantity(quantity + 1)}>+</button></div><small>bultos máster</small></div><div><span>Total estimado neto:</span><strong>{money.format(total)}</strong></div></div><button className="detail-add" onClick={() => onAdd(quantity)}><ShoppingBag size={18} /> Agregar al Presupuesto Mayorista</button><div className="shipping-points"><span><Store /> Retiro sin cargo hoy</span><span><Truck /> Flete 24h bonificado</span><span><ShieldCheck /> Factura A y B</span></div></section></div><section className="detail-more"><h2>Productos que también te pueden interesar</h2><div>{['Alfajor Triple Chocolate', 'Mogul Dientes Gomitas', 'Chocolate Block 38g', 'Beldent Menta Infinito'].map(name => <span key={name}><Check size={14} /> {name}</span>)}</div></section></main>;
}

