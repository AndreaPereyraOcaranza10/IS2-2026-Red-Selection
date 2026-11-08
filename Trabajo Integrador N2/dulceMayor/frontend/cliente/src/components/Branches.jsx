import { jsx, jsxs } from "react/jsx-runtime";
import { useEffect, useState } from "react";
import { ImagePlus, LoaderCircle, MapPin } from "lucide-react";
import { api } from "../api";
export function Branches() {
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    api.publicBranches().then(setBranches).catch(() => setBranches([])).finally(() => setLoading(false));
  }, []);
  return /* @__PURE__ */ jsxs("section", { className: "branches-section", id: "sucursales", children: [
    /* @__PURE__ */ jsx("div", { className: "section-heading branch-heading", children: /* @__PURE__ */ jsxs("div", { children: [
      /* @__PURE__ */ jsx("span", { className: "section-kicker", children: "CERCA DE TU NEGOCIO" }),
      /* @__PURE__ */ jsx("h2", { children: "Nuestras sucursales." })
    ] }) }),
    loading ? /* @__PURE__ */ jsxs("div", { className: "branch-loading", children: [
      /* @__PURE__ */ jsx(LoaderCircle, { className: "spin" }),
      " Cargando sucursales..."
    ] }) : branches.length ? /* @__PURE__ */ jsx("div", { className: "public-branch-grid", children: branches.map((branch) => /* @__PURE__ */ jsxs("article", { className: "public-branch-card", children: [
      /* @__PURE__ */ jsxs("div", { className: "branch-photo-slot", "aria-label": "Espacio reservado para la foto de la sucursal", children: [
        /* @__PURE__ */ jsx(ImagePlus, { size: 28 }),
        /* @__PURE__ */ jsx("strong", { children: "Foto de la sucursal" }),
        /* @__PURE__ */ jsx("span", { children: "Espacio reservado" })
      ] }),
      /* @__PURE__ */ jsxs("div", { className: "public-branch-copy", children: [
        /* @__PURE__ */ jsx("h3", { children: branch.razonSocial }),
        /* @__PURE__ */ jsxs("p", { children: [
          /* @__PURE__ */ jsx(MapPin, { size: 15 }),
          " ",
          branch.calle,
          " ",
          branch.numeracion,
          branch.barrio ? `, ${branch.barrio}` : ""
        ] }),
        /* @__PURE__ */ jsxs("small", { children: [
          branch.localidad,
          ", ",
          branch.departamento,
          ", ",
          branch.provincia
        ] }),
        branch.email && /* @__PURE__ */ jsx("a", { href: `mailto:${branch.email}`, children: branch.email })
      ] })
    ] }, branch.id)) }) : /* @__PURE__ */ jsxs("article", { className: "branch-placeholder", children: [
      /* @__PURE__ */ jsxs("div", { className: "branch-photo-slot", "aria-label": "Espacio reservado para la foto de la sucursal", children: [
        /* @__PURE__ */ jsx(ImagePlus, { size: 30 }),
        /* @__PURE__ */ jsx("strong", { children: "Foto de la sucursal" }),
        /* @__PURE__ */ jsx("span", { children: "Espacio reservado" })
      ] }),
      /* @__PURE__ */ jsxs("div", { className: "branch-placeholder-copy", children: [
        /* @__PURE__ */ jsxs("span", { className: "branch-pending", children: [
          /* @__PURE__ */ jsx(MapPin, { size: 14 }),
          " Datos pendientes"
        ] }),
        /* @__PURE__ */ jsx("h3", { children: "Proximamente" }),
        /* @__PURE__ */ jsx("p", { children: "Publicaremos las sucursales cuando se registren sus direcciones y horarios." })
      ] })
    ] })
  ] });
}
