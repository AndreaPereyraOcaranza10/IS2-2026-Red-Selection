import { LayoutDashboard, Menu, Search, ShoppingBag, UserRound, X } from 'lucide-react';
import { useState } from 'react';
import { DollarQuoteBadge } from './DollarQuoteBadge';

type HeaderProps = {
  count: number;
  account?: { displayName: string; role: string } | null;
  onCartClick: () => void;
  onOrdersClick: () => void;
  onHomeClick: () => void;
  onLoginClick: () => void;
  onProfileClick: () => void;
  onAdminClick: () => void;
};

const administrativeRoles = ['ADMIN', 'ALMACENERO', 'JEFE_ADMINISTRATIVO'];

export function Header({ count, account, onCartClick, onOrdersClick, onHomeClick, onLoginClick, onProfileClick, onAdminClick }: HeaderProps) {
  const [menuOpen, setMenuOpen] = useState(false);
  const canManage = Boolean(account && administrativeRoles.includes(account.role));

  return <header className="site-header"><div className="header-inner">
    <a className="brand" href="#inicio" aria-label="Dulce Mayor, inicio"><span className="brand-mark">DM</span><span className="brand-copy"><strong>Dulce Mayor</strong><small>MAYORISTA B2B &middot; MENDOZA</small></span></a>
    <DollarQuoteBadge />
    <nav className={menuOpen ? 'main-nav nav-open' : 'main-nav'}>
      <a href="#catalogo" onClick={() => { onHomeClick(); setMenuOpen(false); }}>Cat&aacute;logo</a>
      <button className="nav-link" onClick={() => { onOrdersClick(); setMenuOpen(false); }}>Mis pedidos</button>
      <a href="#sucursales" onClick={() => { onHomeClick(); setMenuOpen(false); }}>Sucursales</a>
      <a href="#ayuda" onClick={() => { onHomeClick(); setMenuOpen(false); }}>Ayuda</a>
      {canManage && <button className="nav-link mobile-admin" onClick={() => { onAdminClick(); setMenuOpen(false); }}><LayoutDashboard size={15} /> Panel administrativo</button>}
      <button className="nav-link mobile-login" onClick={() => { account ? onProfileClick() : onLoginClick(); setMenuOpen(false); }}>{account ? 'Mi perfil' : 'Ingresar a mi cuenta'}</button>
    </nav>
    <div className="header-actions">
      {canManage && <button className="admin-shortcut" onClick={onAdminClick} aria-label="Ir al panel administrativo" title="Panel administrativo"><LayoutDashboard size={16} /><span>Panel admin</span></button>}
      {account ? <button className="login-button profile-button" onClick={onProfileClick} aria-label={`Mi perfil${account.displayName ? `: ${account.displayName}` : ''}`} title={account.displayName}><UserRound size={17} /> <span>Mi perfil</span></button> : <button className="login-button" onClick={onLoginClick}><UserRound size={17} /> <span>Ingresar</span></button>}
      <button className="cart-button" onClick={onCartClick}><ShoppingBag size={17} /><span>Presupuesto</span><b>{count}</b></button>
      <button className="menu-toggle" onClick={() => setMenuOpen(!menuOpen)} aria-label={menuOpen ? 'Cerrar menu' : 'Abrir menu'}>{menuOpen ? <X /> : <Menu />}</button>
    </div>
  </div></header>;
}

export function SearchBar({ value, onChange }: { value: string; onChange: (value: string) => void }) {
  return <label className="search-field"><Search size={19} /><input value={value} onChange={e => onChange(e.target.value)} placeholder="Buscar por producto, marca o EAN..." /><kbd>&#8984; K</kbd></label>;
}
