import { Fragment, jsx, jsxs } from "react/jsx-runtime";
import { useCallback, useEffect, useMemo, useState } from "react";
import { ArrowLeft, BadgeCheck, FileText, LoaderCircle, Package, QrCode, RefreshCw, Search, Truck } from "lucide-react";
import { api } from "../api";
const money = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", minimumFractionDigits: 2 });
const dateTime = (value) => new Intl.DateTimeFormat("es-AR", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
const statusText = {
  CARGADO: "Cargado",
  PENDIENTE_STOCK: "Pendiente de stock",
  FACTURADO: "Facturado",
  EN_REPARTO: "En reparto",
  ENTREGADO: "Entregado",
  ANULADO: "Anulado"
};
const steps = ["CARGADO", "FACTURADO", "EN_REPARTO", "ENTREGADO"];
const isInvoiced = (order) => Boolean(order.invoicedAt) || ["FACTURADO", "EN_REPARTO", "ENTREGADO"].includes(order.status);
function OrderCard({ order }) {
  const currentStep = steps.indexOf(order.status);
  const canceled = order.status === "ANULADO";
  const orderNumber = `DM-${order.id.slice(0, 8).toUpperCase()}`;
  return /* @__PURE__ */ jsxs("article", { className: "order-card", children: [
    /* @__PURE__ */ jsxs("header", { children: [
      /* @__PURE__ */ jsxs("div", { children: [
        /* @__PURE__ */ jsx("span", { children: "Pedido" }),
        /* @__PURE__ */ jsxs("b", { children: [
          "#",
          orderNumber
        ] }),
        /* @__PURE__ */ jsx("small", { children: dateTime(order.createdAt) })
      ] }),
      /* @__PURE__ */ jsxs("span", { className: `order-status ${canceled ? "cancelled" : ""}`, children: [
        /* @__PURE__ */ jsx("i", {}),
        " ",
        statusText[order.status] || order.status
      ] })
    ] }),
    /* @__PURE__ */ jsx("div", { className: "tracking-title", children: /* @__PURE__ */ jsxs("div", { children: [
      /* @__PURE__ */ jsx("span", { children: /* @__PURE__ */ jsx(Truck, {}) }),
      /* @__PURE__ */ jsxs("div", { children: [
        /* @__PURE__ */ jsx("b", { children: "Seguimiento del pedido" }),
        /* @__PURE__ */ jsx("small", { children: canceled ? "Este pedido fue cancelado." : `Estado actual: ${statusText[order.status] || order.status}` })
      ] })
    ] }) }),
    !canceled && /* @__PURE__ */ jsx("div", { className: "tracking-steps", children: steps.map((step, index) => /* @__PURE__ */ jsxs("div", { className: index < currentStep ? "tracking-step done" : index === currentStep ? "tracking-step active" : "tracking-step", children: [
      /* @__PURE__ */ jsx("span", { className: "step-icon", children: index < currentStep ? /* @__PURE__ */ jsx(BadgeCheck, {}) : index === currentStep ? /* @__PURE__ */ jsx(RefreshCw, {}) : index === 3 ? /* @__PURE__ */ jsx(Truck, {}) : /* @__PURE__ */ jsx(Package, {}) }),
      /* @__PURE__ */ jsx("b", { children: statusText[step] }),
      /* @__PURE__ */ jsx("small", { children: step === "FACTURADO" && order.invoicedAt ? dateTime(order.invoicedAt) : index === 0 ? dateTime(order.createdAt) : index === currentStep ? "Actual" : "Pendiente" })
    ] }, step)) }),
    /* @__PURE__ */ jsxs("div", { className: "order-lower", children: [
      /* @__PURE__ */ jsxs("section", { className: "items-section", children: [
        /* @__PURE__ */ jsxs("div", { className: "subsection-heading", children: [
          /* @__PURE__ */ jsx("b", { children: "Art\xEDculos del pedido" }),
          /* @__PURE__ */ jsxs("small", { children: [
            order.items.length,
            " ",
            order.items.length === 1 ? "\xEDtem" : "\xEDtems"
          ] })
        ] }),
        order.items.map((item) => /* @__PURE__ */ jsxs("div", { className: "order-product", children: [
          /* @__PURE__ */ jsx("span", { className: "order-product-icon", children: /* @__PURE__ */ jsx(Package, { size: 18 }) }),
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("b", { children: item.productName }),
            /* @__PURE__ */ jsxs("small", { children: [
              money.format(item.unitPrice),
              " por unidad"
            ] })
          ] }),
          /* @__PURE__ */ jsxs("strong", { children: [
            item.quantity,
            " u."
          ] })
        ] }, item.productId))
      ] }),
      /* @__PURE__ */ jsxs("section", { className: "totals-section", children: [
        /* @__PURE__ */ jsx("div", { className: "subsection-heading", children: /* @__PURE__ */ jsx("b", { children: "Resumen" }) }),
        /* @__PURE__ */ jsxs("div", { children: [
          /* @__PURE__ */ jsx("span", { children: "Medio de pago" }),
          /* @__PURE__ */ jsx("b", { children: order.paymentMethod.replaceAll("_", " ") })
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "grand-total", children: [
          /* @__PURE__ */ jsx("span", { children: "Total del pedido" }),
          /* @__PURE__ */ jsx("b", { children: money.format(order.total) })
        ] }),
        order.status === "FACTURADO" && /* @__PURE__ */ jsxs("p", { className: "invoice-issued", children: [
          /* @__PURE__ */ jsx(BadgeCheck, { size: 15 }),
          " Facturado",
          order.invoicedAt ? ` \xB7 ${dateTime(order.invoicedAt)}` : ""
        ] })
      ] })
    ] })
  ] });
}
function InvoiceCard({ order }) {
  return /* @__PURE__ */ jsxs("article", { className: "invoice-record", children: [
    /* @__PURE__ */ jsx("div", { className: "invoice-record-icon", children: /* @__PURE__ */ jsx(FileText, {}) }),
    /* @__PURE__ */ jsxs("div", { className: "invoice-record-main", children: [
      /* @__PURE__ */ jsxs("span", { children: [
        "Pedido #",
        order.id.slice(0, 8).toUpperCase()
      ] }),
      /* @__PURE__ */ jsx("b", { children: "Comprobante de facturaci\xF3n" }),
      /* @__PURE__ */ jsx("small", { children: order.invoicedAt ? dateTime(order.invoicedAt) : dateTime(order.createdAt) })
    ] }),
    /* @__PURE__ */ jsxs("div", { className: "invoice-record-total", children: [
      /* @__PURE__ */ jsx("small", { children: "Total" }),
      /* @__PURE__ */ jsx("b", { children: money.format(order.total) })
    ] }),
    /* @__PURE__ */ jsxs("span", { className: "invoice-record-state", children: [
      /* @__PURE__ */ jsx(BadgeCheck, { size: 14 }),
      " Facturado"
    ] }),
    /* @__PURE__ */ jsxs("details", { className: "invoice-record-details", children: [
      /* @__PURE__ */ jsx("summary", { children: "Ver detalle" }),
      /* @__PURE__ */ jsx("div", { children: order.items.map((item) => /* @__PURE__ */ jsxs("p", { children: [
        /* @__PURE__ */ jsxs("span", { children: [
          item.quantity,
          " \xD7 ",
          item.productName
        ] }),
        /* @__PURE__ */ jsx("b", { children: money.format(item.total) })
      ] }, item.productId)) })
    ] })
  ] });
}
export function OrdersPage({ onBack, onProfile }) {
  const [tab, setTab] = useState("pedidos");
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [query, setQuery] = useState("");
  const [copied, setCopied] = useState(false);
  const loadOrders = useCallback(() => {
    setLoading(true);
    setError("");
    api.orders().then(setOrders).catch((cause) => setError(cause instanceof Error ? cause.message : "No pudimos cargar tus pedidos.")).finally(() => setLoading(false));
  }, []);
  useEffect(loadOrders, [loadOrders]);
  const filteredOrders = useMemo(() => orders.filter((order) => `${order.id} ${order.status} ${order.createdAt}`.toLowerCase().includes(query.toLowerCase())), [orders, query]);
  const invoices = orders.filter(isInvoiced);
  return /* @__PURE__ */ jsxs("main", { className: "orders-page", children: [
    /* @__PURE__ */ jsxs("aside", { className: "account-sidebar", children: [
      /* @__PURE__ */ jsxs("div", { className: "merchant-card", children: [
        /* @__PURE__ */ jsx("div", { className: "merchant-avatar", children: "DM" }),
        /* @__PURE__ */ jsxs("div", { children: [
          /* @__PURE__ */ jsx("b", { children: "Cuenta mayorista" }),
          /* @__PURE__ */ jsx("small", { children: "Portal de clientes" })
        ] })
      ] }),
      /* @__PURE__ */ jsxs("button", { className: tab === "pedidos" ? "account-tab current" : "account-tab", onClick: () => setTab("pedidos"), children: [
        /* @__PURE__ */ jsx(Package, {}),
        " Pedidos ",
        /* @__PURE__ */ jsx("span", { children: orders.length })
      ] }),
      /* @__PURE__ */ jsxs("button", { className: tab === "facturas" ? "account-tab current" : "account-tab", onClick: () => setTab("facturas"), children: [
        /* @__PURE__ */ jsx(FileText, {}),
        " Facturas ",
        /* @__PURE__ */ jsx("span", { children: invoices.length })
      ] }),
      /* @__PURE__ */ jsxs("button", { className: "account-tab", onClick: onProfile, children: [
        /* @__PURE__ */ jsx(BadgeCheck, {}),
        " Mis datos"
      ] })
    ] }),
    /* @__PURE__ */ jsxs("section", { className: "orders-content", children: [
      /* @__PURE__ */ jsxs("button", { className: "back-link", onClick: onBack, children: [
        /* @__PURE__ */ jsx(ArrowLeft, { size: 16 }),
        " Volver al cat\xE1logo"
      ] }),
      /* @__PURE__ */ jsx("div", { className: "orders-title", children: /* @__PURE__ */ jsxs("div", { children: [
        /* @__PURE__ */ jsx("span", { className: "section-kicker", children: "TU CUENTA MAYORISTA" }),
        /* @__PURE__ */ jsx("h1", { children: tab === "pedidos" ? "Mis pedidos" : "Mis facturas" })
      ] }) }),
      tab === "pedidos" && /* @__PURE__ */ jsx("div", { className: "orders-toolbar", children: /* @__PURE__ */ jsxs("label", { children: [
        /* @__PURE__ */ jsx(Search, {}),
        /* @__PURE__ */ jsx("input", { value: query, onChange: (event) => setQuery(event.target.value), placeholder: "Buscar por pedido o estado..." })
      ] }) }),
      loading ? /* @__PURE__ */ jsxs("div", { className: "account-loading", children: [
        /* @__PURE__ */ jsx(LoaderCircle, { className: "spin" }),
        " Cargando ",
        tab === "pedidos" ? "pedidos" : "facturas",
        "..."
      ] }) : error ? /* @__PURE__ */ jsxs("div", { className: "account-state error", children: [
        /* @__PURE__ */ jsx("h2", { children: "No pudimos cargar esta secci\xF3n" }),
        /* @__PURE__ */ jsx("p", { children: error }),
        /* @__PURE__ */ jsx("button", { onClick: loadOrders, children: "Volver a intentar" })
      ] }) : tab === "pedidos" ? filteredOrders.length ? /* @__PURE__ */ jsx("div", { className: "account-record-list", children: filteredOrders.map((order) => /* @__PURE__ */ jsx(OrderCard, { order }, order.id)) }) : /* @__PURE__ */ jsxs("div", { className: "account-state", children: [
        /* @__PURE__ */ jsx("span", { children: /* @__PURE__ */ jsx(Package, {}) }),
        /* @__PURE__ */ jsx("h2", { children: query ? "No encontramos pedidos" : "Todav\xEDa no ten\xE9s pedidos" }),
        /* @__PURE__ */ jsx("p", { children: query ? "Prob\xE1 con otro n\xFAmero o estado." : "Cuando confirmes un pedido desde el cat\xE1logo, aparecer\xE1 ac\xE1 con su seguimiento." }),
        /* @__PURE__ */ jsx("button", { onClick: onBack, children: "Explorar cat\xE1logo" })
      ] }) : invoices.length ? /* @__PURE__ */ jsxs(Fragment, { children: [
        /* @__PURE__ */ jsx("p", { className: "invoice-intro", children: "Facturaci\xF3n asociada a tus pedidos confirmados." }),
        /* @__PURE__ */ jsx("div", { className: "invoice-record-list", children: invoices.map((order) => /* @__PURE__ */ jsx(InvoiceCard, { order }, order.id)) })
      ] }) : /* @__PURE__ */ jsxs("div", { className: "account-state", children: [
        /* @__PURE__ */ jsx("span", { children: /* @__PURE__ */ jsx(FileText, {}) }),
        /* @__PURE__ */ jsx("h2", { children: "Todav\xEDa no hay facturas" }),
        /* @__PURE__ */ jsx("p", { children: "Las facturas aparecer\xE1n aqu\xED cuando un pedido cambie al estado \u201CFacturado\u201D." }),
        /* @__PURE__ */ jsx("button", { onClick: () => setTab("pedidos"), children: "Ver mis pedidos" })
      ] }),
      tab === "pedidos" && !loading && !error && /* @__PURE__ */ jsxs("section", { className: "payment-card", children: [
        /* @__PURE__ */ jsxs("div", { className: "payment-heading", children: [
          /* @__PURE__ */ jsx("span", { children: /* @__PURE__ */ jsx(BadgeCheck, {}) }),
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("b", { children: "Medios de pago mayoristas" }),
            /* @__PURE__ */ jsx("small", { children: "Us\xE1 estos datos para realizar una transferencia" })
          ] })
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "payment-content", children: [
          /* @__PURE__ */ jsxs("div", { className: "bank-data", children: [
            /* @__PURE__ */ jsx("b", { children: "Transferencia bancaria" }),
            /* @__PURE__ */ jsx("small", { children: "Banco de la Naci\xF3n Argentina" }),
            /* @__PURE__ */ jsx("span", { children: "Titular" }),
            /* @__PURE__ */ jsx("strong", { children: "Distribuidora Dulce Mayor S.A." }),
            /* @__PURE__ */ jsx("span", { children: "Alias" }),
            /* @__PURE__ */ jsxs("div", { className: "copy-row", children: [
              /* @__PURE__ */ jsx("b", { children: "DULCE.MAYOR.PAGOS" }),
              /* @__PURE__ */ jsxs("button", { onClick: () => {
                void navigator.clipboard?.writeText("DULCE.MAYOR.PAGOS");
                setCopied(true);
              }, children: [
                /* @__PURE__ */ jsx(BadgeCheck, {}),
                " ",
                copied ? "Copiado" : "Copiar alias"
              ] })
            ] }),
            /* @__PURE__ */ jsx("span", { children: "CBU" }),
            /* @__PURE__ */ jsx("b", { children: "0110599520000012345678" })
          ] }),
          /* @__PURE__ */ jsxs("div", { className: "payment-qr-placeholder", children: [
            /* @__PURE__ */ jsx("span", { children: /* @__PURE__ */ jsx(QrCode, {}) }),
            /* @__PURE__ */ jsx("b", { children: "Pago con c\xF3digo QR" }),
            /* @__PURE__ */ jsx("p", { children: "Pr\xF3ximamente vas a poder escanear aqu\xED el c\xF3digo para pagar." }),
            /* @__PURE__ */ jsx("small", { children: "Espacio reservado para el QR" })
          ] }),
          /* @__PURE__ */ jsxs("p", { children: [
            /* @__PURE__ */ jsx(BadgeCheck, {}),
            " Inform\xE1 el n\xFAmero de pedido al realizar el pago."
          ] })
        ] })
      ] })
    ] })
  ] });
}
