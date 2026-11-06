import { useCallback, useEffect, useMemo, useState, type FormEvent, type ReactNode } from 'react';
import { AlertTriangle, ArrowLeft, ArrowRight, BadgeCheck, Bell, Barcode, Building2, CheckCircle2, ChevronDown, CircleHelp, ClipboardList, Clock3, FileText, LayoutDashboard, LoaderCircle, MapPin, MoreVertical, Package, PackageCheck, Plus, RefreshCw, Search, Settings, ShoppingCart, Truck, Warehouse, X } from 'lucide-react';
import { api, type CustomerOrder } from '../lib/api';
import type { Product } from '../data/products';
import { BranchesAdmin } from './BranchesAdmin';

type Section = 'Inicio' | 'Productos' | 'Stock' | 'Pedidos' | 'Facturación y pagos' | 'Despacho' | 'Mi cuenta';
type OperationsDashboardProps = { onBack: () => void };
type SectionKey = Section | 'Sucursales';

const navItems: { label: SectionKey; icon: typeof LayoutDashboard }[] = [
  { label: 'Inicio', icon: LayoutDashboard }, { label: 'Productos', icon: Package }, { label: 'Stock', icon: Warehouse },
  { label: 'Pedidos', icon: ShoppingCart }, { label: 'Facturación y pagos', icon: FileText }, { label: 'Despacho', icon: Truck }, { label: 'Mi cuenta', icon: Settings },
  { label: 'Sucursales', icon: Building2 },
];
const stateLabels: Record<string, string> = { RECIBIDO: 'Cargado', EN_PREPARACION: 'En preparación', LISTO_FACTURAR: 'Listo facturar', FACTURADO: 'Facturado', EN_CAMINO: 'En camino', ENTREGADO: 'Entregado', CANCELADO: 'Cancelado' };
const nextState: Record<string, string> = { RECIBIDO: 'EN_PREPARACION', EN_PREPARACION: 'LISTO_FACTURAR', LISTO_FACTURAR: 'FACTURADO', FACTURADO: 'EN_CAMINO', EN_CAMINO: 'ENTREGADO' };
const currency = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 });
const moneyCompact = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', notation: 'compact', maximumFractionDigits: 2 });
const orderId = (id: string) => `#DM-${id.slice(0, 5).toUpperCase()}`;
const orderDate = (date: string) => new Date(date).toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' });
const isInvoiced = (order: CustomerOrder) => Boolean(order.invoicedAt) || ['FACTURADO', 'EN_CAMINO', 'ENTREGADO'].includes(order.status);

