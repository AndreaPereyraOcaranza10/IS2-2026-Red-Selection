import { Fragment, jsx, jsxs } from "react/jsx-runtime";
import { useEffect, useMemo, useState } from "react";
import { Headphones, ShoppingBag } from "lucide-react";
import { Navigate, Route, Routes, useLocation, useNavigate, useParams } from "react-router-dom";
import { products as demoProducts } from "./data/products";
import { api } from "./api";
import { ADMIN_ROLES, ACCOUNT_STORAGE_KEY, ROUTES } from "./config";
import { Header } from "./components/Header";
import { CartPanel } from "./components/CartPanel";
import { HomePage } from "./pages/HomePage";
import { ProductDetailPage } from "./pages/ProductDetailPage";
import { OrdersPage } from "./pages/OrdersPage";
import { ProfilePage } from "./pages/ProfilePage";
import { LoginPage } from "./pages/LoginPage";
import { OperationsDashboardPage } from "./pages/OperationsDashboardPage";
function readAccount() {
  try {
    const value = sessionStorage.getItem(ACCOUNT_STORAGE_KEY);
    return value ? JSON.parse(value) : null;
  } catch {
    return null;
  }
}
function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("Ver todo");
  const [quantities, setQuantities] = useState({});
  const [cartOpen, setCartOpen] = useState(false);
  const [catalog, setCatalog] = useState(demoProducts);
  const [account, setAccount] = useState(readAccount);
  const [apiNotice, setApiNotice] = useState("");
  const [checkoutError, setCheckoutError] = useState("");
  const [checkingOut, setCheckingOut] = useState(false);
  useEffect(() => {
    api.products().then(setCatalog).catch(() => setApiNotice("Mostrando cat\xE1logo de demostraci\xF3n."));
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
  const addProduct = (id) => setQuantities((previous) => ({ ...previous, [String(id)]: (previous[String(id)] || 0) + 1 }));
  const handleOrder = async () => {
    if (!account) {
      setCartOpen(false);
      goTo(ROUTES.login);
      return;
    }
    setCheckingOut(true);
    setCheckoutError("");
    try {
      await api.createOrder(Object.entries(quantities).map(([productId, quantity]) => ({ productId, quantity })));
      setQuantities({});
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
      onAdminClick: () => goTo(ROUTES.admin),
      onLogoutClick: logout
    }
  );
  const footerActions = /* @__PURE__ */ jsxs(Fragment, { children: [
    location.pathname !== "/ingresar" && /* @__PURE__ */ jsxs("button", { className: "help-button", "aria-label": "Contactar con atenci\xF3n al cliente", children: [
      /* @__PURE__ */ jsx(Headphones, { size: 19 }),
      /* @__PURE__ */ jsx("span", { children: "\xBFNecesit\xE1s ayuda?" })
    ] }),
    count > 0 && /* @__PURE__ */ jsxs("button", { className: "mobile-cart", onClick: () => setCartOpen(true), children: [
      /* @__PURE__ */ jsx(ShoppingBag, { size: 18 }),
      " Presupuesto (",
      count,
      ")"
    ] }),
    /* @__PURE__ */ jsx(CartPanel, { open: cartOpen, onClose: () => setCartOpen(false), products: catalog, quantities, onCheckout: handleOrder, error: checkoutError, submitting: checkingOut })
  ] });
  return /* @__PURE__ */ jsxs(Fragment, { children: [
    /* @__PURE__ */ jsxs(Routes, { children: [
      /* @__PURE__ */ jsx(Route, { path: ROUTES.home, element: /* @__PURE__ */ jsxs(Fragment, { children: [
        header,
        /* @__PURE__ */ jsx(HomePage, { filtered, catalogCount: filtered.length, apiNotice, category, search, onCategory: setCategory, onSearch: setSearch, quantities, onAdd: addProduct, onDetails: (id) => goTo(ROUTES.product(id)) })
      ] }) }),
      /* @__PURE__ */ jsx(Route, { path: ROUTES.productDetail, element: /* @__PURE__ */ jsxs(Fragment, { children: [
        header,
        /* @__PURE__ */ jsx(ProductRoute, { catalog, onBack: () => goTo(ROUTES.catalog), onAdd: (id, amount) => {
          setQuantities((previous) => ({ ...previous, [id]: (previous[id] || 0) + amount }));
          setCartOpen(true);
        } })
      ] }) }),
      /* @__PURE__ */ jsx(Route, { path: ROUTES.orders, element: /* @__PURE__ */ jsxs(Fragment, { children: [
        header,
        /* @__PURE__ */ jsx(OrdersPage, { onBack: () => goTo(ROUTES.catalog), onProfile: () => goTo(ROUTES.profile) })
      ] }) }),
      /* @__PURE__ */ jsx(Route, { path: ROUTES.profile, element: /* @__PURE__ */ jsxs(Fragment, { children: [
        header,
        /* @__PURE__ */ jsx(ProfilePage, { onBack: () => goTo(ROUTES.home) })
      ] }) }),
      /* @__PURE__ */ jsx(Route, { path: ROUTES.login, element: /* @__PURE__ */ jsxs(Fragment, { children: [
        header,
        /* @__PURE__ */ jsx(LoginPage, { onSuccess: async (email, password) => {
          const session = await api.login(email, password);
          setAccount(session);
          sessionStorage.setItem(ACCOUNT_STORAGE_KEY, JSON.stringify(session));
          goTo(ADMIN_ROLES.includes(session.role) ? ROUTES.admin : ROUTES.home);
        }, onStaff: () => goTo(ROUTES.admin) })
      ] }) }),
      /* @__PURE__ */ jsx(Route, { path: "/admin/*", element: /* @__PURE__ */ jsx(OperationsDashboardPage, { onBack: () => goTo(ROUTES.home) }) }),
      /* @__PURE__ */ jsx(Route, { path: "*", element: /* @__PURE__ */ jsx(Navigate, { to: ROUTES.home, replace: true }) })
    ] }),
    footerActions
  ] });
}
function ProductRoute({ catalog, onBack, onAdd }) {
  const { id } = useParams();
  const product = catalog.find((item) => String(item.id) === id) || demoProducts.find((item) => String(item.id) === id) || demoProducts[0];
  return /* @__PURE__ */ jsx(ProductDetailPage, { product, onBack, onAdd: (quantity) => onAdd(String(product.id), quantity) }, id);
}
export default App;
