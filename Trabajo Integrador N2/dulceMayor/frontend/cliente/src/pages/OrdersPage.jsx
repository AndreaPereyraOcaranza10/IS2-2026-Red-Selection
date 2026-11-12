import { useCallback, useEffect, useMemo, useState } from "react";
import {
  ArrowLeft,
  Ban,
  BadgeCheck,
  Clock3,
  Download,
  FileText,
  LoaderCircle,
  MapPin,
  Package,
  QrCode,
  RefreshCw,
  Search,
  Truck,
} from "lucide-react";
import { api } from "../api";

const money = new Intl.NumberFormat("es-AR", {
  style: "currency",
  currency: "ARS",
  minimumFractionDigits: 2,
});

const statusText = {
  CARGADO: "Recibido",
  PENDIENTE_STOCK: "Pendiente de stock",
  PENDIENTE_PAGO: "Esperando pago",
  FACTURADO: "Facturado",
  EN_REPARTO: "En reparto",
  ENTREGADO: "Entregado",
  ANULADO: "Anulado",
};

const trackingSteps = ["CARGADO", "PENDIENTE_PAGO", "FACTURADO", "EN_REPARTO", "ENTREGADO"];

function dateTime(value) {
  if (!value) return "Sin fecha";
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? String(value)
    : new Intl.DateTimeFormat("es-AR", { dateStyle: "medium", timeStyle: "short" }).format(date);
}

function orderNumber(id = "") {
  return "DM-" + String(id).slice(0, 8).toUpperCase();
}

function stateLabel(state) {
  return statusText[state] || String(state || "Actualización").replaceAll("_", " ");
}

/** Pago con billetera virtual: genera el QR (lo dibuja el backend con ZXing), lo muestra y verifica el cobro contra la pasarela. */
export function PaymentPanel({ order, onPaid }) {
  const [payment, setPayment] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const pendingWallet = payment?.tipoPago === "BILLETERA_VIRTUAL" && payment?.estado === "PENDIENTE" ? payment : null;
  const agreedPayment = payment && !pendingWallet && payment.estado === "PENDIENTE" ? payment : null;

  useEffect(() => {
    let active = true;
    api.orderPayment(order.id)
      .then((current) => { if (active) setPayment(current || null); })
      .catch(() => {});
    return () => { active = false; };
  }, [order.id]);

  const verify = useCallback(async (silent) => {
    try {
      const current = await api.verifyOrderPayment(order.id);
      setPayment(current || null);   // sin pago vigente (QR vencido o cancelado): vuelve a ofrecer "Pagar"
      if (current?.estado === "CONFIRMADO") onPaid();
      else if (!silent) setError("Todavía no vemos el pago. Si ya pagaste, esperá unos segundos y probá de nuevo.");
    } catch (cause) {
      if (!silent) setError(cause instanceof Error ? cause.message : "No pudimos verificar el pago.");
    }
  }, [order.id, onPaid]);

  useEffect(() => {
    if (!pendingWallet) return undefined;
    const timer = window.setInterval(() => verify(true), 5000);
    return () => window.clearInterval(timer);
  }, [pendingWallet?.id, verify]);

  const run = async (action) => {
    if (busy) return;
    setBusy(true);
    setError("");
    try {
      await action();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No pudimos completar la operación.");
    } finally {
      setBusy(false);
    }
  };

  const start = () => run(async () => setPayment(await api.startOrderPayment(order.id)));
  const simulate = () => run(async () => {
    const current = await api.simulateOrderPayment(order.id);
    if (current) setPayment(current);
    if (current?.estado === "CONFIRMADO") onPaid();
  });

  return (
    <div className="order-payment">
      {pendingWallet ? (
        <>
          <div className="order-payment-qr">
            {pendingWallet.imagenQr && (
              <img src={pendingWallet.imagenQr} alt="Código QR para pagar con Mercado Pago" width="220" height="220" />
            )}
            <div>
              <b>{money.format(pendingWallet.monto || order.total || 0)}</b>
              <p>{pendingWallet.instrucciones}</p>
              <small>Cuando pagues, el pedido se factura solo. Estamos verificando el pago…</small>
              <div className="order-payment-actions">
                <button type="button" onClick={() => verify(false)} disabled={busy}>
                  <RefreshCw size={14} /> Ya pagué, verificar
                </button>
                {pendingWallet.pasarelaSimulada && (
                  <button type="button" className="order-payment-demo" onClick={simulate} disabled={busy}>
                    Simular pago (modo demo)
                  </button>
                )}
              </div>
            </div>
          </div>
        </>
      ) : agreedPayment ? (
        <p className="order-payment-agreed" role="status">{agreedPayment.instrucciones}</p>
      ) : (
        <button type="button" className="order-payment-start" onClick={start} disabled={busy}>
          {busy ? <LoaderCircle className="spin" size={14} /> : <QrCode size={14} />}
          {busy ? "Generando QR…" : "Pagar con Mercado Pago"}
        </button>
      )}
      {error && <p className="order-payment-error" role="alert">{error}</p>}
    </div>
  );
}

