import { useEffect, useMemo, useState } from 'react';
import { ArrowDown, ArrowUpDown, Headphones, ShoppingBag } from 'lucide-react';
import { products as demoProducts, type Product } from './data/products';
import { api } from './lib/api';
import { Header, SearchBar } from './components/Header';
import { Hero } from './components/Hero';
import { CategoryBar } from './components/CategoryBar';
import { ProductCard } from './components/ProductCard';
import { Branches } from './components/Branches';
import { CartPanel } from './components/CartPanel';
import { Footer } from './components/Footer';
import { ProductDetail } from './components/ProductDetail';
import { OrdersPage } from './components/OrdersPage';
import { ProfilePage } from './components/ProfilePage';
import { LoginPage } from './components/LoginPage';
import { OperationsDashboard } from './components/OperationsDashboard';

type Screen = 'home' | 'detail' | 'orders' | 'profile' | 'login' | 'operations';

function App() {
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('Ver todo');
  const [quantities, setQuantities] = useState<Record<string, number>>({});
  const [cartOpen, setCartOpen] = useState(false);
  const [screen, setScreen] = useState<Screen>('home');
  const [catalog, setCatalog] = useState<Product[]>(demoProducts);
  const [selectedProduct, setSelectedProduct] = useState<Product>(demoProducts[0]);
  const [account, setAccount] = useState<{ email: string; role: string; displayName: string } | null>(null);
  const [apiNotice, setApiNotice] = useState('');
  const [checkoutError, setCheckoutError] = useState('');
  const [checkingOut, setCheckingOut] = useState(false);

  useEffect(() => { api.products().then(setCatalog).catch(() => setApiNotice('Mostrando catálogo de demostración.')); }, []);
  const count = Object.values(quantities).reduce((total, quantity) => total + quantity, 0);
  const filtered = useMemo(() => catalog.filter(product =>
    (category === 'Ver todo' || product.category === category) &&
    `${product.name} ${product.brand} ${product.category}`.toLowerCase().includes(search.toLowerCase())), [catalog, category, search]);
  const addProduct = (id: string | number) => setQuantities(previous => ({ ...previous, [String(id)]: (previous[String(id)] || 0) + 1 }));
  const setPage = (next: Screen) => { setScreen(next); window.scrollTo({ top: 0, behavior: 'smooth' }); };
  const goHome = () => setPage('home');
  const openDetail = (id: string | number) => {
    setSelectedProduct(catalog.find(product => String(product.id) === String(id)) || demoProducts[0]);
    setPage('detail');
  };
  const handleOrder = async () => {
    if (!account) { setCartOpen(false); setPage('login'); return; }
    setCheckingOut(true);
    setCheckoutError('');
    try {
      await api.createOrder(Object.entries(quantities).map(([productId, quantity]) => ({ productId, quantity })));
      setQuantities({}); setCartOpen(false); setPage('orders');
    } catch (error) { setCheckoutError(error instanceof Error ? error.message : 'No se pudo enviar el pedido.'); }
    finally { setCheckingOut(false); }
  };

  return <>
    {screen !== 'operations' && <Header count={count} account={account} onCartClick={() => setCartOpen(true)} onOrdersClick={() => setPage('orders')} onHomeClick={goHome} onLoginClick={() => setPage('login')} onProfileClick={() => setPage('profile')} onAdminClick={() => setPage('operations')} />}
    {screen === 'detail' ? <ProductDetail product={selectedProduct} onBack={goHome} onAdd={amount => { setQuantities(previous => ({ ...previous, [String(selectedProduct.id)]: (previous[String(selectedProduct.id)] || 0) + amount })); setCartOpen(true); }} />
      : screen === 'orders' ? <OrdersPage onBack={goHome} onProfile={() => setPage('profile')} />
        : screen === 'profile' ? <ProfilePage onBack={goHome} />
        : screen === 'login' ? <LoginPage onSuccess={async (email, password) => { const session = await api.login(email, password); setAccount(session); setPage(['ADMIN', 'ALMACENERO', 'JEFE_ADMINISTRATIVO'].includes(session.role) ? 'operations' : 'home'); }} onStaff={() => setPage('operations')} />
          : screen === 'operations' ? <OperationsDashboard onBack={goHome} />
            : <>
              <main><Hero /><section className="catalog-section" id="catalogo">
                <div className="section-heading catalog-heading"><div><span className="section-kicker">LO QUE TU NEGOCIO NECESITA</span><h2>El catálogo.</h2></div><span className="catalog-note"><i /> Precios exclusivos mayoristas</span></div>
                {apiNotice && <p className="catalog-notice" role="status">{apiNotice}</p>}
                <div className="catalog-tools"><SearchBar value={search} onChange={setSearch} /><button className="sort-button"><ArrowUpDown size={16} /> Más vendidos <ArrowDown size={14} /></button></div>
                <CategoryBar active={category} onChange={setCategory} />
                <div className="product-grid">{filtered.map(product => <ProductCard key={product.id} product={product} quantity={quantities[String(product.id)] || 0} onAdd={() => addProduct(product.id)} onDetails={() => openDetail(product.id)} />)}</div>
                {!filtered.length && <div className="no-results">No hay productos disponibles para esta búsqueda o categoría.</div>}
                <div className="catalog-bottom"><span>Mostrando <b>{filtered.length}</b> de <b>2.000+</b> productos</span><button>Ver catálogo completo <ArrowDown size={16} /></button></div>
              </section><Branches /></main><Footer />
            </>}
    {screen !== 'login' && <button className="help-button" aria-label="Contactar con atención al cliente"><Headphones size={19} /><span>¿Necesitás ayuda?</span></button>}
    {count > 0 && <button className="mobile-cart" onClick={() => setCartOpen(true)}><ShoppingBag size={18} /> Presupuesto ({count})</button>}
    <CartPanel open={cartOpen} onClose={() => setCartOpen(false)} products={catalog} quantities={quantities} onCheckout={handleOrder} error={checkoutError} submitting={checkingOut} />
  </>;
}

export default App;