function ManualOrderModal({ products, onClose, onCreated }: { products: Product[]; onClose: () => void; onCreated: () => void }) {
  const [client, setClient] = useState('');
  const [lines, setLines] = useState([{ productId: '', quantity: 1 }]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const items = lines.filter(line => line.productId && line.quantity > 0);
    if (!items.length) { setError('Agregá al menos un producto al pedido.'); return; }
    setSaving(true); setError('');
    try {
      await api.createManualOrder(client.trim(), items);
      onCreated();
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudo crear el pedido.'); }
    finally { setSaving(false); }
  };
  return <div className="manual-modal-backdrop" onMouseDown={event => { if (event.target === event.currentTarget) onClose(); }}><section className="manual-modal" role="dialog" aria-modal="true" aria-labelledby="manual-order-title"><header><div><span className="section-kicker">OPERACIONES</span><h2 id="manual-order-title">Nuevo pedido manual</h2></div><button onClick={onClose} aria-label="Cerrar"><X /></button></header><form onSubmit={submit}><label>Cliente / comercio<input value={client} onChange={event => setClient(event.target.value)} placeholder="Nombre del comercio" required /></label><div className="manual-lines-heading"><b>Productos</b><button type="button" onClick={() => setLines(previous => [...previous, { productId: '', quantity: 1 }])}><Plus /> Agregar producto</button></div>{lines.map((line, index) => <div className="manual-line" key={index}><select aria-label="Producto" value={line.productId} onChange={event => setLines(previous => previous.map((item, i) => i === index ? { ...item, productId: event.target.value } : item))} required><option value="">Seleccioná un producto</option>{products.map(product => <option value={String(product.id)} key={product.id}>{product.name} · {currency.format(product.price)}</option>)}</select><input aria-label="Cantidad" type="number" min="1" value={line.quantity} onChange={event => setLines(previous => previous.map((item, i) => i === index ? { ...item, quantity: Number(event.target.value) } : item))} required /><button type="button" aria-label="Quitar producto" disabled={lines.length === 1} onClick={() => setLines(previous => previous.filter((_, i) => i !== index))}><X /></button></div>)}{error && <p className="manual-error" role="alert">{error}</p>}<footer><button type="button" className="ops-secondary" onClick={onClose}>Cancelar</button><button className="ops-primary" disabled={saving}>{saving ? <LoaderCircle className="spin" /> : <Plus />}{saving ? 'Creando...' : 'Crear pedido'}</button></footer></form></section></div>;
}

function OrdersTable({ orders, onAdvance }: { orders: CustomerOrder[]; onAdvance: (order: CustomerOrder) => void }) {
  const [query, setQuery] = useState('');
  const visible = useMemo(() => orders.filter(order => `${order.id} ${order.clientName} ${stateLabels[order.status] || order.status}`.toLowerCase().includes(query.toLowerCase())), [orders, query]);
  return <section className="orders-table-card ops-orders-table"><header className="table-title"><div><h2>Pedidos pendientes de procesar</h2><p>Cola de preparación, facturación y despacho</p></div><b>{visible.length} pedidos</b></header><div className="table-toolbar"><label><Search /><input value={query} onChange={event => setQuery(event.target.value)} placeholder="Filtrar pedidos o clientes..." /></label></div><div className="ops-table-wrap"><table className="ops-table"><thead><tr><th>N° pedido · Fecha</th><th>Cliente</th><th>Unidades / Total</th><th>Estado de stock</th><th>Estado</th><th>Acción</th></tr></thead><tbody>{visible.map(order => <tr key={order.id}><td><b>{orderId(order.id)}</b><small>{orderDate(order.createdAt)}</small></td><td><b>{order.clientName}</b><small><MapPin /> Cuenta asociada</small></td><td><b>{currency.format(order.total)}</b><small>{order.items.reduce((sum, item) => sum + item.quantity, 0)} unidades</small></td><td><span className="table-stock"><CheckCircle2 /> Stock reservado</span></td><td><span className={`table-state ${order.status === 'LISTO_FACTURAR' ? 'state-invoice' : order.status === 'CANCELADO' ? 'state-stock' : 'state-loaded'}`}>{stateLabels[order.status] || order.status}</span></td><td>{nextState[order.status] ? <button className="row-action" onClick={() => onAdvance(order)}>{order.status === 'LISTO_FACTURAR' ? 'Facturar' : order.status === 'FACTURADO' ? 'Despachar' : 'Procesar'} <ArrowRight /></button> : <span className="ops-muted">—</span>}</td></tr>)}</tbody></table>{visible.length === 0 && <div className="ops-empty">No hay pedidos que coincidan.</div>}</div></section>;
}

function InventoryTable({ products, onStockChange, stockOnly = false }: { products: Product[]; onStockChange: (product: Product, stock: number) => void; stockOnly?: boolean }) {
  const [query, setQuery] = useState('');
  const visible = products.filter(product => (!stockOnly || Number(product.stock) <= (product.stockMinimum || 0)) && `${product.name} ${product.brand} ${product.category}`.toLowerCase().includes(query.toLowerCase()));
  return <section className="orders-table-card ops-inventory-table"><header className="table-title"><div><h2>{stockOnly ? 'Alertas de stock' : 'Catálogo de productos'}</h2><p>{stockOnly ? 'Productos que alcanzaron su nivel mínimo de inventario' : 'Productos registrados y disponibilidad actual'}</p></div><b>{visible.length} productos</b></header><div className="table-toolbar"><label><Search /><input value={query} onChange={event => setQuery(event.target.value)} placeholder="Buscar productos..." /></label></div><div className="ops-table-wrap"><table className="ops-table"><thead><tr><th>Producto</th><th>Marca / categoría</th><th>Precio</th><th>Stock actual</th><th>Stock mínimo</th><th>Acción</th></tr></thead><tbody>{visible.map(product => <tr key={product.id}><td><b>{product.name}</b><small>{product.pack}</small></td><td>{product.brand}<small>{product.category}</small></td><td><b>{currency.format(product.price)}</b></td><td><span className={Number(product.stock) <= (product.stockMinimum || 0) ? 'table-state state-stock' : 'table-state state-loaded'}>{product.stock} u.</span></td><td>{product.stockMinimum} u.</td><td><div className="stock-stepper"><button aria-label={`Reducir stock de ${product.name}`} disabled={Number(product.stock) <= 0} onClick={() => onStockChange(product, Number(product.stock) - 1)}>−</button><button aria-label={`Aumentar stock de ${product.name}`} onClick={() => onStockChange(product, Number(product.stock) + 1)}>+</button></div></td></tr>)}</tbody></table>{visible.length === 0 && <div className="ops-empty">No hay productos que coincidan.</div>}</div></section>;
}

export function OperationsDashboard({ onBack }: OperationsDashboardProps) {
  const [section, setSection] = useState<SectionKey>('Inicio');
  const [orders, setOrders] = useState<CustomerOrder[]>([]);
  const [inventory, setInventory] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');
  const [showAlerts, setShowAlerts] = useState(true);
  const [manualOrderOpen, setManualOrderOpen] = useState(false);

  const loadData = useCallback(async (quiet = false) => {
    if (quiet) setRefreshing(true); else setLoading(true);
    setError('');
    const [ordersResult, inventoryResult] = await Promise.allSettled([api.orders(), api.inventory()]);
    if (ordersResult.status === 'fulfilled') setOrders(ordersResult.value);
    else setError(ordersResult.reason instanceof Error ? ordersResult.reason.message : 'No se pudieron cargar los pedidos.');
    if (inventoryResult.status === 'fulfilled') setInventory(inventoryResult.value);
    else setError(inventoryResult.reason instanceof Error ? inventoryResult.reason.message : 'No se pudo cargar el inventario.');
    setLoading(false); setRefreshing(false);
  }, []);
  useEffect(() => { void loadData(); }, [loadData]);

  const todayOrders = orders.filter(order => new Date(order.createdAt).toDateString() === new Date().toDateString());
  const pendingOrders = orders.filter(order => !['ENTREGADO', 'CANCELADO'].includes(order.status));
  const toProcess = orders.filter(order => ['RECIBIDO', 'EN_PREPARACION', 'LISTO_FACTURAR'].includes(order.status));
  const invoicedToday = orders.filter(order => isInvoiced(order) && new Date(order.invoicedAt || order.createdAt).toDateString() === new Date().toDateString());
  const invoicedTotal = invoicedToday.reduce((sum, order) => sum + order.total, 0);
  const lowStockProducts = inventory.filter(product => Number(product.stock) <= (product.stockMinimum || 0));
  const inTransit = orders.filter(order => order.status === 'EN_CAMINO');
  const deliveredToday = orders.filter(order => order.status === 'ENTREGADO' && new Date(order.createdAt).toDateString() === new Date().toDateString()).length;

  const advanceOrder = async (order: CustomerOrder) => {
    const status = nextState[order.status];
    if (!status) return;
    try {
      const updated = await api.updateOrderStatus(order.id, status) as CustomerOrder;
      setOrders(previous => previous.map(item => item.id === order.id ? updated : item));
      setNotice(`${orderId(order.id)} actualizado: ${stateLabels[status]}.`);
    } catch (cause) { setNotice(cause instanceof Error ? cause.message : 'No se pudo actualizar el pedido.'); }
  };
  const changeStock = async (product: Product, stock: number) => {
    try {
      const updated = await api.updateStock(product.id, stock);
      setInventory(previous => previous.map(item => item.id === product.id ? updated : item));
      setNotice(`Stock actualizado: ${product.name} · ${stock} unidades.`);
    } catch (cause) { setNotice(cause instanceof Error ? cause.message : 'No se pudo actualizar el stock.'); }
  };
  const createManualOrderDone = () => { setManualOrderOpen(false); setSection('Pedidos'); void loadData(true); setNotice('Pedido manual creado correctamente.'); };
  const todayLabel = new Intl.DateTimeFormat('es-AR', { weekday: 'long', day: 'numeric', month: 'short' }).format(new Date());
  const invoiceOrders = orders.filter(isInvoiced);
  const dispatchOrders = orders.filter(order => ['EN_CAMINO', 'ENTREGADO'].includes(order.status));

  return <main className="ops-app"><aside className="ops-sidebar"><a className="ops-logo" href="#ops"><span>DM</span><div><b>Dulce Mayor</b><small>DEPÓSITO · ONLINE</small></div></a><button className="ops-new-order" onClick={() => setManualOrderOpen(true)}><Plus /> Nuevo pedido</button><div className="ops-nav-label">OPERACIONES</div><nav>{navItems.map(({ label, icon: Icon }) => <button className={section === label ? 'ops-nav-item active' : 'ops-nav-item'} key={label} onClick={() => setSection(label)}><Icon /><span>{label}</span>{label === 'Stock' && lowStockProducts.length > 0 && <b>{lowStockProducts.length}</b>}{label === 'Pedidos' && pendingOrders.length > 0 && <b>{pendingOrders.length}</b>}{label === 'Despacho' && inTransit.length > 0 && <b>{inTransit.length}</b>}</button>)}</nav><div className="ops-side-bottom"><button onClick={() => setNotice('Atajos: Ctrl + K buscar, P nuevo pedido, S consultar stock.')}><CircleHelp /> Atajos de teclado <kbd>?</kbd></button><button onClick={onBack}><ArrowLeft /> Volver al portal mayorista</button><div className="ops-user"><span>DM</span><div><b>Cuenta operativa</b><small>Personal autorizado</small></div><MoreVertical /></div></div></aside>
    <section className="ops-main"><header className="ops-topbar"><div><b>Depósito Central</b><span><MapPin /> Godoy Cruz</span><ChevronDown /></div><div className="ops-top-actions"><button onClick={() => setNotice(pendingOrders.length ? `Tenés ${pendingOrders.length} pedidos pendientes.` : 'No hay notificaciones nuevas.')} aria-label="Notificaciones"><Bell />{pendingOrders.length > 0 && <i />}</button><span className="ops-user-avatar">DM</span></div></header>
      <div className="ops-content"><div className="ops-breadcrumb">Inicio <ChevronDown /> Centro de mando <ChevronDown /> {section}</div><div className="ops-heading"><div><h1>{section === 'Inicio' ? 'Tareas operativas' : section}</h1><p>{todayLabel} · Resumen actualizado desde Dulce Mayor</p></div><div><button className="ops-secondary" onClick={() => void loadData(true)} disabled={refreshing}>{refreshing ? <LoaderCircle className="spin" /> : <RefreshCw />} Actualizar</button><button className="ops-primary" onClick={() => setManualOrderOpen(true)}><Plus /> Nuevo pedido manual</button></div></div>
        {notice && <div className="ops-notice" role="status"><CheckCircle2 />{notice}<button onClick={() => setNotice('')}><X /></button></div>}{error && <div className="ops-notice ops-notice-error" role="alert"><AlertTriangle />{error}<button onClick={() => void loadData(true)}>Reintentar</button></div>}
        {loading ? <div className="ops-loading"><LoaderCircle className="spin" /> Cargando datos operativos...</div> : <>
          {section === 'Inicio' && <><div className="ops-kpi-title"><b>Resumen operativo & KPIs</b><span>Hoy · {todayOrders.length} pedidos</span></div><div className="kpi-grid"><Kpi icon={<ClipboardList />} color="blue" label="Por procesar" value={String(toProcess.length)} suffix="órdenes" detail={`${todayOrders.length} pedidos hoy · Ver lista`} onClick={() => setSection('Pedidos')} /><Kpi icon={<AlertTriangle />} color="orange" label="Sin stock suficiente" value="0" suffix="órdenes trabadas" detail="Validación automática de inventario" onClick={() => setSection('Stock')} /><Kpi icon={<Warehouse />} color="red" label="Stock crítico" value={String(lowStockProducts.length)} suffix="productos bajo umbral" detail="Ver alertas de inventario" onClick={() => setSection('Stock')} /><Kpi icon={<FileText />} color="green" label="Facturación hoy" value={moneyCompact.format(invoicedTotal)} suffix={`${invoicedToday.length} comprobantes`} detail="Ver facturación" onClick={() => setSection('Facturación y pagos')} /></div>
            <div className="ops-middle-grid"><article className="dispatch-card"><header><div><span className="kpi-icon blue"><Truck /></span><div><h2>Estado del despacho</h2><small>Pedidos en tránsito</small></div></div><span className="route-status"><i /> {inTransit.length} en ruta</span></header><div className="dispatch-progress"><div><b>Entregas de hoy</b><strong>{deliveredToday} <small>/ {deliveredToday + inTransit.length}</small></strong></div><div className="progress-track"><i style={{ width: `${deliveredToday + inTransit.length ? deliveredToday / (deliveredToday + inTransit.length) * 100 : 0}%` }} /></div><div className="dispatch-meta"><span>{inTransit.length} pedidos en tránsito</span><button onClick={() => setSection('Despacho')}>Ver despacho <ArrowRight /></button></div></div><div className="dispatch-footer"><Clock3 /> Datos sincronizados con pedidos <span>· {pendingOrders.length} pendientes</span></div></article><article className="quick-actions"><h2>Acciones rápidas</h2><button onClick={() => setManualOrderOpen(true)}><Plus /> Crear pedido manual</button><button onClick={() => setSection('Stock')}><Barcode /> Consultar y ajustar stock</button></article></div>
            <OrdersTable orders={pendingOrders.slice(0, 8)} onAdvance={advanceOrder} />
            {showAlerts && <section className="stock-alerts"><header><div><span className="kpi-icon red"><AlertTriangle /></span><div><h2>Alertas críticas de stock</h2><small>Productos en o por debajo del mínimo configurado</small></div></div><button onClick={() => setShowAlerts(false)} aria-label="Ocultar alertas"><X /></button></header><div className="alert-grid">{lowStockProducts.slice(0, 3).map(product => <article key={product.id}><div><b>{product.name}</b><span className="alert-pill warning">Crítico</span></div><small>{product.brand} · Mínimo: {product.stockMinimum}</small><p><Warehouse /> Stock: <b>{product.stock} unidades</b></p><button onClick={() => setSection('Stock')}>Ajustar stock <ArrowRight /></button></article>)}{!lowStockProducts.length && <div className="ops-empty">No hay alertas de stock.</div>}</div></section>}</>}
          {section === 'Pedidos' && <OrdersTable orders={orders} onAdvance={advanceOrder} />}
          {section === 'Productos' && <InventoryTable products={inventory} onStockChange={changeStock} />}
          {section === 'Stock' && <InventoryTable products={inventory} onStockChange={changeStock} stockOnly />}
          {section === 'Facturación y pagos' && <section className="orders-table-card ops-billing-table"><header className="table-title"><div><h2>Pedidos facturados</h2><p>Pedidos con estado de facturación confirmado</p></div><b>{invoiceOrders.length} comprobantes</b></header><DataRows orders={invoiceOrders} empty="Todavía no hay pedidos facturados." /></section>}
          {section === 'Despacho' && <section className="orders-table-card ops-dispatch-table"><header className="table-title"><div><h2>Seguimiento de despachos</h2><p>Pedidos en camino y entregados</p></div><b>{dispatchOrders.length} pedidos</b></header><DataRows orders={dispatchOrders} onAdvance={advanceOrder} empty="No hay pedidos en tránsito." /></section>}
          {section === 'Mi cuenta' && <section className="admin-account-card"><span className="ops-user-avatar">DM</span><h2>Cuenta operativa</h2><p>Acceso interno de Dulce Mayor</p><div><b>Ubicación</b><span>Depósito Central · Godoy Cruz</span></div><div><b>Panel</b><span>Administración y operaciones</span></div></section>}
          {section === 'Sucursales' && <BranchesAdmin />}
        </>}
        <div className="ops-footer-note"><PackageCheck /> Datos conectados a la API de Dulce Mayor <span>·</span> Actualizá para sincronizar</div>
      </div>
    </section>{manualOrderOpen && <ManualOrderModal products={inventory} onClose={() => setManualOrderOpen(false)} onCreated={createManualOrderDone} />}</main>;
}

function Kpi({ icon, color, label, value, suffix, detail, onClick }: { icon: ReactNode; color: string; label: string; value: string; suffix: string; detail: string; onClick: () => void }) {
  return <article className="kpi-card"><span className={`kpi-icon ${color}`}>{icon}</span><small>{label}</small><strong>{value} <em>{suffix}</em></strong><button onClick={onClick}>{detail} <ArrowRight /></button></article>;
}

function DataRows({ orders, onAdvance, empty }: { orders: CustomerOrder[]; onAdvance?: (order: CustomerOrder) => void; empty: string }) {
  return <div className="ops-table-wrap"><table className="ops-table"><thead><tr><th>Pedido</th><th>Cliente</th><th>Fecha</th><th>Total</th><th>Estado</th><th>Acción</th></tr></thead><tbody>{orders.map(order => <tr key={order.id}><td><b>{orderId(order.id)}</b><small>{order.items.length} artículos</small></td><td>{order.clientName}</td><td>{orderDate(order.invoicedAt || order.createdAt)}</td><td><b>{currency.format(order.total)}</b></td><td>{stateLabels[order.status] || order.status}</td><td>{onAdvance && nextState[order.status] && <button className="row-action" onClick={() => onAdvance(order)}>Avanzar <ArrowRight /></button>}</td></tr>)}</tbody></table>{orders.length === 0 && <div className="ops-empty">{empty}</div>}</div>;
}
