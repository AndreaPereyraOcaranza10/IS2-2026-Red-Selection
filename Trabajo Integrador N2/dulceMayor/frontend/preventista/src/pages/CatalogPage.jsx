import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Async, EmptyState, Icon, IconButton, Layout, Sheet } from '@dulcemayor/shared';
import { CATEGORIES } from '../config.js';
import { useCart } from '../context/CartContext.jsx';
import PreventistaNav from '../components/PreventistaNav.jsx';
import ProductCard from '../components/ProductCard.jsx';

/** Catálogo con búsqueda y filtro por categoría. El backend ya excluye los productos sin stock. */
export default function CatalogPage() {
  const navigate = useNavigate();
  const cart = useCart();
  const [q, setQ] = useState('');
  const [category, setCategory] = useState('Todas');
  const [filterOpen, setFilterOpen] = useState(false);

  return (
    <Layout
      title="Catálogo"
      nav={<PreventistaNav />}
      actions={<IconButton icon="cart" label="Ver pedido" badge={cart.count} onClick={() => navigate('/pedido', { replace: true })} />}
    >
      <div className="searchrow">
        <div className="input grow">
          <Icon name="search" />
          <input type="search" placeholder="Buscar golosinas" aria-label="Buscar" value={q} onChange={(e) => setQ(e.target.value)} />
        </div>
        <button
          className="icon-btn"
          style={{ width: 56, height: 56, background: 'var(--surface)', boxShadow: 'inset 0 0 0 2px var(--primary)', color: 'var(--primary)' }}
          onClick={() => setFilterOpen(true)}
          aria-label="Filtrar por categoría"
        >
          <Icon name="filter" />
        </button>
      </div>

      <Async state={cart.catalog}>
        {(products) => {
          const term = q.trim().toLowerCase();
          const items = products.filter(
            (p) => p.stockLevel !== 'NONE' && (category === 'Todas' || p.category === category) && (!term || p.name.toLowerCase().includes(term)),
          );
          return (
            <>
              <div className="row small sec">
                {category !== 'Todas' && <span className="chip c-rep">{category}</span>}
                <span>{items.length} productos con stock</span>
              </div>
              {items.length ? (
                <div className="grid g2">
                  {items.map((p) => (
                    <ProductCard key={p.id} product={p} quantity={cart.quantityOf(p.id)} onAdd={() => cart.add(p.id)} onSub={() => cart.sub(p.id)} />
                  ))}
                </div>
              ) : (
                <EmptyState icon="search" title="No encontramos productos" text="Probá con otra palabra o cambiá la categoría." />
              )}
            </>
          );
        }}
      </Async>

      <Sheet open={filterOpen} onClose={() => setFilterOpen(false)}>
        <h2 style={{ fontSize: 22, lineHeight: '28px' }}>Filtrar por categoría</h2>
        <div className="row" style={{ flexWrap: 'wrap', gap: 8 }}>
          {CATEGORIES.map((c) => (
            <button key={c} className="pillbtn" aria-pressed={c === category} onClick={() => setCategory(c)}>
              {c}
            </button>
          ))}
        </div>
        <div className="row" style={{ gap: 12 }}>
          <button className="btn btn-s" onClick={() => setCategory('Todas')}>Limpiar</button>
          <button className="btn btn-p" onClick={() => setFilterOpen(false)}>Aplicar</button>
        </div>
      </Sheet>
    </Layout>
  );
}
