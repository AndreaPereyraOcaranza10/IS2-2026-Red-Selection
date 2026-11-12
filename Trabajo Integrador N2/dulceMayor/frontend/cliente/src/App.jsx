import { Fragment, jsx, jsxs } from "react/jsx-runtime";
import { useEffect, useMemo, useState } from "react";
import { Headphones, ShoppingBag } from "lucide-react";
import { Navigate, Route, Routes, useLocation, useNavigate, useParams } from "react-router-dom";
import { api } from "./api";
import { ADMIN_ROLES, ACCOUNT_STORAGE_KEY, ROUTES, adminAppUrl } from "./config";
import { Header } from "./components/Header";
import { CartPanel } from "./components/CartPanel";
import { BudgetAssistant } from "./components/BudgetAssistant";
import { HomePage } from "./pages/HomePage";
import { ProductDetailPage } from "./pages/ProductDetailPage";
import { OrdersPage } from "./pages/OrdersPage";
import { ProfilePage } from "./pages/ProfilePage";
import { LoginPage } from "./pages/LoginPage";
import { ForcedPasswordPage } from "./pages/ForcedPasswordPage";

function AdminAppRedirect() {
  useEffect(() => {
    const destination = `${window.location.pathname}${window.location.search}${window.location.hash}`;
    window.location.replace(adminAppUrl(destination));
  }, []);
  return /* @__PURE__ */ jsx("main", { className: "admin-redirect", children: "Abriendo el panel administrativo…" });
}
function readAccount() {
  try {
    const value = sessionStorage.getItem(ACCOUNT_STORAGE_KEY);
    return value ? JSON.parse(value) : null;
  } catch {
    return null;
  }
}
function App({ auth0Configured = false }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("Ver todo");
  const [quantities, setQuantities] = useState({});
  const [cartOpen, setCartOpen] = useState(false);
  const [assistantOpen, setAssistantOpen] = useState(false);
  const [catalog, setCatalog] = useState([]);
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [account, setAccount] = useState(readAccount);
  const [apiNotice, setApiNotice] = useState("");
  const [checkoutError, setCheckoutError] = useState("");
  const [checkingOut, setCheckingOut] = useState(false);
  const [promoCode, setPromoCode] = useState("");
  const [promotion, setPromotion] = useState(null);
  const [promoLoading, setPromoLoading] = useState(false);
  const [promoError, setPromoError] = useState("");
  useEffect(() => {
    api.products().then((products) => {
      setCatalog(products);
      setApiNotice("");
    }).catch(() => {
      setCatalog([]);
      setApiNotice("No se pudo cargar el cat\xE1logo real. Revis\xE1 la conexi\xF3n con la API e intent\xE1 nuevamente.");
    }).finally(() => setCatalogLoading(false));
  }, []);
  useEffect(() => {
    if (!location.hash) {
      window.scrollTo({ top: 0, behavior: "smooth" });
      return;
    }
    const id = decodeURIComponent(location.hash.slice(1));
    requestAnimationFrame(() => document.getElementById(id)?.scrollIntoView());
  }, [location.pathname, location.hash]);
  const count = Object.values(quantities).reduce((total, quantity) => total + quantity, 0);
  const cartSubtotal = catalog.reduce((sum, product) => sum + product.price * (quantities[String(product.id)] || 0), 0);
  const estimatedDiscount = promotion ? Math.round((promotion.type === "PRODUCTO_DESTACADO"
    ? catalog.filter((product) => String(product.id) === String(promotion.productId)).reduce((sum, product) => sum + product.price * (quantities[String(product.id)] || 0), 0)
    : cartSubtotal) * Number(promotion.discountPercent || 0) / 100) : 0;
  const categories = useMemo(() => [...new Set(catalog.map((product) => product.category).filter(Boolean))].sort((a, b) => a.localeCompare(b, "es")), [catalog]);
  const filtered = useMemo(() => catalog.filter((product) => (category === "Ver todo" || product.category === category) && `${product.name} ${product.brand} ${product.category}`.toLowerCase().includes(search.toLowerCase())), [catalog, category, search]);
  const goTo = (path) => {
    navigate(path);
  };
  const logout = () => {
    sessionStorage.removeItem(ACCOUNT_STORAGE_KEY);
    setAccount(null);
    setCartOpen(false);
    navigate(ROUTES.login);
  };
  const persistAccount = (session) => {
    setAccount(session);
    sessionStorage.setItem(ACCOUNT_STORAGE_KEY, JSON.stringify(session));
  };
  useEffect(() => {
    if (account?.mustChangePassword && location.pathname !== "/cambiar-clave") {
      navigate("/cambiar-clave", { replace: true });
    }
  }, [account, location.pathname, navigate]);
  const addProduct = (id, amount = 1) => {
    setPromotion(null);
    setPromoError("");
    setQuantities((previous) => {
      const key = String(id);
      const product = catalog.find((item) => String(item.id) === key);
      const stock = Number(product?.stock);
      if (!product || !Number.isFinite(stock) || stock <= 0) return previous;
      const increment = Math.max(1, Math.trunc(Number(amount) || 1));
      return { ...previous, [key]: Math.min(stock, (previous[key] || 0) + increment) };
    });
  };
  const removeProduct = (id) => {
    setPromotion(null);
    setPromoError("");
    setQuantities((previous) => {
      const next = { ...previous };
      delete next[String(id)];
      return next;
    });
    setCheckoutError("");
  };
  const addAssistantItems = (items) => {
    items.forEach((item) => addProduct(item.productoId, item.cantidad));
    setAssistantOpen(false);
    setCartOpen(true);
  };
  const applyPromotion = async () => {
    if (!account) {
      setPromoError("Iniciá sesión como cliente para validar un código de descuento.");
      return;
    }
    if (ADMIN_ROLES.includes(account.role)) {
      setPromoError("Los códigos de descuento se aplican desde una cuenta de cliente.");
      return;
    }
    setPromoLoading(true);
    setPromoError("");
    setPromotion(null);
    try {
      const result = await api.validatePromotion(promoCode.trim());
      if (!result?.valid) throw new Error(result?.message || "El código no es válido.");
      if (result.type === "VOLUMEN" && count < Number(result.minQuantity || 0)) {
        throw new Error(`Este código requiere al menos ${result.minQuantity} unidades en el pedido.`);
      }
      if (result.type === "PRODUCTO_DESTACADO" && !Object.entries(quantities).some(([id, quantity]) => quantity > 0 && id === String(result.productId))) {
        throw new Error(`Agregá ${result.productName || "el producto destacado"} al pedido para usar este código.`);
      }
      setPromotion(result);
    } catch (error) {
      setPromoError(error instanceof Error ? error.message : "No se pudo validar el código.");
    } finally {
      setPromoLoading(false);
    }
  };
  const handleOrder = async () => {
    if (!account) {
      setCartOpen(false);
      goTo(ROUTES.login);
      return;
    }
    if (ADMIN_ROLES.includes(account.role)) {
      setCheckoutError("Esta sesión es del personal. Para cargar el pedido, usá «Nuevo pedido manual» desde el panel administrativo.");
      setCartOpen(true);
      return;
    }
    setCheckingOut(true);
    setCheckoutError("");
    try {
      await api.createOrder(Object.entries(quantities).map(([productId, quantity]) => ({ productId, quantity })), promotion ? promoCode.trim() : undefined);
      setQuantities({});
      setPromotion(null);
      setPromoCode("");
      setCartOpen(false);
      goTo(ROUTES.orders);
    } catch (error) {
      setCheckoutError(error instanceof Error ? error.message : "No se pudo enviar el pedido.");
    } finally {
      setCheckingOut(false);
    }
  };
  const header = /* @__PURE__ */ jsx(
    Header,
    {
      count,
      account,
      onCartClick: () => setCartOpen(true),
      onOrdersClick: () => goTo(ROUTES.orders),
      onLoginClick: () => goTo(ROUTES.login),
      onProfileClick: () => goTo(ROUTES.profile),
      onAdminClick: () => window.location.assign(adminAppUrl(ROUTES.admin)),
      onLogoutClick: logout
    }
  );
  const footerActions = /* @__PURE__ */ jsxs(Fragment, { children: [
    location.pathname !== "/ingresar" && /* @__PURE__ */ jsxs("button", { className: "help-button", onClick: () => setAssistantOpen(true), "aria-label": "Abrir asistente de presupuestos", children: [
      /* @__PURE__ */ jsx(Headphones, { size: 19 }),
      /* @__PURE__ */ jsx("span", { children: "\xBFNecesit\xE1s ayuda?" })
    ] }),
    /* @__PURE__ */ jsx(BudgetAssistant, { open: assistantOpen, onClose: () => setAssistantOpen(false), onAddItems: addAssistantItems }),
    count > 0 && /* @__PURE__ */ jsxs("button", { className: "mobile-cart", onClick: () => setCartOpen(true), children: [
      /* @__PURE__ */ jsx(ShoppingBag, { size: 18 }),
      " Presupuesto (",
      count,
      ")"
    ] }),
    /* @__PURE__ */ jsx(CartPanel, { open: cartOpen, onClose: () => setCartOpen(false), products: catalog, quantities, onRemoveProduct: removeProduct, onCheckout: handleOrder, error: checkoutError, submitting: checkingOut, promoCode, onPromoCodeChange: (value) => { setPromoCode(value); setPromotion(null); setPromoError(""); }, onApplyPromo: applyPromotion, onRemovePromo: () => { setPromotion(null); setPromoError(""); }, promotion, promoLoading, promoError, estimatedDiscount })
  ] });
  return /* @__PURE__ */ jsxs(Fragment, { children: [
    /* @__PURE__ */ jsxs(Routes, { children: [
      /* @__PURE__ */ jsx(Route, { path: ROUTES.home, element: /* @__PURE__ */ jsxs(Fragment, { children: [
        header,
        /* @__PURE__ */ jsx(HomePage, { filtered, categories, catalogCount: filtered.length, catalogLoading, apiNotice, category, search, onCategory: setCategory, onSearch: setSearch, quantities, onAdd: addProduct, onDetails: (id) => goTo(ROUTES.product(id)) })
      ] }) }),
      /* @__PURE__ */ jsx(Route, { path: ROUTES.productDetail, element: /* @__PURE__ */ jsxs(Fragment, { children: [
        header,
        /* @__PURE__ */ jsx(ProductRoute, { catalog, quantities, onBack: () => goTo(ROUTES.catalog), onAdd: (id, amount) => {
          addProduct(id, amount);
          setCartOpen(true);
        } })
      ] }) }),
      /* @__PURE__ */ jsx(Route, { path: ROUTES.orders, element: /* @__PURE__ */ jsxs(Fragment, { children: [
        header,
        /* @__PURE__ */ jsx(OrdersPage, { onBack: () => goTo(ROUTES.catalog), onProfile: () => goTo(ROUTES.profile), account })
      ] }) }),
      /* @__PURE__ */ jsx(Route, { path: ROUTES.profile, element: /* @__PURE__ */ jsxs(Fragment, { children: [
        header,
        /* @__PURE__ */ jsx(ProfilePage, { onBack: () => goTo(ROUTES.home), onPasswordChanged: persistAccount })
      ] }) }),
      /* @__PURE__ */ jsx(Route, { path: "/cambiar-clave", element: /* @__PURE__ */ jsx(ForcedPasswordPage, { account, onComplete: (session) => { if (ADMIN_ROLES.includes(session.role)) window.location.assign(adminAppUrl(ROUTES.admin)); else { persistAccount(session); goTo(ROUTES.home); } } }) }),
      /* @__PURE__ */ jsx(Route, { path: ROUTES.login, element: /* @__PURE__ */ jsxs(Fragment, { children: [
        header,
        /* @__PURE__ */ jsx(LoginPage, { auth0Configured, onSuccess: async (email, password) => {
          const session = await api.login(email, password);
          if (ADMIN_ROLES.includes(session.role)) {
            window.location.assign(adminAppUrl(ROUTES.admin));
            return;
          }
          persistAccount(session);
          goTo(session.mustChangePassword ? "/cambiar-clave" : ROUTES.home);
        }, onAuth0Success: (session) => {
          persistAccount(session);
          goTo(ROUTES.home);
        }, onStaff: () => window.location.assign(adminAppUrl("/login")) })
      ] }) }),
      /* @__PURE__ */ jsx(Route, { path: "/admin/*", element: /* @__PURE__ */ jsx(AdminAppRedirect, {}) }),
      /* @__PURE__ */ jsx(Route, { path: "*", element: /* @__PURE__ */ jsx(Navigate, { to: ROUTES.home, replace: true }) })
    ] }),
    footerActions
  ] });
}
function ProductRoute({ catalog, quantities, onBack, onAdd }) {
  const { id } = useParams();
  const catalogProduct = catalog.find((item) => String(item.id) === id);
  const [product, setProduct] = useState(catalogProduct || null);
  const [loading, setLoading] = useState(!catalogProduct);
  useEffect(() => {
    let active = true;
    if (catalogProduct) {
      setProduct(catalogProduct);
      setLoading(false);
      return () => { active = false; };
    }
    setLoading(true);
    api.product(id).then((value) => {
      if (active) setProduct(value);
    }).catch(() => {
      if (active) setProduct(null);
    }).finally(() => {
      if (active) setLoading(false);
    });
    return () => { active = false; };
  }, [catalogProduct, id]);
  if (loading) return /* @__PURE__ */ jsx("main", { className: "detail-page", children: "Cargando producto..." });
  if (!product) return /* @__PURE__ */ jsxs("main", { className: "detail-page", children: [
    /* @__PURE__ */ jsx("h1", { children: "Producto no disponible" }),
    /* @__PURE__ */ jsx("p", { children: "No encontramos este producto en el cat\xE1logo." }),
    /* @__PURE__ */ jsx("button", { className: "back-link", onClick: onBack, children: "Volver al cat\xE1logo" })
  ] });
  return /* @__PURE__ */ jsx(ProductDetailPage, { product, quantityInCart: quantities[String(product.id)] || 0, onBack, onAdd: (quantity) => onAdd(String(product.id), quantity) }, id);
}
export default App;
