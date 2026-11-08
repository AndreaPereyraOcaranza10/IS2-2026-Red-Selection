import { Fragment, jsx, jsxs } from "react/jsx-runtime";
import { ArrowRight, Check, LoaderCircle, ShoppingBag, X } from "lucide-react";
const currency = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 });
export function CartPanel({ open, onClose, products, quantities, onCheckout, error = "", submitting = false }) {
  const items = products.filter((product) => quantities[String(product.id)]);
  const total = items.reduce((sum, product) => sum + product.price * quantities[String(product.id)], 0);
  return /* @__PURE__ */ jsxs(Fragment, { children: [
    /* @__PURE__ */ jsx("button", { "aria-label": "Cerrar presupuesto", className: open ? "cart-backdrop visible" : "cart-backdrop", onClick: onClose }),
    /* @__PURE__ */ jsxs("aside", { className: open ? "cart-panel open" : "cart-panel", "aria-hidden": !open, children: [
      /* @__PURE__ */ jsxs("div", { className: "cart-head", children: [
        /* @__PURE__ */ jsxs("div", { children: [
          /* @__PURE__ */ jsx("span", { className: "section-kicker", children: "TU PEDIDO" }),
          /* @__PURE__ */ jsxs("h2", { children: [
            "Presupuesto ",
            /* @__PURE__ */ jsxs("span", { children: [
              "(",
              items.length,
              ")"
            ] })
          ] })
        ] }),
        /* @__PURE__ */ jsx("button", { className: "icon-button", onClick: onClose, "aria-label": "Cerrar", children: /* @__PURE__ */ jsx(X, {}) })
      ] }),
      items.length ? /* @__PURE__ */ jsxs(Fragment, { children: [
        /* @__PURE__ */ jsx("div", { className: "cart-items", children: items.map((item) => /* @__PURE__ */ jsxs("div", { className: "cart-item", children: [
          /* @__PURE__ */ jsx("img", { src: item.image, alt: "" }),
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("b", { children: item.name }),
            /* @__PURE__ */ jsxs("small", { children: [
              quantities[String(item.id)],
              " \xD7 ",
              currency.format(item.price)
            ] })
          ] }),
          /* @__PURE__ */ jsx("strong", { children: currency.format(item.price * quantities[String(item.id)]) })
        ] }, item.id)) }),
        /* @__PURE__ */ jsxs("div", { className: "cart-total", children: [
          /* @__PURE__ */ jsx("span", { children: "Subtotal estimado" }),
          /* @__PURE__ */ jsx("b", { children: currency.format(total) })
        ] }),
        /* @__PURE__ */ jsxs("p", { className: "cart-note", children: [
          /* @__PURE__ */ jsx(Check, { size: 15 }),
          " El total final se confirma con el precio vigente del cat\xE1logo."
        ] }),
        error && /* @__PURE__ */ jsx("p", { className: "cart-error", role: "alert", children: error }),
        /* @__PURE__ */ jsx("button", { className: "checkout-button", onClick: onCheckout, disabled: submitting, children: submitting ? /* @__PURE__ */ jsxs(Fragment, { children: [
          /* @__PURE__ */ jsx(LoaderCircle, { className: "spin" }),
          " Enviando pedido..."
        ] }) : /* @__PURE__ */ jsxs(Fragment, { children: [
          "Confirmar pedido ",
          /* @__PURE__ */ jsx(ArrowRight, { size: 17 })
        ] }) })
      ] }) : /* @__PURE__ */ jsxs("div", { className: "empty-cart", children: [
        /* @__PURE__ */ jsx("span", { children: /* @__PURE__ */ jsx(ShoppingBag, {}) }),
        /* @__PURE__ */ jsx("h3", { children: "Tu presupuesto est\xE1 vac\xEDo" }),
        /* @__PURE__ */ jsx("p", { children: "Agreg\xE1 productos del cat\xE1logo para armar tu pedido mayorista." }),
        /* @__PURE__ */ jsx("button", { onClick: onClose, children: "Explorar productos" })
      ] })
    ] })
  ] });
}
