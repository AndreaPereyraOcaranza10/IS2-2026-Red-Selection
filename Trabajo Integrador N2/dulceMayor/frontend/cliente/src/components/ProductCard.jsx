import { jsx, jsxs } from "react/jsx-runtime";
import { Check, Plus } from "lucide-react";
const currency = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 });
export function ProductCard({ product, quantity, onAdd, onDetails }) {
  const inStock = product.inStock ?? (typeof product.stock === "number" ? product.stock > 0 : product.stock === "Disponible");
  const lowStock = typeof product.stock === "number" ? product.stock <= (product.stockMinimum ?? 5) : product.stock === "\xDAltimos bultos";
  const reachedStockLimit = typeof product.stock === "number" && quantity >= product.stock;
  return /* @__PURE__ */ jsxs("article", { className: "product-card", children: [
    /* @__PURE__ */ jsxs("div", { className: "product-image", style: { backgroundColor: product.color }, children: [
      product.tag && /* @__PURE__ */ jsx("span", { className: product.tag.includes("%") ? "product-tag discount" : "product-tag", children: product.tag }),
      /* @__PURE__ */ jsx("img", { src: product.image, alt: product.name, loading: "lazy" }),
      /* @__PURE__ */ jsxs("span", { className: inStock ? lowStock ? "stock stock-low" : "stock stock-ok" : "stock stock-low", children: [
        /* @__PURE__ */ jsx("i", {}),
        !inStock ? "Sin stock" : lowStock ? "\xDAltimos bultos" : "Disponible"
      ] })
    ] }),
    /* @__PURE__ */ jsxs("div", { className: "product-info", children: [
      /* @__PURE__ */ jsxs("div", { className: "product-meta", children: [
        /* @__PURE__ */ jsx("span", { children: product.brand }),
        /* @__PURE__ */ jsx("span", { children: product.category })
      ] }),
      /* @__PURE__ */ jsx("button", { className: "product-title-button", onClick: onDetails, children: /* @__PURE__ */ jsx("h3", { children: product.name }) }),
      /* @__PURE__ */ jsx("p", { className: "pack-size", children: product.pack }),
      /* @__PURE__ */ jsxs("div", { className: "product-price", children: [
        /* @__PURE__ */ jsx("strong", { children: product.price == null ? "Precio no disponible" : currency.format(product.price) }),
        /* @__PURE__ */ jsxs("small", { children: [
          product.unitPrice == null ? "Precio unitario no disponible" : `${currency.format(product.unitPrice)} c/u`
        ] })
      ] }),
      /* @__PURE__ */ jsxs("button", { className: quantity ? "add-button added" : "add-button", disabled: reachedStockLimit, title: reachedStockLimit ? "Ya agregaste todo el stock disponible" : undefined, onClick: onAdd, children: [
        quantity ? /* @__PURE__ */ jsx(Check, { size: 17 }) : /* @__PURE__ */ jsx(Plus, { size: 17 }),
        reachedStockLimit ? `Stock máximo \xB7 ${quantity}` : quantity ? `Agregado \xB7 ${quantity}` : "Agregar al presupuesto"
      ] })
    ] })
  ] });
}
