import { useCallback, useEffect, useMemo, useState } from 'react';
import { ArrowLeft, BadgeCheck, FileText, LoaderCircle, Package, QrCode, RefreshCw, Search, Truck } from 'lucide-react';
import { api, type CustomerOrder } from '../lib/api';

type OrdersPageProps = { onBack: () => void; onProfile: () => void };
type AccountTab = 'pedidos' | 'facturas';

const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 2 });
const dateTime = (value: string) => new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));
const statusText: Record<string, string> = {
  RECIBIDO: 'Recibido', EN_PREPARACION: 'En preparación', LISTO_FACTURAR: 'Listo para facturar',
  FACTURADO: 'Facturado', EN_CAMINO: 'En camino', ENTREGADO: 'Entregado', CANCELADO: 'Cancelado',
};
const steps = ['RECIBIDO', 'EN_PREPARACION', 'LISTO_FACTURAR', 'FACTURADO', 'EN_CAMINO', 'ENTREGADO'];
const isInvoiced = (order: CustomerOrder) => Boolean(order.invoicedAt) || ['FACTURADO', 'EN_CAMINO', 'ENTREGADO'].includes(order.status);

function OrderCard({ order }: { order: CustomerOrder }) {
  const currentStep = steps.indexOf(order.status);
  const canceled = order.status === 'CANCELADO';
  const orderNumber = `DM-${order.id.slice(0, 8).toUpperCase()}`;
  return <article className="order-card">
    <header><div><span>Pedido</span><b>#{orderNumber}</b><small>{dateTime(order.createdAt)}</small></div><span className={`order-status ${canceled ? 'cancelled' : ''}`}><i /> {statusText[order.status] || order.status}</span></header>
    <div className="tracking-title"><div><span><Truck /></span><div><b>Seguimiento del pedido</b><small>{canceled ? 'Este pedido fue cancelado.' : `Estado actual: ${statusText[order.status] || order.status}`}</small></div></div></div>
    {!canceled && <div className="tracking-steps">{steps.map((step, index) => <div className={index < currentStep ? 'tracking-step done' : index === currentStep ? 'tracking-step active' : 'tracking-step'} key={step}><span className="step-icon">{index < currentStep ? <BadgeCheck /> : index === currentStep ? <RefreshCw /> : index === 3 ? <Truck /> : <Package />}</span><b>{statusText[step]}</b><small>{step === 'FACTURADO' && order.invoicedAt ? dateTime(order.invoicedAt) : index === 0 ? dateTime(order.createdAt) : index === currentStep ? 'Actual' : 'Pendiente'}</small></div>)}</div>}
    <div className="order-lower"><section className="items-section"><div className="subsection-heading"><b>Artículos del pedido</b><small>{order.items.length} {order.items.length === 1 ? 'ítem' : 'ítems'}</small></div>{order.items.map(item => <div className="order-product" key={item.productId}><span className="order-product-icon"><Package size={18} /></span><div><b>{item.productName}</b><small>{money.format(item.unitPrice)} por unidad</small></div><strong>{item.quantity} u.</strong></div>)}</section><section className="totals-section"><div className="subsection-heading"><b>Resumen</b></div><div><span>Medio de pago</span><b>{order.paymentMethod.replaceAll('_', ' ')}</b></div><div className="grand-total"><span>Total del pedido</span><b>{money.format(order.total)}</b></div>{order.status === 'FACTURADO' && <p className="invoice-issued"><BadgeCheck size={15} /> Facturado{order.invoicedAt ? ` · ${dateTime(order.invoicedAt)}` : ''}</p>}</section></div>
  </article>;
}

function InvoiceCard({ order }: { order: CustomerOrder }) {
  return <article className="invoice-record"><div className="invoice-record-icon"><FileText /></div><div className="invoice-record-main"><span>Pedido #{order.id.slice(0, 8).toUpperCase()}</span><b>Comprobante de facturación</b><small>{order.invoicedAt ? dateTime(order.invoicedAt) : dateTime(order.createdAt)}</small></div><div className="invoice-record-total"><small>Total</small><b>{money.format(order.total)}</b></div><span className="invoice-record-state"><BadgeCheck size={14} /> Facturado</span><details className="invoice-record-details"><summary>Ver detalle</summary><div>{order.items.map(item => <p key={item.productId}><span>{item.quantity} × {item.productName}</span><b>{money.format(item.total)}</b></p>)}</div></details></article>;
}