function OrderCard({ order, onCancel, cancelling, onPaid }) {
  const canceled = order.status === "ANULADO";
  const delivered = order.status === "ENTREGADO";
  const events = [...(order.trace?.events || [])].sort(
    (left, right) => new Date(left.at || 0) - new Date(right.at || 0),
  );
  const activeIndex = order.status === "PENDIENTE_STOCK"
    ? -1
    : trackingSteps.indexOf(order.status);

  return (
    <article className="order-card">
      <header>
        <div>
          <span>Pedido</span>
          <b>#{orderNumber(order.id)}</b>
          <small>{dateTime(order.createdAt)}</small>
        </div>
        <span className={"order-status " + (canceled ? "cancelled" : delivered ? "delivered" : "")}>
          <i /> {stateLabel(order.status)}
        </span>
      </header>

      <div className="tracking-title">
        <div>
          <span><Truck /></span>
          <div>
            <b>Seguimiento del pedido</b>
            <small>
              {canceled
                ? "Este pedido fue cancelado."
                : order.status === "PENDIENTE_STOCK"
                  ? "Estamos esperando disponibilidad de stock para reservarlo."
                  : order.status === "PENDIENTE_PAGO"
                    ? "Tu pedido tiene el stock reservado y espera el pago."
                    : "Estado actual: " + stateLabel(order.status)}
            </small>
          </div>
        </div>
      </div>

      {!canceled && (
        <div className="tracking-steps">
          {trackingSteps.map((step, index) => {
            // "Facturado" ya es un paso completo para el cliente (el pago está acreditado): sale verde aunque sea el actual.
            const done = activeIndex >= 0 && (index < activeIndex || (index === activeIndex && step === "FACTURADO"));
            const active = index === activeIndex && step !== "FACTURADO";
            const event = events.find((item) => item.state === step);
            return (
              <div className={"tracking-step " + (done ? "done " : "") + (active ? "active " : "") + (active && step === "ENTREGADO" ? "delivered" : "")} key={step}>
                <span className="step-icon">
                  {done || (active && step === "ENTREGADO") ? <BadgeCheck /> : active ? <RefreshCw /> : step === "EN_REPARTO" ? <Truck /> : <Package />}
                </span>
                <b>{stateLabel(step)}</b>
                <small>{event ? dateTime(event.at) : active ? "Actual" : done ? "Completado" : "Pendiente"}</small>
              </div>
            );
          })}
        </div>
      )}

      {order.status === "PENDIENTE_STOCK" && (
        <p className="order-stock-notice" role="status">
          El pedido fue recibido y se reservará cuando haya stock suficiente.
        </p>
      )}

      {order.status === "PENDIENTE_PAGO" && (
        <p className="order-stock-notice" role="status">
          Reservamos tu mercadería. Podés pagar ahora con billetera virtual (Mercado Pago) o esperar que administración
          te contacte para acordar efectivo o transferencia. Si no se paga en 48 horas, el pedido se anula solo.
        </p>
      )}

      {order.status === "PENDIENTE_PAGO" && <PaymentPanel order={order} onPaid={onPaid} />}

      {onCancel && ["PENDIENTE_STOCK", "PENDIENTE_PAGO"].includes(order.status) && (
        <div className="order-cancel">
          <button type="button" className="order-cancel-button" onClick={() => onCancel(order)} disabled={cancelling}>
            {cancelling ? <LoaderCircle className="spin" size={14} /> : <Ban size={14} />}
            {cancelling ? "Cancelando…" : "Cancelar pedido"}
          </button>
        </div>
      )}

      {events.length > 0 && (
        <details className="order-event-details">
          <summary><Clock3 size={15} /> Ver historial de estados</summary>
          <ol>
            {events.map((event, index) => (
              <li key={String(event.state) + "-" + String(event.at) + "-" + index}>
                <span className="order-event-dot" />
                <div>
                  <b>{stateLabel(event.state)}</b>
                  <small>{dateTime(event.at)}</small>
                  {event.note && <p>{event.note}</p>}
                </div>
              </li>
            ))}
          </ol>
        </details>
      )}

      {order.trace?.carrier && (
        <p className="order-carrier">
          <Truck size={15} /> Transportista: {order.trace.carrier.name}
          {order.trace.carrier.vehicle ? " · " + order.trace.carrier.vehicle : ""}
        </p>
      )}

      {order.pointOfSale?.address && (
        <p className="order-point-of-sale"><MapPin size={15} /> Entrega en {order.pointOfSale.address}</p>
      )}

      <div className="order-lower">
        <section className="items-section">
          <div className="subsection-heading">
            <b>Artículos del pedido</b>
            <small>{order.items?.length || 0} {(order.items?.length || 0) === 1 ? "ítem" : "ítems"}</small>
          </div>
          {(order.items || []).map((item, index) => (
            <div className="order-product" key={String(item.productId) + "-" + index}>
              <span className="order-product-icon"><Package size={18} /></span>
              <div>
                <b>{item.productName}</b>
                <small>{money.format(item.unitPrice)} por unidad</small>
              </div>
              <strong>{item.quantity} u.</strong>
            </div>
          ))}
        </section>

        <section className="totals-section">
          <div className="subsection-heading"><b>Resumen</b></div>
          <div>
            <span>Medio de pago</span>
            <b>{String(order.paymentMethod || "A_COORDINAR").replaceAll("_", " ")}</b>
          </div>
          <div className="grand-total">
            <span>Total del pedido</span>
            <b>{money.format(order.total || 0)}</b>
          </div>
          {order.invoiceId && !canceled && (
            <p className="invoice-issued">
              <BadgeCheck size={15} /> Facturado
              {order.invoicedAt ? " · " + dateTime(order.invoicedAt) : ""}
            </p>
          )}
        </section>
      </div>
    </article>
  );
}

