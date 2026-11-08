import { jsx, jsxs } from "react/jsx-runtime";
import { useEffect, useState } from "react";
import { LoaderCircle, MapPin } from "lucide-react";
import { api } from "../api";

export function Branches() {
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    api.publicBranches()
      .then((items) => setBranches(items))
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  return /* @__PURE__ */ jsxs("section", { className: "branches-section", id: "sucursales", children: [
    /* @__PURE__ */ jsx("div", { className: "section-heading branch-heading", children: /* @__PURE__ */ jsxs("div", { children: [
      /* @__PURE__ */ jsx("span", { className: "section-kicker", children: "CERCA DE TU NEGOCIO" }),
      /* @__PURE__ */ jsx("h2", { children: "Nuestras sucursales." })
    ] }) }),
    loading ? /* @__PURE__ */ jsxs("div", { className: "branch-loading", children: [
      /* @__PURE__ */ jsx(LoaderCircle, { className: "spin" }),
      " Cargando sucursales..."
    ] }) : error ? /* @__PURE__ */ jsx("p", { className: "branch-loading", role: "alert", children: "No se pudieron cargar las sucursales. Intent\xE1 actualizar la p\xE1gina." }) : branches.length ? /* @__PURE__ */ jsx("div", { className: "public-branch-grid", children: branches.map((branch) => {
      const address = [branch.calle, branch.numeracion, branch.barrio, branch.localidad, branch.departamento, branch.provincia].filter(Boolean).join(", ");
      const mapQuery = encodeURIComponent(address);
      return /* @__PURE__ */ jsxs("article", { className: "public-branch-card", children: [
        branch.photos?.length ? /* @__PURE__ */ jsx("div", { className: "branch-photos", children: branch.photos.map((photo, index) => /* @__PURE__ */ jsx("img", { src: photo, alt: `${branch.razonSocial}, foto ${index + 1}`, loading: "lazy" }, photo)) }) : /* @__PURE__ */ jsx("div", { className: "branch-photo-slot", children: "Fotograf\xEDa de la sucursal no disponible" }),
        /* @__PURE__ */ jsxs("div", { className: "public-branch-copy", children: [
          /* @__PURE__ */ jsx("h3", { children: branch.razonSocial }),
          /* @__PURE__ */ jsxs("p", { children: [
            /* @__PURE__ */ jsx(MapPin, { size: 15 }),
            " ",
            address
          ] }),
          branch.email && /* @__PURE__ */ jsx("a", { href: `mailto:${branch.email}`, children: branch.email }),
          /* @__PURE__ */ jsx("iframe", { className: "public-branch-map", title: `Mapa de ${branch.razonSocial}`, src: `https://maps.google.com/maps?q=${mapQuery}&output=embed`, loading: "lazy", referrerPolicy: "no-referrer-when-downgrade" }),
          /* @__PURE__ */ jsx("a", { className: "branch-directions", href: `https://www.google.com/maps/search/?api=1&query=${mapQuery}`, target: "_blank", rel: "noreferrer", children: "Cómo llegar" })
        ] })
      ] }, branch.id);
    }) }) : /* @__PURE__ */ jsx("p", { className: "branch-loading", children: "Todav\xEDa no hay sucursales registradas." })
  ] });
}
