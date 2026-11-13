import { Fragment, jsx, jsxs } from "react/jsx-runtime";
import { lazy, Suspense, useCallback, useEffect, useMemo, useState } from "react";
import { ProductsAdminPage, useAuth } from '@dulcemayor/shared';
import { Link, useLocation, useNavigate } from "react-router-dom";
import { ADMIN_ROUTES, institutionalSiteUrl } from "../config";
import { AlertTriangle, ArrowLeft, ArrowRight, BarChart3, Bell, Barcode, Building2, CheckCircle2, ChevronDown, ClipboardList, Clock3, ExternalLink, FileText, LayoutDashboard, LoaderCircle, MapPin, Package, PackageCheck, Plus, RefreshCw, Search, Settings, ShoppingCart, Store, Tag, Truck, Users, Warehouse, X, FileBarChart } from "lucide-react";
import { apiAdmin as api } from "../api";
import { BranchesAdmin } from "./BranchesAdmin";
import { ClientsAdminPage } from "./ClientsAdminPage.jsx";
import { UsersAdminPage } from "./UsersAdminPage.jsx";
import { PromotionsAdminPage } from "./PromotionsAdminPage.jsx";
import { ReportsAdminPage } from "./ReportsAdminPage.jsx";
import { OrdersAdminPage } from "./OrdersAdminPage.jsx";
import { BillingPaymentsAdminPage } from "./BillingPaymentsAdminPage.jsx";
import { DispatchDialog, PendingDispatchCard } from "./DispatchPanel.jsx";