function InvoiceCard({ order, onDownload, downloading }) {
  const canceled = order.status === "ANULADO";
  return (
    <article className="invoice-record">
      <div className="invoice-record-icon"><FileText /></div>
      <div className="invoice-record-main">
        <span>Pedido #{orderNumber(order.id)}</span>
        <b>{canceled ? "Factura anulada" : "Factura del pedido"}</b>
        <small>{dateTime(order.invoicedAt || order.createdAt)}</small>
      </div>
      <div className="invoice-record-total">
        <small>Total</small>
        <b>{money.format(order.total || 0)}</b>
      </div>
      <span className={"invoice-record-state " + (canceled ? "cancelled" : "")}>
        <BadgeCheck size={14} /> {canceled ? "Anulada" : "Facturada"}
      </span>
      {order.invoiceId && !canceled && (
        <button
          className="invoice-download-button"
          type="button"
          onClick={() => onDownload(order)}
          disabled={downloading}
        >
          {downloading ? <LoaderCircle className="spin" /> : <Download />}
          {downloading ? "Preparando PDF…" : "Descargar factura PDF"}
        </button>
      )}
      <details className="invoice-record-details">
        <summary>Ver detalle</summary>
        <div>
          {(order.items || []).map((item, index) => (
            <p key={String(item.productId) + "-" + index}>
              <span>{item.quantity} × {item.productName}</span>
              <b>{money.format(item.total)}</b>
            </p>
          ))}
        </div>
      </details>
    </article>
  );
}

