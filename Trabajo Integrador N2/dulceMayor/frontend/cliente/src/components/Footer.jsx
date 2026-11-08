import { jsx, jsxs } from "react/jsx-runtime";
import { Instagram, MapPin, Phone } from "lucide-react";
import { Link } from "react-router-dom";
export function Footer() {
  return /* @__PURE__ */ jsxs("footer", { className: "site-footer", id: "ayuda", children: [
    /* @__PURE__ */ jsxs("div", { className: "footer-main", children: [
      /* @__PURE__ */ jsxs(Link, { className: "brand footer-brand", to: "/#inicio", children: [
        /* @__PURE__ */ jsx("span", { className: "brand-mark", children: "DM" }),
        /* @__PURE__ */ jsxs("span", { className: "brand-copy", children: [
          /* @__PURE__ */ jsx("strong", { children: "Dulce Mayor" }),
          /* @__PURE__ */ jsx("small", { children: "MAYORISTA B2B \xB7 MENDOZA" })
        ] })
      ] }),
      /* @__PURE__ */ jsxs("p", { children: [
        "El socio mayorista de los kioscos",
        /* @__PURE__ */ jsx("br", {}),
        "y comercios de Mendoza."
      ] }),
      /* @__PURE__ */ jsxs("div", { className: "footer-contact", children: [
        /* @__PURE__ */ jsxs("a", { href: "tel:+542614250800", children: [
          /* @__PURE__ */ jsx(Phone, { size: 16 }),
          " (0261) 425-0800"
        ] }),
        /* @__PURE__ */ jsxs("span", { children: [
          /* @__PURE__ */ jsx(MapPin, { size: 16 }),
          " Mendoza, Argentina"
        ] })
      ] }),
      /* @__PURE__ */ jsx("a", { className: "social", href: "#ayuda", "aria-label": "Instagram", children: /* @__PURE__ */ jsx(Instagram, { size: 19 }) })
    ] }),
    /* @__PURE__ */ jsxs("div", { className: "footer-bottom", children: [
      /* @__PURE__ */ jsx("span", { children: "\xA9 2025 Dulce Mayor. Todos los derechos reservados." }),
      /* @__PURE__ */ jsxs("span", { children: [
        "Hecho en Mendoza, con dulzura. ",
        /* @__PURE__ */ jsx("b", { children: "\u2665" })
      ] })
    ] })
  ] });
}
