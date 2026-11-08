import { jsx, jsxs } from "react/jsx-runtime";
import { useState } from "react";
import { ArrowLeft, BadgeCheck, Check, ChevronRight, Package, ShieldCheck, ShoppingBag, Store, Truck } from "lucide-react";
const money = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 });
export function ProductDetailPage({ product, onBack, onAdd }) {
  const [tier, setTier] = useState(2);
  const [quantity, setQuantity] = useState(1);
  const tiers = [
    { name: "Unidad", detail: "1 unidad", price: Math.round(product.unitPrice), saving: "" },
    { name: "Bulto cerrado", detail: product.pack, price: product.price, saving: "" },
    { name: "Bulto pallet", detail: "6 bultos", price: Math.round(product.price * 6 * 0.9), saving: "-10%" }
  ];
  const selected = tiers[tier];
  const total = selected.price * quantity;
  return /* @__PURE__ */ jsxs("main", { className: "detail-page", children: [
    /* @__PURE__ */ jsxs("button", { className: "back-link", onClick: onBack, children: [
      /* @__PURE__ */ jsx(ArrowLeft, { size: 16 }),
      " Volver al cat\xE1logo"
    ] }),
    /* @__PURE__ */ jsxs("div", { className: "breadcrumbs", children: [
      "Inicio ",
      /* @__PURE__ */ jsx(ChevronRight, {}),
      " Cat\xE1logo ",
      /* @__PURE__ */ jsx(ChevronRight, {}),
      " ",
      product.category,
      " ",
      /* @__PURE__ */ jsx(ChevronRight, {}),
      " ",
      /* @__PURE__ */ jsx("b", { children: product.name })
    ] }),
    /* @__PURE__ */ jsxs("div", { className: "detail-layout", children: [
      /* @__PURE__ */ jsxs("div", { className: "detail-gallery", children: [
        /* @__PURE__ */ jsxs("div", { className: "detail-image", style: { background: product.color }, children: [
          /* @__PURE__ */ jsxs("span", { className: "detail-badge", children: [
            /* @__PURE__ */ jsx(BadgeCheck, { size: 14 }),
            " Precio mayorista"
          ] }),
          /* @__PURE__ */ jsx("img", { src: product.image, alt: product.name })
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "detail-thumbs", children: [
          /* @__PURE__ */ jsx("button", { className: "thumb-active", children: /* @__PURE__ */ jsx("img", { src: product.image, alt: "Vista principal" }) }),
          /* @__PURE__ */ jsx("button", { children: /* @__PURE__ */ jsx(Package, {}) }),
          /* @__PURE__ */ jsx("button", { children: /* @__PURE__ */ jsx(ShoppingBag, {}) })
        ] })
      ] }),
      /* @__PURE__ */ jsxs("section", { className: "detail-info", children: [
        /* @__PURE__ */ jsxs("div", { className: "detail-brand", children: [
          product.brand.toUpperCase(),
          " ",
          /* @__PURE__ */ jsx("span", { children: "\xB7" }),
          " ",
          product.category.toUpperCase()
        ] }),
        /* @__PURE__ */ jsx("h1", { children: product.name }),
        /* @__PURE__ */ jsx("p", { className: "detail-subtitle", children: product.pack }),
        /* @__PURE__ */ jsxs("div", { className: "detail-rating", children: [
          /* @__PURE__ */ jsx("span", { children: "\u2605\u2605\u2605\u2605\u2605" }),
          " ",
          /* @__PURE__ */ jsx("b", { children: "4.8" }),
          " ",
          /* @__PURE__ */ jsx("small", { children: "(32 rese\xF1as de comercios)" })
        ] }),
        /* @__PURE__ */ jsx("div", { className: "detail-divider" }),
        /* @__PURE__ */ jsxs("div", { className: "tier-heading", children: [
          /* @__PURE__ */ jsx("b", { children: "Presentaci\xF3n de venta" }),
          /* @__PURE__ */ jsx("span", { children: "Mayor volumen = mayor margen" })
        ] }),
        /* @__PURE__ */ jsx("div", { className: "tier-grid", children: tiers.map((option, index) => /* @__PURE__ */ jsxs("button", { className: tier === index ? "tier-option selected" : "tier-option", onClick: () => setTier(index), children: [
          /* @__PURE__ */ jsx("span", { className: "tier-radio", children: tier === index && /* @__PURE__ */ jsx("i", {}) }),
          /* @__PURE__ */ jsxs("span", { className: "tier-name", children: [
            option.name,
            option.saving && /* @__PURE__ */ jsx("small", { children: option.saving }),
            /* @__PURE__ */ jsx("em", { children: option.detail })
          ] }),
          /* @__PURE__ */ jsx("strong", { children: money.format(option.price) })
        ] }, option.name)) }),
        /* @__PURE__ */ jsxs("div", { className: "quantity-total", children: [
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("span", { children: "Cantidad:" }),
            /* @__PURE__ */ jsxs("div", { className: "quantity-stepper", children: [
              /* @__PURE__ */ jsx("button", { "aria-label": "Restar", onClick: () => setQuantity(Math.max(1, quantity - 1)), children: "\u2212" }),
              /* @__PURE__ */ jsx("b", { children: quantity }),
              /* @__PURE__ */ jsx("button", { "aria-label": "Sumar", onClick: () => setQuantity(quantity + 1), children: "+" })
            ] }),
            /* @__PURE__ */ jsx("small", { children: "bultos m\xE1ster" })
          ] }),
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("span", { children: "Total estimado neto:" }),
            /* @__PURE__ */ jsx("strong", { children: money.format(total) })
          ] })
        ] }),
        /* @__PURE__ */ jsxs("button", { className: "detail-add", onClick: () => onAdd(quantity), children: [
          /* @__PURE__ */ jsx(ShoppingBag, { size: 18 }),
          " Agregar al Presupuesto Mayorista"
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "shipping-points", children: [
          /* @__PURE__ */ jsxs("span", { children: [
            /* @__PURE__ */ jsx(Store, {}),
            " Retiro sin cargo hoy"
          ] }),
          /* @__PURE__ */ jsxs("span", { children: [
            /* @__PURE__ */ jsx(Truck, {}),
            " Flete 24h bonificado"
          ] }),
          /* @__PURE__ */ jsxs("span", { children: [
            /* @__PURE__ */ jsx(ShieldCheck, {}),
            " Factura A y B"
          ] })
        ] })
      ] })
    ] }),
    /* @__PURE__ */ jsxs("section", { className: "detail-more", children: [
      /* @__PURE__ */ jsx("h2", { children: "Productos que tambi\xE9n te pueden interesar" }),
      /* @__PURE__ */ jsx("div", { children: ["Alfajor Triple Chocolate", "Mogul Dientes Gomitas", "Chocolate Block 38g", "Beldent Menta Infinito"].map((name) => /* @__PURE__ */ jsxs("span", { children: [
        /* @__PURE__ */ jsx(Check, { size: 14 }),
        " ",
        name
      ] }, name)) })
    ] })
  ] });
}
