import { Fragment, jsx, jsxs } from "react/jsx-runtime";
import { Branches } from "../components/Branches";
import { CategoryBar } from "../components/CategoryBar";
import { Footer } from "../components/Footer";
import { Hero } from "../components/Hero";
import { ProductCard } from "../components/ProductCard";
import { SearchBar } from "../components/Header";
export function HomePage({ filtered, categories, catalogCount, catalogLoading, apiNotice, category, search, onCategory, onSearch, quantities, onAdd, onDetails }) {
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
          /* @__PURE__ */ jsx(SearchBar, { value: search, onChange: onSearch })
        ] }),
        /* @__PURE__ */ jsx(CategoryBar, { active: category, onChange: onCategory, categories }),
        !catalogLoading && filtered.length > 0 && /* @__PURE__ */ jsx("div", { className: "product-grid", children: filtered.map((product) => /* @__PURE__ */ jsx(ProductCard, { product, quantity: quantities[String(product.id)] || 0, onAdd: () => onAdd(product.id), onDetails: () => onDetails(product.id) }, product.id)) }),
        catalogLoading && /* @__PURE__ */ jsx("div", { className: "no-results", role: "status", children: "Consultando el cat\xE1logo..." }),
        !catalogLoading && !filtered.length && /* @__PURE__ */ jsx("div", { className: "no-results", children: "No hay productos disponibles para esta b\xFAsqueda o categor\xEDa." }),
        /* @__PURE__ */ jsxs("div", { className: "catalog-bottom", children: [
          /* @__PURE__ */ jsxs("span", { children: [
            " ",
            /* @__PURE__ */ jsx("b", { children: catalogCount }),
            catalogCount === 1 ? " producto disponible" : " productos disponibles"
          ] })
        ] })
      ] }),
      /* @__PURE__ */ jsx(Branches, {})
    ] }),
    /* @__PURE__ */ jsx(Footer, {})
  ] });
}
