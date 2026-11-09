import { jsx, jsxs } from "react/jsx-runtime";
import { ArrowRight, BadgePercent, PackageCheck, Truck } from "lucide-react";
import { Link } from "react-router-dom";
export function Hero() {
  return /* @__PURE__ */ jsxs("section", { className: "hero", id: "inicio", children: [
    /* @__PURE__ */ jsxs("div", { className: "hero-copy", children: [
      /* @__PURE__ */ jsxs("div", { className: "eyebrow", children: [
        /* @__PURE__ */ jsx("span", {}),
        " EL MAYORISTA DE MENDOZA"
      ] }),
      /* @__PURE__ */ jsxs("h1", { children: [
        "Tu kiosco, siempre",
        /* @__PURE__ */ jsx("br", {}),
        "bien ",
        /* @__PURE__ */ jsx("em", { children: "abastecido." })
      ] }),
      /* @__PURE__ */ jsx("p", { children: "Todo lo que necesit\xE1s para tu negocio, en un solo lugar. Precios mayoristas, entrega en tu zona y un pedido que se arma en minutos." }),
      /* @__PURE__ */ jsxs(Link, { className: "hero-cta", to: "/#catalogo", children: [
        "Explorar cat\xE1logo ",
        /* @__PURE__ */ jsx(ArrowRight, { size: 17 })
      ] }),
      /* @__PURE__ */ jsxs("div", { className: "hero-facts", children: [
        /* @__PURE__ */ jsxs("div", { children: [
          /* @__PURE__ */ jsx(PackageCheck, {}),
          /* @__PURE__ */ jsxs("span", { children: [
            /* @__PURE__ */ jsx("b", { children: "Cat\xE1logo" }),
            /* @__PURE__ */ jsx("small", { children: "de productos" })
          ] })
        ] }),
        /* @__PURE__ */ jsxs("div", { children: [
          /* @__PURE__ */ jsx(Truck, {}),
          /* @__PURE__ */ jsxs("span", { children: [
            /* @__PURE__ */ jsx("b", { children: "Entregas" }),
            /* @__PURE__ */ jsx("small", { children: "coordinadas por zona" })
          ] })
        ] }),
        /* @__PURE__ */ jsxs("div", { children: [
          /* @__PURE__ */ jsx(BadgePercent, {}),
          /* @__PURE__ */ jsxs("span", { children: [
            /* @__PURE__ */ jsx("b", { children: "Precio mayorista" }),
            /* @__PURE__ */ jsx("small", { children: "desde la primera caja" })
          ] })
        ] })
      ] })
    ] }),
    /* @__PURE__ */ jsxs("div", { className: "hero-art", children: [
      /* @__PURE__ */ jsx("div", { className: "hero-orbit orbit-one" }),
      /* @__PURE__ */ jsx("div", { className: "hero-orbit orbit-two" }),
      /* @__PURE__ */ jsx("div", { className: "hero-spark spark-one", children: "\u2733" }),
      /* @__PURE__ */ jsx("div", { className: "hero-spark spark-two", children: "\u2726" }),
      /* @__PURE__ */ jsxs("div", { className: "hero-sticker", children: [
        /* @__PURE__ */ jsx("small", { children: "PRECIOS" }),
        /* @__PURE__ */ jsxs("b", { children: [
          "DE",
          /* @__PURE__ */ jsx("br", {}),
          "MAYOR"
        ] })
      ] }),
      /* @__PURE__ */ jsx("div", { className: "hero-choco", children: "\u{1F36B}" }),
      /* @__PURE__ */ jsx("div", { className: "hero-gummy", children: "\u{1F36C}" }),
      /* @__PURE__ */ jsxs("div", { className: "hero-pop", children: [
        "\xA1Stock",
        /* @__PURE__ */ jsx("br", {}),
        "para hoy!"
      ] }),
      /* @__PURE__ */ jsxs("div", { className: "art-caption", children: [
        "UN MUNDO DE DULZURA",
        /* @__PURE__ */ jsx("br", {}),
        /* @__PURE__ */ jsx("span", { children: "PARA TU NEGOCIO" })
      ] })
    ] })
  ] });
}