export function OrdersPage({ onBack, onProfile }: OrdersPageProps) {
  const [tab, setTab] = useState<AccountTab>('pedidos');
  const [orders, setOrders] = useState<CustomerOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [query, setQuery] = useState('');
  const [copied, setCopied] = useState(false);

  const loadOrders = useCallback(() => {
    setLoading(true);
    setError('');
    api.orders().then(setOrders)
      .catch(cause => setError(cause instanceof Error ? cause.message : 'No pudimos cargar tus pedidos.'))
      .finally(() => setLoading(false));
  }, []);
  useEffect(loadOrders, [loadOrders]);

  const filteredOrders = useMemo(() => orders.filter(order => `${order.id} ${order.status} ${order.createdAt}`.toLowerCase().includes(query.toLowerCase())), [orders, query]);
  const invoices = orders.filter(isInvoiced);

  return <main className="orders-page"><aside className="account-sidebar"><div className="merchant-card"><div className="merchant-avatar">DM</div><div><b>Cuenta mayorista</b><small>Portal de clientes</small></div></div><button className={tab === 'pedidos' ? 'account-tab current' : 'account-tab'} onClick={() => setTab('pedidos')}><Package /> Pedidos <span>{orders.length}</span></button><button className={tab === 'facturas' ? 'account-tab current' : 'account-tab'} onClick={() => setTab('facturas')}><FileText /> Facturas <span>{invoices.length}</span></button><button className="account-tab" onClick={onProfile}><BadgeCheck /> Mis datos</button></aside>
    <section className="orders-content"><button className="back-link" onClick={onBack}><ArrowLeft size={16} /> Volver al catálogo</button><div className="orders-title"><div><span className="section-kicker">TU CUENTA MAYORISTA</span><h1>{tab === 'pedidos' ? 'Mis pedidos' : 'Mis facturas'}</h1></div></div>
      {tab === 'pedidos' && <div className="orders-toolbar"><label><Search /><input value={query} onChange={event => setQuery(event.target.value)} placeholder="Buscar por pedido o estado..." /></label></div>}
      {loading ? <div className="account-loading"><LoaderCircle className="spin" /> Cargando {tab === 'pedidos' ? 'pedidos' : 'facturas'}...</div>
        : error ? <div className="account-state error"><h2>No pudimos cargar esta sección</h2><p>{error}</p><button onClick={loadOrders}>Volver a intentar</button></div>
          : tab === 'pedidos' ? filteredOrders.length ? <div className="account-record-list">{filteredOrders.map(order => <OrderCard key={order.id} order={order} />)}</div> : <div className="account-state"><span><Package /></span><h2>{query ? 'No encontramos pedidos' : 'Todavía no tenés pedidos'}</h2><p>{query ? 'Probá con otro número o estado.' : 'Cuando confirmes un pedido desde el catálogo, aparecerá acá con su seguimiento.'}</p><button onClick={onBack}>Explorar catálogo</button></div>
            : invoices.length ? <><p className="invoice-intro">Facturación asociada a tus pedidos confirmados.</p><div className="invoice-record-list">{invoices.map(order => <InvoiceCard key={order.id} order={order} />)}</div></> : <div className="account-state"><span><FileText /></span><h2>Todavía no hay facturas</h2><p>Las facturas aparecerán aquí cuando un pedido cambie al estado “Facturado”.</p><button onClick={() => setTab('pedidos')}>Ver mis pedidos</button></div>}
      {tab === 'pedidos' && !loading && !error && <section className="payment-card"><div className="payment-heading"><span><BadgeCheck /></span><div><b>Medios de pago mayoristas</b><small>Usá estos datos para realizar una transferencia</small></div></div><div className="payment-content"><div className="bank-data"><b>Transferencia bancaria</b><small>Banco de la Nación Argentina</small><span>Titular</span><strong>Distribuidora Dulce Mayor S.A.</strong><span>Alias</span><div className="copy-row"><b>DULCE.MAYOR.PAGOS</b><button onClick={() => { void navigator.clipboard?.writeText('DULCE.MAYOR.PAGOS'); setCopied(true); }}><BadgeCheck /> {copied ? 'Copiado' : 'Copiar alias'}</button></div><span>CBU</span><b>0110599520000012345678</b></div><div className="payment-qr-placeholder"><span><QrCode /></span><b>Pago con código QR</b><p>Próximamente vas a poder escanear aquí el código para pagar.</p><small>Espacio reservado para el QR</small></div><p><BadgeCheck /> Informá el número de pedido al realizar el pago.</p></div></section>}
    </section></main>;
}
