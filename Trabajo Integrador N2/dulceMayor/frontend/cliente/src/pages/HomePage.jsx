import { Fragment, jsx, jsxs } from "react/jsx-runtime";
import { ArrowDown, ArrowUpDown } from "lucide-react";
import { Branches } from "../components/Branches";
import { CategoryBar } from "../components/CategoryBar";
import { Footer } from "../components/Footer";
import { Hero } from "../components/Hero";
import { ProductCard } from "../components/ProductCard";
import { SearchBar } from "../components/Header";
export function HomePage({ filtered, catalogCount, apiNotice, category, search, onCategory, onSearch, quantities, onAdd, onDetails }) {
  return /* @__PURE__ */ jsxs(Fragment, { children: [
    /* @__PURE__ */ jsxs("main", { children: [
      /* @__PURE__ */ jsx(Hero, {}),
      /* @__PURE__ */ jsxs("section", { className: "catalog-section", id: "catalogo", children: [
        /* @__PURE__ */ jsxs("div", { className: "section-heading catalog-heading", children: [
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("span", { className: "section-kicker", children: "LO QUE TU NEGOCIO NECESITA" }),
            /* @__PURE__ */ jsx("h2", { children: "El cat\xE1logo." })
          ] }),
          /* @__PURE__ */ jsxs("span", { className: "catalog-note", children: [
            /* @__PURE__ */ jsx("i", {}),
            " Precios exclusivos mayoristas"
          ] })
        ] }),
        apiNotice && /* @__PURE__ */ jsx("p", { className: "catalog-notice", role: "status", children: apiNotice }),
        /* @__PURE__ */ jsxs("div", { className: "catalog-tools", children: [
          /* @__PURE__ */ jsx(SearchBar, { value: search, onChange: onSearch }),
          /* @__PURE__ */ jsxs("button", { className: "sort-button", children: [
            /* @__PURE__ */ jsx(ArrowUpDown, { size: 16 }),
            " M\xE1s vendidos ",
            /* @__PURE__ */ jsx(ArrowDown, { size: 14 })
          ] })
        ] }),
        /* @__PURE__ */ jsx(CategoryBar, { active: category, onChange: onCategory }),
        /* @__PURE__ */ jsx("div", { className: "product-grid", children: filtered.map((product) => /* @__PURE__ */ jsx(ProductCard, { product, quantity: quantities[String(product.id)] || 0, onAdd: () => onAdd(product.id), onDetails: () => onDetails(product.id) }, product.id)) }),
        !filtered.length && /* @__PURE__ */ jsx("div", { className: "no-results", children: "No hay productos disponibles para esta b\xFAsqueda o categor\xEDa." }),
        /* @__PURE__ */ jsxs("div", { className: "catalog-bottom", children: [
          /* @__PURE__ */ jsxs("span", { children: [
            "Mostrando ",
            /* @__PURE__ */ jsx("b", { children: catalogCount }),
            " de ",
            /* @__PURE__ */ jsx("b", { children: "2.000+" }),
            " productos"
          ] }),
          /* @__PURE__ */ jsxs("button", { children: [
            "Ver cat\xE1logo completo ",
            /* @__PURE__ */ jsx(ArrowDown, { size: 16 })
          ] })
        ] })
      ] }),
      /* @__PURE__ */ jsx(Branches, {})
    ] }),
    /* @__PURE__ */ jsx(Footer, {})
  ] });
}
