import { jsx, jsxs } from "react/jsx-runtime";
import { LayoutDashboard, LogOut, Menu, Search, ShoppingBag, UserRound, X } from "lucide-react";
import { useState } from "react";
import { Link } from "react-router-dom";
import { ADMIN_ROLES } from "../config";
import { DollarQuoteBadge } from "./DollarQuoteBadge";
export function Header({ count, account, onCartClick, onOrdersClick, onLoginClick, onProfileClick, onAdminClick, onLogoutClick }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const canManage = Boolean(account && ADMIN_ROLES.includes(account.role));
  return /* @__PURE__ */ jsx("header", { className: "site-header", children: /* @__PURE__ */ jsxs("div", { className: "header-inner", children: [
    /* @__PURE__ */ jsxs(Link, { className: "brand", to: "/#inicio", "aria-label": "Dulce Mayor, inicio", children: [
      /* @__PURE__ */ jsx("span", { className: "brand-mark", children: "DM" }),
      /* @__PURE__ */ jsxs("span", { className: "brand-copy", children: [
        /* @__PURE__ */ jsx("strong", { children: "Dulce Mayor" }),
        /* @__PURE__ */ jsx("small", { children: "MAYORISTA B2B \xB7 MENDOZA" })
      ] })
    ] }),
    /* @__PURE__ */ jsx(DollarQuoteBadge, {}),
    /* @__PURE__ */ jsxs("nav", { className: menuOpen ? "main-nav nav-open" : "main-nav", children: [
      /* @__PURE__ */ jsx(Link, { to: "/#catalogo", onClick: () => setMenuOpen(false), children: "Cat\xE1logo" }),
      /* @__PURE__ */ jsx("button", { className: "nav-link", onClick: () => {
        onOrdersClick();
        setMenuOpen(false);
      }, children: "Mis pedidos" }),
      /* @__PURE__ */ jsx(Link, { to: "/#sucursales", onClick: () => setMenuOpen(false), children: "Sucursales" }),
      /* @__PURE__ */ jsx(Link, { to: "/#ayuda", onClick: () => setMenuOpen(false), children: "Ayuda" }),
      canManage && /* @__PURE__ */ jsxs("button", { className: "nav-link mobile-admin", onClick: () => {
        onAdminClick();
        setMenuOpen(false);
      }, children: [
        /* @__PURE__ */ jsx(LayoutDashboard, { size: 15 }),
        " Panel administrativo"
      ] }),
      /* @__PURE__ */ jsx("button", { className: "nav-link mobile-login", onClick: () => {
        account ? onProfileClick() : onLoginClick();
        setMenuOpen(false);
      }, children: account ? "Mi perfil" : "Ingresar a mi cuenta" })
      ,account && /* @__PURE__ */ jsxs("button", { className: "nav-link mobile-logout", onClick: () => {
        onLogoutClick();
        setMenuOpen(false);
      }, children: [/* @__PURE__ */ jsx(LogOut, { size: 15 }), " Cerrar sesión"] })
    ] }),
    /* @__PURE__ */ jsxs("div", { className: "header-actions", children: [
      canManage && /* @__PURE__ */ jsxs("button", { className: "admin-shortcut", onClick: onAdminClick, "aria-label": "Ir al panel administrativo", title: "Panel administrativo", children: [
        /* @__PURE__ */ jsx(LayoutDashboard, { size: 16 }),
        /* @__PURE__ */ jsx("span", { children: "Panel admin" })
      ] }),
      account ? /* @__PURE__ */ jsxs("button", { className: "login-button profile-button", onClick: onProfileClick, "aria-label": `Mi perfil${account.displayName ? `: ${account.displayName}` : ""}`, title: account.displayName, children: [
        /* @__PURE__ */ jsx(UserRound, { size: 17 }),
        " ",
        /* @__PURE__ */ jsx("span", { children: "Mi perfil" })
      ] }) : /* @__PURE__ */ jsxs("button", { className: "login-button", onClick: onLoginClick, children: [
        /* @__PURE__ */ jsx(UserRound, { size: 17 }),
        " ",
        /* @__PURE__ */ jsx("span", { children: "Ingresar" })
      ] }),
      account && /* @__PURE__ */ jsxs("button", { className: "login-button logout-button", onClick: onLogoutClick, children: [
        /* @__PURE__ */ jsx(LogOut, { size: 16 }),
        /* @__PURE__ */ jsx("span", { children: "Cerrar sesión" })
      ] }),
      /* @__PURE__ */ jsxs("button", { className: "cart-button", onClick: onCartClick, children: [
        /* @__PURE__ */ jsx(ShoppingBag, { size: 17 }),
        /* @__PURE__ */ jsx("span", { children: "Presupuesto" }),
        /* @__PURE__ */ jsx("b", { children: count })
      ] }),
      /* @__PURE__ */ jsx("button", { className: "menu-toggle", onClick: () => setMenuOpen(!menuOpen), "aria-label": menuOpen ? "Cerrar menu" : "Abrir menu", children: menuOpen ? /* @__PURE__ */ jsx(X, {}) : /* @__PURE__ */ jsx(Menu, {}) })
    ] })
  ] }) });
}
export function SearchBar({ value, onChange }) {
  return /* @__PURE__ */ jsxs("label", { className: "search-field", children: [
    /* @__PURE__ */ jsx(Search, { size: 19 }),
    /* @__PURE__ */ jsx("input", { value, onChange: (e) => onChange(e.target.value), placeholder: "Buscar por producto, marca o EAN..." }),
    /* @__PURE__ */ jsx("kbd", { children: "\u2318 K" })
  ] });
}