const StatisticsAdminPage = lazy(() => import("./StatisticsAdminPage.jsx").then((module) => ({ default: module.StatisticsAdminPage })));
const navItems = [
  { label: "Inicio", icon: LayoutDashboard },
  { label: "Productos", icon: Package },
  { label: "Stock", icon: Warehouse },
  { label: "Pedidos", icon: ShoppingCart },
  { label: "Facturaci\xF3n y pagos", icon: FileText },
  { label: "Despacho", icon: Truck },
  { label: "Reportes", icon: FileBarChart },
  { label: "Sucursales", icon: Building2 },
  { label: "Clientes", icon: Store },
  { label: "Usuarios internos", icon: Users },
  { label: "Promociones", icon: Tag },
  { label: "Estadísticas", icon: BarChart3 }
];
const menuByRole = {
  ADMINISTRATIVO: navItems.filter(({ label }) => !["Usuarios internos", "Promociones", "Estadísticas"].includes(label)).map(({ label }) => label),
  JEFE_ADMINISTRATIVO: navItems.map(({ label }) => label),
};
const sectionPaths = {
  "Inicio": ADMIN_ROUTES.home,
  "Productos": ADMIN_ROUTES.products,
  "Stock": ADMIN_ROUTES.stock,
  "Pedidos": ADMIN_ROUTES.orders,
  [navItems[4].label]: ADMIN_ROUTES.billing,
  "Despacho": ADMIN_ROUTES.dispatch,
  "Reportes": ADMIN_ROUTES.reports,
  "Mi cuenta": ADMIN_ROUTES.account,
  "Sucursales": ADMIN_ROUTES.branches,
  "Clientes": ADMIN_ROUTES.clients,
  "Usuarios internos": ADMIN_ROUTES.users,
  "Promociones": ADMIN_ROUTES.promotions,
  "Estadísticas": ADMIN_ROUTES.statistics
};
const stateLabels = { CARGADO: "Cargado", PENDIENTE_STOCK: "Pendiente de stock", PENDIENTE_PAGO: "Esperando pago", FACTURADO: "Facturado", EN_REPARTO: "En reparto", ENTREGADO: "Entregado", ANULADO: "Anulado" };
const nextState = { PENDIENTE_STOCK: "PENDIENTE_PAGO", FACTURADO: "EN_REPARTO", EN_REPARTO: "ENTREGADO" };
// Se despacha un pedido ya pago (FACTURADO) o uno que espera el pago pero se acordó en efectivo: se factura al despachar.
const canDispatch = (order) => order.status === "FACTURADO" || (order.status === "PENDIENTE_PAGO" && order.paymentMethod === "EFECTIVO");
const currency = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 });
const moneyCompact = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", notation: "compact", maximumFractionDigits: 2 });
const orderId = (id) => `#DM-${String(id).slice(0, 5).toUpperCase()}`;
const orderDate = (date) => new Date(date).toLocaleString("es-AR", { dateStyle: "short", timeStyle: "short" });
const isInvoiced = (order) => Boolean(order.invoicedAt) || ["FACTURADO", "EN_REPARTO", "ENTREGADO"].includes(order.status);
function ManualOrderModal({ products, onClose, onCreated }) {
  const [client, setClient] = useState("");
  const [lines, setLines] = useState([{ productId: "", quantity: 1 }]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const submit = async (event) => {
    event.preventDefault();
    const items = lines.filter((line) => line.productId && line.quantity > 0);
    if (!items.length) {
      setError("Agreg\xE1 al menos un producto al pedido.");
      return;
    }
    setSaving(true);
    setError("");
    try {
      await api.createManualOrder(client.trim(), items);
      onCreated();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo crear el pedido.");
    } finally {
      setSaving(false);
    }
  };
  return /* @__PURE__ */ jsx("div", { className: "manual-modal-backdrop", onMouseDown: (event) => {
    if (event.target === event.currentTarget) onClose();
  }, children: /* @__PURE__ */ jsxs("section", { className: "manual-modal", role: "dialog", "aria-modal": "true", "aria-labelledby": "manual-order-title", children: [
    /* @__PURE__ */ jsxs("header", { children: [
      /* @__PURE__ */ jsxs("div", { children: [
        /* @__PURE__ */ jsx("span", { className: "section-kicker", children: "OPERACIONES" }),
        /* @__PURE__ */ jsx("h2", { id: "manual-order-title", children: "Nuevo pedido manual" })
      ] }),
      /* @__PURE__ */ jsx("button", { onClick: onClose, "aria-label": "Cerrar", children: /* @__PURE__ */ jsx(X, {}) })
    ] }),
    /* @__PURE__ */ jsxs("form", { onSubmit: submit, children: [
      /* @__PURE__ */ jsxs("label", { children: [
        "Cliente / comercio",
        /* @__PURE__ */ jsx("input", { value: client, onChange: (event) => setClient(event.target.value), placeholder: "Nombre del comercio", required: true })
      ] }),
      /* @__PURE__ */ jsxs("div", { className: "manual-lines-heading", children: [
        /* @__PURE__ */ jsx("b", { children: "Productos" }),
        /* @__PURE__ */ jsxs("button", { type: "button", onClick: () => setLines((previous) => [...previous, { productId: "", quantity: 1 }]), children: [
          /* @__PURE__ */ jsx(Plus, {}),
          " Agregar producto"
        ] })
      ] }),
      lines.map((line, index) => /* @__PURE__ */ jsxs("div", { className: "manual-line", children: [
        /* @__PURE__ */ jsxs("select", { "aria-label": "Producto", value: line.productId, onChange: (event) => setLines((previous) => previous.map((item, i) => i === index ? { ...item, productId: event.target.value } : item)), required: true, children: [
          /* @__PURE__ */ jsx("option", { value: "", children: "Seleccion\xE1 un producto" }),
          products.map((product) => /* @__PURE__ */ jsxs("option", { value: String(product.id), children: [
            product.name,
            " \xB7 ",
            currency.format(product.price)
          ] }, product.id))
        ] }),
        /* @__PURE__ */ jsx("input", { "aria-label": "Cantidad", type: "number", min: "1", value: line.quantity, onChange: (event) => setLines((previous) => previous.map((item, i) => i === index ? { ...item, quantity: Number(event.target.value) } : item)), required: true }),
        /* @__PURE__ */ jsx("button", { type: "button", "aria-label": "Quitar producto", disabled: lines.length === 1, onClick: () => setLines((previous) => previous.filter((_, i) => i !== index)), children: /* @__PURE__ */ jsx(X, {}) })
      ] }, index)),
      error && /* @__PURE__ */ jsx("p", { className: "manual-error", role: "alert", children: error }),
      /* @__PURE__ */ jsxs("footer", { children: [
        /* @__PURE__ */ jsx("button", { type: "button", className: "ops-secondary", onClick: onClose, children: "Cancelar" }),
        /* @__PURE__ */ jsxs("button", { className: "ops-primary", disabled: saving, children: [
          saving ? /* @__PURE__ */ jsx(LoaderCircle, { className: "spin" }) : /* @__PURE__ */ jsx(Plus, {}),
          saving ? "Creando..." : "Crear pedido"
        ] })
      ] })
    ] })
  ] }) });
}
function OrdersTable({ orders, onAdvance }) {
  const [query, setQuery] = useState("");
  const visible = useMemo(() => orders.filter((order) => `${order.id} ${order.clientName} ${stateLabels[order.status] || order.status}`.toLowerCase().includes(query.toLowerCase())), [orders, query]);
  return /* @__PURE__ */ jsxs("section", { className: "orders-table-card ops-orders-table", children: [
    /* @__PURE__ */ jsxs("header", { className: "table-title", children: [
      /* @__PURE__ */ jsxs("div", { children: [
        /* @__PURE__ */ jsx("h2", { children: "Pedidos pendientes de procesar" }),
        /* @__PURE__ */ jsx("p", { children: "Cola de preparaci\xF3n, facturaci\xF3n y despacho" })
      ] }),
      /* @__PURE__ */ jsxs("b", { children: [
        visible.length,
        " pedidos"
      ] })
    ] }),
    /* @__PURE__ */ jsx("div", { className: "table-toolbar", children: /* @__PURE__ */ jsxs("label", { children: [
      /* @__PURE__ */ jsx(Search, {}),
      /* @__PURE__ */ jsx("input", { value: query, onChange: (event) => setQuery(event.target.value), placeholder: "Filtrar pedidos o clientes..." })
    ] }) }),
    /* @__PURE__ */ jsxs("div", { className: "ops-table-wrap", children: [
      /* @__PURE__ */ jsxs("table", { className: "ops-table", children: [
        /* @__PURE__ */ jsx("thead", { children: /* @__PURE__ */ jsxs("tr", { children: [
          /* @__PURE__ */ jsx("th", { children: "N\xB0 pedido \xB7 Fecha" }),
          /* @__PURE__ */ jsx("th", { children: "Cliente" }),
          /* @__PURE__ */ jsx("th", { children: "Unidades / Total" }),
          /* @__PURE__ */ jsx("th", { children: "Estado de stock" }),
          /* @__PURE__ */ jsx("th", { children: "Estado" }),
          /* @__PURE__ */ jsx("th", { children: "Acci\xF3n" })
        ] }) }),
        /* @__PURE__ */ jsx("tbody", { children: visible.map((order) => /* @__PURE__ */ jsxs("tr", { children: [
          /* @__PURE__ */ jsxs("td", { children: [
            /* @__PURE__ */ jsx("b", { children: orderId(order.id) }),
            /* @__PURE__ */ jsx("small", { children: orderDate(order.createdAt) })
          ] }),
          /* @__PURE__ */ jsxs("td", { children: [
            /* @__PURE__ */ jsx("b", { children: order.clientName }),
            /* @__PURE__ */ jsxs("small", { children: [
              /* @__PURE__ */ jsx(MapPin, {}),
              " Cuenta asociada"
            ] })
          ] }),
          /* @__PURE__ */ jsxs("td", { children: [
            /* @__PURE__ */ jsx("b", { children: currency.format(order.total) }),
            /* @__PURE__ */ jsxs("small", { children: [
              order.items.reduce((sum, item) => sum + item.quantity, 0),
              " unidades"
            ] })
          ] }),
          /* @__PURE__ */ jsx("td", { children: /* @__PURE__ */ jsxs("span", { className: "table-stock", children: [
            /* @__PURE__ */ jsx(CheckCircle2, {}),
            " Stock reservado"
          ] }) }),
          /* @__PURE__ */ jsx("td", { children: /* @__PURE__ */ jsx("span", { className: `table-state ${order.status === "CARGADO" ? "state-invoice" : order.status === "ANULADO" ? "state-stock" : "state-loaded"}`, children: stateLabels[order.status] || order.status }) }),
          /* @__PURE__ */ jsx("td", { children: nextState[order.status] || canDispatch(order) ? /* @__PURE__ */ jsxs("button", { className: "row-action", onClick: () => onAdvance(order), children: [
            order.status === "PENDIENTE_STOCK" ? "Reintentar reserva" : canDispatch(order) ? "Despachar" : "Procesar",
            " ",
            /* @__PURE__ */ jsx(ArrowRight, {})
          ] }) : /* @__PURE__ */ jsx("span", { className: "ops-muted", children: "\u2014" }) })
        ] }, order.id)) })
      ] }),
      visible.length === 0 && /* @__PURE__ */ jsx("div", { className: "ops-empty", children: "No hay pedidos que coincidan." })
    ] })
  ] });
}
function InventoryTable({ products, onStockChange, stockOnly = false }) {
  const [query, setQuery] = useState("");
  const visible = products.filter((product) => (!stockOnly || Number(product.stock) <= (product.stockMinimum || 0)) && `${product.name} ${product.brand} ${product.category}`.toLowerCase().includes(query.toLowerCase()));
  return /* @__PURE__ */ jsxs("section", { className: "orders-table-card ops-inventory-table", children: [
    /* @__PURE__ */ jsxs("header", { className: "table-title", children: [
      /* @__PURE__ */ jsxs("div", { children: [
        /* @__PURE__ */ jsx("h2", { children: stockOnly ? "Alertas de stock" : "Cat\xE1logo de productos" }),
        /* @__PURE__ */ jsx("p", { children: stockOnly ? "Productos que alcanzaron su nivel m\xEDnimo de inventario" : "Productos registrados y disponibilidad actual" })
      ] }),
      /* @__PURE__ */ jsxs("b", { children: [
        visible.length,
        " productos"
      ] })
    ] }),
    /* @__PURE__ */ jsx("div", { className: "table-toolbar", children: /* @__PURE__ */ jsxs("label", { children: [
      /* @__PURE__ */ jsx(Search, {}),
      /* @__PURE__ */ jsx("input", { value: query, onChange: (event) => setQuery(event.target.value), placeholder: "Buscar productos..." })
    ] }) }),
    /* @__PURE__ */ jsxs("div", { className: "ops-table-wrap", children: [
      /* @__PURE__ */ jsxs("table", { className: "ops-table", children: [
        /* @__PURE__ */ jsx("thead", { children: /* @__PURE__ */ jsxs("tr", { children: [
          /* @__PURE__ */ jsx("th", { children: "Producto" }),
          /* @__PURE__ */ jsx("th", { children: "Marca / categor\xEDa" }),
          /* @__PURE__ */ jsx("th", { children: "Precio" }),
          /* @__PURE__ */ jsx("th", { children: "Stock actual" }),
          /* @__PURE__ */ jsx("th", { children: "Stock m\xEDnimo" }),
          /* @__PURE__ */ jsx("th", { children: "Acci\xF3n" })
        ] }) }),
        /* @__PURE__ */ jsx("tbody", { children: visible.map((product) => /* @__PURE__ */ jsxs("tr", { children: [
          /* @__PURE__ */ jsxs("td", { children: [
            /* @__PURE__ */ jsx("b", { children: product.name }),
            /* @__PURE__ */ jsx("small", { children: product.pack })
          ] }),
          /* @__PURE__ */ jsxs("td", { children: [
            product.brand,
            /* @__PURE__ */ jsx("small", { children: product.category })
          ] }),
          /* @__PURE__ */ jsx("td", { children: /* @__PURE__ */ jsx("b", { children: currency.format(product.price) }) }),
          /* @__PURE__ */ jsx("td", { children: /* @__PURE__ */ jsxs("span", { className: Number(product.stock) <= (product.stockMinimum || 0) ? "table-state state-stock" : "table-state state-loaded", children: [
            product.stock,
            " u."
          ] }) }),
          /* @__PURE__ */ jsxs("td", { children: [
            product.stockMinimum,
            " u."
          ] }),
          /* @__PURE__ */ jsx("td", { children: /* @__PURE__ */ jsxs("div", { className: "stock-stepper", children: [
            /* @__PURE__ */ jsx("button", { "aria-label": `Reducir stock de ${product.name}`, disabled: Number(product.stock) <= 0, onClick: () => onStockChange(product, Number(product.stock) - 1), children: "\u2212" }),
            /* @__PURE__ */ jsx("button", { "aria-label": `Aumentar stock de ${product.name}`, onClick: () => onStockChange(product, Number(product.stock) + 1), children: "+" })
          ] }) })
        ] }, product.id)) })
      ] }),
      visible.length === 0 && /* @__PURE__ */ jsx("div", { className: "ops-empty", children: "No hay productos que coincidan." })
    ] })
  ] });
}
export function OperationsDashboardPage({ onBack }) {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const currentPath = location.pathname.replace(/\/+$/, "") || "/admin";
  const section = Object.entries(sectionPaths).find(([, path]) => path === currentPath)?.[0] || "Inicio";
  const setSection = (next) => navigate(sectionPaths[next]);
  const visibleNavItems = navItems.filter(({ label }) => menuByRole[user?.role]?.includes(label));
  useEffect(() => {
    if (!Object.values(sectionPaths).includes(currentPath)) navigate("/acceso-denegado", { replace: true });
  }, [currentPath, navigate]);
  useEffect(() => {
    const rutasSoloJefe = [ADMIN_ROUTES.users, ADMIN_ROUTES.promotions, ADMIN_ROUTES.statistics];
    if (rutasSoloJefe.includes(currentPath) && user?.role !== "JEFE_ADMINISTRATIVO") {
      navigate("/acceso-denegado", { replace: true });
    }
  }, [currentPath, navigate, user?.role]);
  const [orders, setOrders] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");
  const [showAlerts, setShowAlerts] = useState(true);
  const [manualOrderOpen, setManualOrderOpen] = useState(false);
  const [dispatchTarget, setDispatchTarget] = useState(null);
  const loadData = useCallback(async (quiet = false) => {
    if (quiet) setRefreshing(true);
    else setLoading(true);
    setError("");
    const [ordersResult, inventoryResult] = await Promise.allSettled([api.orders(), api.inventory()]);
    if (ordersResult.status === "fulfilled") setOrders(ordersResult.value);
    else setError(ordersResult.reason instanceof Error ? ordersResult.reason.message : "No se pudieron cargar los pedidos.");
    if (inventoryResult.status === "fulfilled") setInventory(inventoryResult.value);
    else setError(inventoryResult.reason instanceof Error ? inventoryResult.reason.message : "No se pudo cargar el inventario.");
    setLoading(false);
    setRefreshing(false);
  }, []);
  useEffect(() => {
    void loadData();
  }, [loadData]);
  const todayOrders = orders.filter((order) => new Date(order.createdAt).toDateString() === (/* @__PURE__ */ new Date()).toDateString());
  const pendingOrders = orders.filter((order) => !["ENTREGADO", "ANULADO"].includes(order.status));
  const toProcess = orders.filter((order) => ["CARGADO", "PENDIENTE_STOCK", "PENDIENTE_PAGO"].includes(order.status));
  const invoicedToday = orders.filter((order) => isInvoiced(order) && new Date(order.invoicedAt || order.createdAt).toDateString() === (/* @__PURE__ */ new Date()).toDateString());
  const invoicedTotal = invoicedToday.reduce((sum, order) => sum + order.total, 0);
  const lowStockProducts = inventory.filter((product) => Number(product.stock) <= (product.stockMinimum || 0));
  const inTransit = orders.filter((order) => order.status === "EN_REPARTO");
  const deliveredToday = orders.filter((order) => order.status === "ENTREGADO" && new Date(order.createdAt).toDateString() === (/* @__PURE__ */ new Date()).toDateString()).length;
  const advanceOrder = async (order, requestedStatus) => {
    if (canDispatch(order) && (!requestedStatus || requestedStatus === "EN_REPARTO")) {
      setDispatchTarget([order]);
      return;
    }
    const status = requestedStatus || nextState[order.status];
    if (!status) return;
    try {
      const updated = await api.updateOrderStatus(order.id, status);
      setOrders((previous) => previous.map((item) => item.id === order.id ? updated : item));
      setNotice(`${orderId(order.id)} actualizado: ${stateLabels[status]}.`);
    } catch (cause) {
      setNotice(cause instanceof Error ? cause.message : "No se pudo actualizar el pedido.");
    }
  };
  const changeStock = async (product, stock) => {
    const updated = await api.updateStock(product.id, stock);
    setInventory((previous) => previous.map((item) => item.id === product.id ? updated : item));
    setNotice(`Stock actualizado: ${product.name} \xB7 ${stock} unidades.`);
  };
  const dispatchDone = (result) => {
    setDispatchTarget(null);
    void loadData(true);
    setNotice(`${result.pedidoIds.length === 1 ? "1 pedido asignado" : `${result.pedidoIds.length} pedidos asignados`} a ${result.transportista}.`);
  };
  const createManualOrderDone = () => {
    setManualOrderOpen(false);
    setSection("Pedidos");
    void loadData(true);
    setNotice("Pedido manual creado correctamente.");
  };
  const todayLabel = new Intl.DateTimeFormat("es-AR", { weekday: "long", day: "numeric", month: "short" }).format(/* @__PURE__ */ new Date());
  const invoiceOrders = orders.filter(isInvoiced);
  const pendingDispatch = orders.filter(canDispatch);
  const dispatchOrders = orders.filter((order) => ["EN_REPARTO", "ENTREGADO"].includes(order.status));
  return /* @__PURE__ */ jsxs("main", { className: "ops-app", children: [
    /* @__PURE__ */ jsxs("aside", { className: "ops-sidebar", children: [
      /* @__PURE__ */ jsxs(Link, { className: "ops-logo", to: "/admin/inicio", children: [
        /* @__PURE__ */ jsx("span", { children: "DM" }),
        /* @__PURE__ */ jsxs("div", { children: [
          /* @__PURE__ */ jsx("b", { children: "Dulce Mayor" }),
          /* @__PURE__ */ jsx("small", { children: "DEP\xD3SITO \xB7 ONLINE" })
        ] })
      ] }),
      /* @__PURE__ */ jsxs("a", { className: "ops-new-order ops-site-link", href: institutionalSiteUrl(), "aria-label": "Ir al sitio institucional", title: "Ir al sitio institucional", children: [
        /* @__PURE__ */ jsx(ExternalLink, {}),
        " Sitio institucional"
      ] }),
      /* @__PURE__ */ jsx("div", { className: "ops-nav-label", children: "OPERACIONES" }),
      /* @__PURE__ */ jsx("nav", { children: visibleNavItems.map(({ label, icon: Icon }) => /* @__PURE__ */ jsxs("button", { className: section === label ? "ops-nav-item active" : "ops-nav-item", onClick: () => setSection(label), children: [
        /* @__PURE__ */ jsx(Icon, {}),
        /* @__PURE__ */ jsx("span", { children: label }),
        label === "Stock" && lowStockProducts.length > 0 && /* @__PURE__ */ jsx("b", { children: lowStockProducts.length }),
        label === "Pedidos" && pendingOrders.length > 0 && /* @__PURE__ */ jsx("b", { children: pendingOrders.length }),
        label === "Despacho" && pendingDispatch.length > 0 && /* @__PURE__ */ jsx("b", { children: pendingDispatch.length })
      ] }, label)) }),
      /* @__PURE__ */ jsxs("div", { className: "ops-side-bottom", children: [
        /* @__PURE__ */ jsxs("button", { onClick: () => { logout(); onBack?.(); }, children: [
          /* @__PURE__ */ jsx(ArrowLeft, {}),
          " Cerrar sesi\u00f3n"
        ] }),
        /* @__PURE__ */ jsxs("button", { type: "button", className: section === "Mi cuenta" ? "ops-user active" : "ops-user", onClick: () => setSection("Mi cuenta"), "aria-label": "Abrir Mi perfil", title: "Mi perfil", children: [
          /* @__PURE__ */ jsx("span", { children: "DM" }),
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("b", { children: user?.fullName || user?.username || "Cuenta operativa" }),
            /* @__PURE__ */ jsx("small", { children: user?.role || "Personal autorizado" })
          ] }),
          /* @__PURE__ */ jsx(Settings, {})
        ] })
      ] })
    ] }),
    /* @__PURE__ */ jsxs("section", { className: "ops-main", children: [
      /* @__PURE__ */ jsxs("header", { className: "ops-topbar", children: [
        /* @__PURE__ */ jsxs("div", { children: [
          /* @__PURE__ */ jsx("b", { children: "Dep\xF3sito Central" }),
          /* @__PURE__ */ jsxs("span", { children: [
            /* @__PURE__ */ jsx(MapPin, {}),
            " Godoy Cruz"
          ] }),
          /* @__PURE__ */ jsx(ChevronDown, {})
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "ops-top-actions", children: [
          /* @__PURE__ */ jsxs("button", { onClick: () => setNotice(pendingOrders.length ? `Ten\xE9s ${pendingOrders.length} pedidos pendientes.` : "No hay notificaciones nuevas."), "aria-label": "Notificaciones", children: [
            /* @__PURE__ */ jsx(Bell, {}),
            pendingOrders.length > 0 && /* @__PURE__ */ jsx("i", {})
          ] }),
          /* @__PURE__ */ jsx("button", { type: "button", className: "ops-user-avatar", onClick: () => setSection("Mi cuenta"), "aria-label": "Abrir Mi cuenta", title: "Mi cuenta", children: "DM" })
        ] })
      ] }),
      /* @__PURE__ */ jsxs("div", { className: "ops-content", children: [
        /* @__PURE__ */ jsxs("div", { className: "ops-breadcrumb", children: [
          "Inicio ",
          /* @__PURE__ */ jsx(ChevronDown, {}),
          " Centro de mando ",
          /* @__PURE__ */ jsx(ChevronDown, {}),
          " ",
          section
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "ops-heading", children: [
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("h1", { children: section === "Inicio" ? "Tareas operativas" : section }),
            /* @__PURE__ */ jsxs("p", { children: [
              todayLabel,
              " \xB7 Resumen actualizado desde Dulce Mayor"
            ] })
          ] }),
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsxs("button", { className: "ops-secondary", onClick: () => void loadData(true), disabled: refreshing, children: [
              refreshing ? /* @__PURE__ */ jsx(LoaderCircle, { className: "spin" }) : /* @__PURE__ */ jsx(RefreshCw, {}),
              " Actualizar"
            ] }),
            /* @__PURE__ */ jsxs("button", { className: "ops-primary", onClick: () => setManualOrderOpen(true), children: [
              /* @__PURE__ */ jsx(Plus, {}),
              " Nuevo pedido manual"
            ] })
          ] })
        ] }),
        notice && /* @__PURE__ */ jsxs("div", { className: "ops-notice", role: "status", children: [
          /* @__PURE__ */ jsx(CheckCircle2, {}),
          notice,
          /* @__PURE__ */ jsx("button", { onClick: () => setNotice(""), children: /* @__PURE__ */ jsx(X, {}) })
        ] }),
        error && /* @__PURE__ */ jsxs("div", { className: "ops-notice ops-notice-error", role: "alert", children: [
          /* @__PURE__ */ jsx(AlertTriangle, {}),
          error,
          /* @__PURE__ */ jsx("button", { onClick: () => void loadData(true), children: "Reintentar" })
        ] }),
        loading ? /* @__PURE__ */ jsxs("div", { className: "ops-loading", children: [
          /* @__PURE__ */ jsx(LoaderCircle, { className: "spin" }),
          " Cargando datos operativos..."
        ] }) : /* @__PURE__ */ jsxs(Fragment, { children: [
          section === "Inicio" && /* @__PURE__ */ jsxs(Fragment, { children: [
            /* @__PURE__ */ jsxs("div", { className: "ops-kpi-title", children: [
              /* @__PURE__ */ jsx("b", { children: "Resumen operativo & KPIs" }),
              /* @__PURE__ */ jsxs("span", { children: [
                "Hoy \xB7 ",
                todayOrders.length,
                " pedidos"
              ] })
            ] }),
            /* @__PURE__ */ jsxs("div", { className: "kpi-grid", children: [
              /* @__PURE__ */ jsx(Kpi, { icon: /* @__PURE__ */ jsx(ClipboardList, {}), color: "blue", label: "Por procesar", value: String(toProcess.length), suffix: "\xF3rdenes", detail: `${todayOrders.length} pedidos hoy \xB7 Ver lista`, onClick: () => setSection("Pedidos") }),
              /* @__PURE__ */ jsx(Kpi, { icon: /* @__PURE__ */ jsx(AlertTriangle, {}), color: "orange", label: "Sin stock suficiente", value: "0", suffix: "\xF3rdenes trabadas", detail: "Validaci\xF3n autom\xE1tica de inventario", onClick: () => setSection("Stock") }),
              /* @__PURE__ */ jsx(Kpi, { icon: /* @__PURE__ */ jsx(Warehouse, {}), color: "red", label: "Stock cr\xEDtico", value: String(lowStockProducts.length), suffix: "productos bajo umbral", detail: "Ver alertas de inventario", onClick: () => setSection("Stock") }),
              /* @__PURE__ */ jsx(Kpi, { icon: /* @__PURE__ */ jsx(FileText, {}), color: "green", label: "Facturaci\xF3n hoy", value: moneyCompact.format(invoicedTotal), suffix: `${invoicedToday.length} comprobantes`, detail: "Ver facturaci\xF3n", onClick: () => setSection("Facturaci\xF3n y pagos") })
            ] }),
            /* @__PURE__ */ jsxs("div", { className: "ops-middle-grid", children: [
              /* @__PURE__ */ jsxs("article", { className: "dispatch-card", children: [
                /* @__PURE__ */ jsxs("header", { children: [
                  /* @__PURE__ */ jsxs("div", { children: [
                    /* @__PURE__ */ jsx("span", { className: "kpi-icon blue", children: /* @__PURE__ */ jsx(Truck, {}) }),
                    /* @__PURE__ */ jsxs("div", { children: [
                      /* @__PURE__ */ jsx("h2", { children: "Estado del despacho" }),
                      /* @__PURE__ */ jsx("small", { children: "Pedidos en tr\xE1nsito" })
                    ] })
                  ] }),
                  /* @__PURE__ */ jsxs("span", { className: "route-status", children: [
                    /* @__PURE__ */ jsx("i", {}),
                    " ",
                    inTransit.length,
                    " en ruta"
                  ] })
                ] }),
                /* @__PURE__ */ jsxs("div", { className: "dispatch-progress", children: [
                  /* @__PURE__ */ jsxs("div", { children: [
                    /* @__PURE__ */ jsx("b", { children: "Entregas de hoy" }),
                    /* @__PURE__ */ jsxs("strong", { children: [
                      deliveredToday,
                      " ",
                      /* @__PURE__ */ jsxs("small", { children: [
                        "/ ",
                        deliveredToday + inTransit.length
                      ] })
                    ] })
                  ] }),
                  /* @__PURE__ */ jsx("div", { className: "progress-track", children: /* @__PURE__ */ jsx("i", { style: { width: `${deliveredToday + inTransit.length ? deliveredToday / (deliveredToday + inTransit.length) * 100 : 0}%` } }) }),
                  /* @__PURE__ */ jsxs("div", { className: "dispatch-meta", children: [
                    /* @__PURE__ */ jsxs("span", { children: [
                      inTransit.length,
                      " pedidos en tr\xE1nsito"
                    ] }),
                    /* @__PURE__ */ jsxs("button", { onClick: () => setSection("Despacho"), children: [
                      "Ver despacho ",
                      /* @__PURE__ */ jsx(ArrowRight, {})
                    ] })
                  ] })
                ] }),
                /* @__PURE__ */ jsxs("div", { className: "dispatch-footer", children: [
                  /* @__PURE__ */ jsx(Clock3, {}),
                  " Datos sincronizados con pedidos ",
                  /* @__PURE__ */ jsxs("span", { children: [
                    "\xB7 ",
                    pendingOrders.length,
                    " pendientes"
                  ] })
                ] })
              ] }),
              /* @__PURE__ */ jsxs("article", { className: "quick-actions", children: [
                /* @__PURE__ */ jsx("h2", { children: "Acciones r\xE1pidas" }),
                /* @__PURE__ */ jsxs("button", { onClick: () => setManualOrderOpen(true), children: [
                  /* @__PURE__ */ jsx(Plus, {}),
                  " Crear pedido manual"
                ] }),
                /* @__PURE__ */ jsxs("button", { onClick: () => setSection("Stock"), children: [
                  /* @__PURE__ */ jsx(Barcode, {}),
                  " Consultar y ajustar stock"
                ] })
              ] })
            ] }),
            /* @__PURE__ */ jsx(OrdersTable, { orders: pendingOrders.slice(0, 8), onAdvance: advanceOrder }),
            showAlerts && /* @__PURE__ */ jsxs("section", { className: "stock-alerts", children: [
              /* @__PURE__ */ jsxs("header", { children: [
                /* @__PURE__ */ jsxs("div", { children: [
                  /* @__PURE__ */ jsx("span", { className: "kpi-icon red", children: /* @__PURE__ */ jsx(AlertTriangle, {}) }),
                  /* @__PURE__ */ jsxs("div", { children: [
                    /* @__PURE__ */ jsx("h2", { children: "Alertas cr\xEDticas de stock" }),
                    /* @__PURE__ */ jsx("small", { children: "Productos en o por debajo del m\xEDnimo configurado" })
                  ] })
                ] }),
                /* @__PURE__ */ jsx("button", { onClick: () => setShowAlerts(false), "aria-label": "Ocultar alertas", children: /* @__PURE__ */ jsx(X, {}) })
              ] }),
              /* @__PURE__ */ jsxs("div", { className: "alert-grid", children: [
                lowStockProducts.slice(0, 3).map((product) => /* @__PURE__ */ jsxs("article", { children: [
                  /* @__PURE__ */ jsxs("div", { children: [
                    /* @__PURE__ */ jsx("b", { children: product.name }),
                    /* @__PURE__ */ jsx("span", { className: "alert-pill warning", children: "Cr\xEDtico" })
                  ] }),
                  /* @__PURE__ */ jsxs("small", { children: [
                    product.brand,
                    " \xB7 M\xEDnimo: ",
                    product.stockMinimum
                  ] }),
                  /* @__PURE__ */ jsxs("p", { children: [
                    /* @__PURE__ */ jsx(Warehouse, {}),
                    " Stock: ",
                    /* @__PURE__ */ jsxs("b", { children: [
                      product.stock,
                      " unidades"
                    ] })
                  ] }),
                  /* @__PURE__ */ jsxs("button", { onClick: () => setSection("Stock"), children: [
                    "Ajustar stock ",
                    /* @__PURE__ */ jsx(ArrowRight, {})
                  ] })
                ] }, product.id)),
                !lowStockProducts.length && /* @__PURE__ */ jsx("div", { className: "ops-empty", children: "No hay alertas de stock." })
              ] })
            ] })
          ] }),
          section === "Pedidos" && /* @__PURE__ */ jsx(OrdersAdminPage, { orders, inventory, loading, onRefresh: () => loadData(true), onStatusChange: advanceOrder }),
          section === "Productos" && /* @__PURE__ */ jsx(ProductsAdminPage, { apiAdmin: api, onStockChange: changeStock }),
          section === "Stock" && /* @__PURE__ */ jsx(ProductsAdminPage, { apiAdmin: api, onStockChange: changeStock, stockOnly: true }),
          section === "Facturaci\xF3n y pagos" && /* @__PURE__ */ jsx(BillingPaymentsAdminPage, { orders, loading, onRefresh: () => loadData(true) }),
          section === "Despacho" && /* @__PURE__ */ jsxs(Fragment, { children: [
            /* @__PURE__ */ jsx(PendingDispatchCard, { orders: pendingDispatch, onAssign: setDispatchTarget }),
            /* @__PURE__ */ jsxs("section", { className: "orders-table-card ops-dispatch-table", children: [
              /* @__PURE__ */ jsxs("header", { className: "table-title", children: [
                /* @__PURE__ */ jsxs("div", { children: [
                  /* @__PURE__ */ jsx("h2", { children: "Seguimiento de despachos" }),
                  /* @__PURE__ */ jsx("p", { children: "Pedidos en camino y entregados" })
                ] }),
                /* @__PURE__ */ jsxs("b", { children: [
                  dispatchOrders.length,
                  " pedidos"
                ] })
              ] }),
              /* @__PURE__ */ jsx(DataRows, { orders: dispatchOrders, onAdvance: advanceOrder, showCarrier: true, empty: "No hay pedidos en tr\xE1nsito." })
            ] })
          ] }),
          section === "Reportes" && /* @__PURE__ */ jsx(ReportsAdminPage, { apiAdmin: api, role: user?.role }),
          section === "Mi cuenta" && /* @__PURE__ */ jsxs("section", { className: "admin-account-card", children: [
            /* @__PURE__ */ jsx("span", { className: "ops-user-avatar", children: "DM" }),
            /* @__PURE__ */ jsx("h2", { children: user?.fullName || user?.username || "Cuenta operativa" }),
            /* @__PURE__ */ jsx("p", { children: "Acceso interno de Dulce Mayor" }),
            /* @__PURE__ */ jsxs("div", { children: [
              /* @__PURE__ */ jsx("b", { children: "Ubicaci\xF3n" }),
              /* @__PURE__ */ jsx("span", { children: "Dep\xF3sito Central \xB7 Godoy Cruz" })
            ] }),
            /* @__PURE__ */ jsxs("div", { children: [
              /* @__PURE__ */ jsx("b", { children: "Panel" }),
              /* @__PURE__ */ jsx("span", { children: "Administraci\xF3n y operaciones" })
            ] })
          ] }),
          section === "Sucursales" && /* @__PURE__ */ jsx(BranchesAdmin, {})
          ,section === "Clientes" && /* @__PURE__ */ jsx(ClientsAdminPage, {})
          ,section === "Usuarios internos" && user?.role === "JEFE_ADMINISTRATIVO" && /* @__PURE__ */ jsx(UsersAdminPage, {})
          ,section === "Promociones" && user?.role === "JEFE_ADMINISTRATIVO" && /* @__PURE__ */ jsx(PromotionsAdminPage, {})
          ,section === "Estadísticas" && user?.role === "JEFE_ADMINISTRATIVO" && /* @__PURE__ */ jsx(Suspense, { fallback: /* @__PURE__ */ jsx("div", { className: "stats-empty", children: "Cargando gráficos…" }), children: /* @__PURE__ */ jsx(StatisticsAdminPage, { apiAdmin: api }) })
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "ops-footer-note", children: [
          /* @__PURE__ */ jsx(PackageCheck, {}),
          " Datos conectados a la API de Dulce Mayor ",
          /* @__PURE__ */ jsx("span", { children: "\xB7" }),
          " Actualiz\xE1 para sincronizar"
        ] })
      ] })
    ] }),
    manualOrderOpen && /* @__PURE__ */ jsx(ManualOrderModal, { products: inventory, onClose: () => setManualOrderOpen(false), onCreated: createManualOrderDone }),
    dispatchTarget && /* @__PURE__ */ jsx(DispatchDialog, { orders: dispatchTarget, onClose: () => setDispatchTarget(null), onDone: dispatchDone })
  ] });
}
function Kpi({ icon, color, label, value, suffix, detail, onClick }) {
  return /* @__PURE__ */ jsxs("article", { className: "kpi-card", children: [
    /* @__PURE__ */ jsx("span", { className: `kpi-icon ${color}`, children: icon }),
    /* @__PURE__ */ jsx("small", { children: label }),
    /* @__PURE__ */ jsxs("strong", { children: [
      value,
      " ",
      /* @__PURE__ */ jsx("em", { children: suffix })
    ] }),
    /* @__PURE__ */ jsxs("button", { onClick, children: [
      detail,
      " ",
      /* @__PURE__ */ jsx(ArrowRight, {})
    ] })
  ] });
}
function DataRows({ orders, onAdvance, showCarrier = false, empty }) {
  return /* @__PURE__ */ jsxs("div", { className: "ops-table-wrap", children: [
    /* @__PURE__ */ jsxs("table", { className: "ops-table", children: [
      /* @__PURE__ */ jsx("thead", { children: /* @__PURE__ */ jsxs("tr", { children: [
        /* @__PURE__ */ jsx("th", { children: "Pedido" }),
        /* @__PURE__ */ jsx("th", { children: "Cliente" }),
        /* @__PURE__ */ jsx("th", { children: "Fecha" }),
        /* @__PURE__ */ jsx("th", { children: "Total" }),
        /* @__PURE__ */ jsx("th", { children: "Estado" }),
        showCarrier && /* @__PURE__ */ jsx("th", { children: "Transportista" }),
        /* @__PURE__ */ jsx("th", { children: "Acci\xF3n" })
      ] }) }),
      /* @__PURE__ */ jsx("tbody", { children: orders.map((order) => /* @__PURE__ */ jsxs("tr", { children: [
        /* @__PURE__ */ jsxs("td", { children: [
          /* @__PURE__ */ jsx("b", { children: orderId(order.id) }),
          /* @__PURE__ */ jsxs("small", { children: [
            order.items.length,
            " art\xEDculos"
          ] })
        ] }),
        /* @__PURE__ */ jsx("td", { children: order.clientName }),
        /* @__PURE__ */ jsx("td", { children: orderDate(order.invoicedAt || order.createdAt) }),
        /* @__PURE__ */ jsx("td", { children: /* @__PURE__ */ jsx("b", { children: currency.format(order.total) }) }),
        /* @__PURE__ */ jsx("td", { children: stateLabels[order.status] || order.status }),
        showCarrier && /* @__PURE__ */ jsx("td", { children: order.trace?.carrier?.name || "\u2014" }),
        /* @__PURE__ */ jsx("td", { children: onAdvance && nextState[order.status] && /* @__PURE__ */ jsxs("button", { className: "row-action", onClick: () => onAdvance(order), children: [
          "Avanzar ",
          /* @__PURE__ */ jsx(ArrowRight, {})
        ] }) })
      ] }, order.id)) })
    ] }),
    orders.length === 0 && /* @__PURE__ */ jsx("div", { className: "ops-empty", children: empty })
  ] });
}