export function OrdersPage({ onBack, onProfile, account }) {
  const [tab, setTab] = useState("pedidos");
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [invoiceError, setInvoiceError] = useState("");
  const [query, setQuery] = useState("");
  const [downloadingInvoiceId, setDownloadingInvoiceId] = useState("");
  const [cancellingId, setCancellingId] = useState("");

  const loadOrders = useCallback(() => {
    setLoading(true);
    setError("");
    api.orders()
      .then((result) => setOrders(Array.isArray(result) ? result : []))
      .catch((cause) => setError(cause instanceof Error ? cause.message : "No pudimos cargar tus pedidos."))
      .finally(() => setLoading(false));
  }, []);

  useEffect(loadOrders, [loadOrders]);

  const filteredOrders = useMemo(
    () => orders.filter((order) =>
      (String(order.id) + " " + String(order.status) + " " + String(order.createdAt))
        .toLowerCase().includes(query.toLowerCase()),
    ),
    [orders, query],
  );
  const invoices = orders.filter((order) => Boolean(order.invoiceId));

  const cancelOrder = async (order) => {
    if (cancellingId) return;
    if (!window.confirm("¿Querés cancelar este pedido? Se libera la mercadería que reservamos para vos.")) return;
    setError("");
    setCancellingId(order.id);
    try {
      await api.cancelOrder(order.id);
      loadOrders();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No pudimos cancelar el pedido.");
    } finally {
      setCancellingId("");
    }
  };

  const downloadInvoice = async (order) => {
    if (!order.invoiceId || downloadingInvoiceId) return;
    setInvoiceError("");
    setDownloadingInvoiceId(order.invoiceId);
    try {
      const { blob, filename } = await api.invoicePdf(order.invoiceId);
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filename || ("factura-" + order.invoiceId + ".pdf");
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (cause) {
      setInvoiceError(cause instanceof Error ? cause.message : "No pudimos descargar la factura.");
    } finally {
      setDownloadingInvoiceId("");
    }
  };

  return (
    <main className="orders-page">
      <aside className="account-sidebar">
        <div className="merchant-card">
          <div className="merchant-avatar">DM</div>
          <div>
            <b>{account?.displayName || account?.email || "Cuenta mayorista"}</b>
            <small>Portal de clientes</small>
          </div>
        </div>
        <button className={tab === "pedidos" ? "account-tab current" : "account-tab"} onClick={() => setTab("pedidos")}>
          <Package /> Pedidos <span>{orders.length}</span>
        </button>
        <button className={tab === "facturas" ? "account-tab current" : "account-tab"} onClick={() => setTab("facturas")}>
          <FileText /> Facturas <span>{invoices.length}</span>
        </button>
        <button className="account-tab" onClick={onProfile}>
          <BadgeCheck /> Mis datos
        </button>
      </aside>

      <section className="orders-content">
        <button className="back-link" onClick={onBack}>
          <ArrowLeft size={16} /> Volver al catálogo
        </button>
        <div className="orders-title">
          <div>
            <span className="section-kicker">TU CUENTA MAYORISTA</span>
            <h1>{tab === "pedidos" ? "Mis pedidos" : "Mis facturas"}</h1>
          </div>
          <button className="orders-refresh-button" type="button" onClick={loadOrders} disabled={loading}>
            <RefreshCw size={15} /> Actualizar
          </button>
        </div>

        {tab === "pedidos" && (
          <div className="orders-toolbar">
            <label>
              <Search />
              <input
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Buscar por pedido o estado..."
              />
            </label>
          </div>
        )}

        {invoiceError && <p className="invoice-download-error" role="alert">{invoiceError}</p>}

        {loading ? (
          <div className="account-loading"><LoaderCircle className="spin" /> Cargando {tab}…</div>
        ) : error ? (
          <div className="account-state error" role="alert">
            <h2>No pudimos cargar esta sección</h2>
            <p>{error}</p>
            <button onClick={loadOrders}>Volver a intentar</button>
          </div>
        ) : tab === "pedidos" ? (
          filteredOrders.length ? (
            <div className="account-record-list">
              {filteredOrders.map((order) => <OrderCard order={order} key={order.id} onCancel={cancelOrder} cancelling={cancellingId === order.id} onPaid={loadOrders} />)}
            </div>
          ) : (
            <div className="account-state">
              <span><Package /></span>
              <h2>{query ? "No encontramos pedidos" : "Todavía no tenés pedidos"}</h2>
              <p>
                {query
                  ? "Probá con otro número o estado."
                  : "Cuando confirmes un pedido desde el catálogo, aparecerá acá con su seguimiento."}
              </p>
              <button onClick={onBack}>Explorar catálogo</button>
            </div>
          )
        ) : invoices.length ? (
          <>
            <p className="invoice-intro">Facturas asociadas a tus pedidos confirmados.</p>
            <div className="invoice-record-list">
              {invoices.map((order) => (
                <InvoiceCard
                  order={order}
                  key={order.id}
                  onDownload={downloadInvoice}
                  downloading={downloadingInvoiceId === order.invoiceId}
                />
              ))}
            </div>
          </>
        ) : (
          <div className="account-state">
            <span><FileText /></span>
            <h2>Todavía no hay facturas</h2>
            <p>Las facturas aparecerán acá cuando se emita una para uno de tus pedidos.</p>
            <button onClick={() => setTab("pedidos")}>Ver mis pedidos</button>
          </div>
        )}

        {tab === "pedidos" && !loading && !error && (
          <section className="payment-card">
            <div className="payment-heading">
              <span><BadgeCheck /></span>
              <div>
                <b>Medios de pago mayoristas</b>
                <small>La información de pago se habilitará cuando esté integrada a un pedido.</small>
              </div>
            </div>
            <div className="payment-content">
              <p className="payment-unavailable"><BadgeCheck /> Todavía no hay medios de pago configurados para mostrar.</p>
            </div>
          </section>
        )}
      </section>
    </main>
  );
}