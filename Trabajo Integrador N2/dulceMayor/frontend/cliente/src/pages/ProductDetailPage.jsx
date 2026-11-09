import { jsx, jsxs } from "react/jsx-runtime";
import { useState } from "react";
import { ArrowLeft, BadgeCheck, Minus, Plus, ShoppingBag } from "lucide-react";
import { useDollarQuote } from "../hooks/useDollarQuote";

const pesos = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 });
const dolares = new Intl.NumberFormat("es-AR", { style: "currency", currency: "USD", currencyDisplay: "code", maximumFractionDigits: 2 });

export function ProductDetailPage({ product, quantityInCart = 0, onBack, onAdd }) {
  const [quantity, setQuantity] = useState(1);
  const { quote } = useDollarQuote();
  const hasStock = product.inStock ?? Number(product.stock) > 0;
  const stockKnown = product.stock !== null && product.stock !== undefined && Number.isFinite(Number(product.stock));
  const remainingStock = stockKnown ? Math.max(0, Number(product.stock) - quantityInCart) : Number.POSITIVE_INFINITY;
  const selectedQuantity = Math.min(quantity, remainingStock);
  const canAdd = hasStock && remainingStock > 0;
  const total = product.price == null ? null : Number(product.price) * selectedQuantity;
  const priceInDollars = (price) => price == null ? "Precio no disponible" : quote?.sell > 0 ? dolares.format(Number(price) / quote.sell) : "Cotización no disponible";

  return /* @__PURE__ */ jsxs("main", { className: "detail-page", children: [
    /* @__PURE__ */ jsxs("button", { className: "back-link", onClick: onBack, children: [
      /* @__PURE__ */ jsx(ArrowLeft, { size: 16 }),
      " Volver al catálogo"
    ] }),
    /* @__PURE__ */ jsxs("div", { className: "breadcrumbs", children: [
      "Inicio  ›  Catálogo  ›  ", product.category, "  ›  ", /* @__PURE__ */ jsx("b", { children: product.name })
    ] }),
    /* @__PURE__ */ jsxs("div", { className: "detail-layout", children: [
      /* @__PURE__ */ jsx("div", { className: "detail-gallery", children: /* @__PURE__ */ jsxs("div", { className: "detail-image", style: { background: product.color }, children: [
        /* @__PURE__ */ jsxs("span", { className: "detail-badge", children: [
          /* @__PURE__ */ jsx(BadgeCheck, { size: 14 }),
          " Precio mayorista"
        ] }),
        product.image && /* @__PURE__ */ jsx("img", { src: product.image, alt: product.name })
      ] }) }),
      /* @__PURE__ */ jsxs("section", { className: "detail-info", children: [
        /* @__PURE__ */ jsxs("div", { className: "detail-brand", children: [
          product.brand || "Dulce Mayor", "  ·  ", product.category
        ] }),
        /* @__PURE__ */ jsx("h1", { children: product.name }),
        /* @__PURE__ */ jsx("p", { className: "detail-subtitle", children: product.pack }),
        product.description && /* @__PURE__ */ jsx("p", { className: "detail-description", children: product.description }),
        /* @__PURE__ */ jsx("div", { className: "detail-divider" }),
        /* @__PURE__ */ jsxs("div", { className: "detail-price", children: [
          /* @__PURE__ */ jsx("span", { children: "Precio por bulto" }),
          /* @__PURE__ */ jsx("strong", { children: product.price == null ? "Precio no disponible" : pesos.format(product.price) }),
          /* @__PURE__ */ jsx("small", { children: priceInDollars(product.price) }),
          product.unitPrice != null && /* @__PURE__ */ jsxs("small", { children: ["Precio unitario de referencia: ", pesos.format(product.unitPrice)] })
        ] }),
        /* @__PURE__ */ jsxs("p", { className: hasStock ? "detail-stock available" : "detail-stock unavailable", children: [
          hasStock ? "Disponible" : "Sin stock", typeof product.stock === "number" ? ` · ${product.stock} unidades` : ""
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "quantity-total", children: [
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("span", { children: "Bultos:" }),
            /* @__PURE__ */ jsxs("div", { className: "quantity-stepper", children: [
              /* @__PURE__ */ jsx("button", { "aria-label": "Restar", disabled: selectedQuantity <= 1, onClick: () => setQuantity(Math.max(1, selectedQuantity - 1)), children: /* @__PURE__ */ jsx(Minus, { size: 14 }) }),
              /* @__PURE__ */ jsx("b", { children: selectedQuantity }),
              /* @__PURE__ */ jsx("button", { "aria-label": "Sumar", disabled: selectedQuantity >= remainingStock, onClick: () => setQuantity(Math.min(remainingStock, selectedQuantity + 1)), children: /* @__PURE__ */ jsx(Plus, { size: 14 }) })
            ] })
          ] }),
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("span", { children: "Total estimado:" }),
            /* @__PURE__ */ jsx("strong", { children: total == null ? "Precio no disponible" : pesos.format(total) }),
            /* @__PURE__ */ jsx("small", { children: priceInDollars(total) })
          ] })
        ] }),
        remainingStock === 0 && /* @__PURE__ */ jsx("p", { className: "detail-stock unavailable", children: "Ya agregaste todo el stock disponible de este producto." }),
        /* @__PURE__ */ jsxs("button", { className: "detail-add", disabled: !canAdd, onClick: () => onAdd(selectedQuantity), children: [
          /* @__PURE__ */ jsx(ShoppingBag, { size: 18 }),
          !hasStock ? "Producto sin stock" : remainingStock === 0 ? "Stock máximo agregado" : "Agregar al presupuesto"
        ] })
      ] })
    ] })
  ] });
}
