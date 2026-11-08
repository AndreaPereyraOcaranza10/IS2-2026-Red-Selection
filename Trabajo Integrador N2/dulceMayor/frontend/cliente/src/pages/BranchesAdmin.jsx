import { Fragment, jsx, jsxs } from "react/jsx-runtime";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Building2, ImagePlus, LoaderCircle, MapPin, Pencil, Plus, Search, Trash2, X } from "lucide-react";
import { api } from "../api";
const blankForm = () => ({ razonSocial: "", cuit: "", calle: "", numeracion: "", barrio: "", manzanaPiso: "", casaDepartamento: "", referencia: "", localidadId: "", provinciaId: "", departamentoId: "", email: "" });
export function BranchesAdmin() {
  const [branches, setBranches] = useState([]);
  const [provinces, setProvinces] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [localities, setLocalities] = useState([]);
  const [provinceId, setProvinceId] = useState("");
  const [departmentId, setDepartmentId] = useState("");
  const [form, setForm] = useState(blankForm());
  const [photoFiles, setPhotoFiles] = useState([]);
  const [editing, setEditing] = useState(null);
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const loadBranches = useCallback(async () => {
    setLoading(true);
    try {
      setBranches(await api.branches());
      setError("");
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudieron cargar las sucursales.");
    } finally {
      setLoading(false);
    }
  }, []);
  useEffect(() => {
    void loadBranches();
    api.provinces().then(setProvinces).catch((cause) => setError(cause instanceof Error ? cause.message : "No se pudieron cargar las provincias."));
  }, [loadBranches]);
  const visible = useMemo(() => branches.filter((branch) => `${branch.razonSocial} ${branch.cuit} ${branch.localidad} ${branch.departamento} ${branch.provincia}`.toLowerCase().includes(query.toLowerCase())), [branches, query]);
  const existingPhotoCount = editing ? branches.find((branch) => branch.id === editing)?.photos?.length || 0 : 0;
  const remainingPhotoSlots = Math.max(0, 5 - existingPhotoCount);
  const newBranch = () => {
    setEditing(null);
    setForm(blankForm());
    setPhotoFiles([]);
    setProvinceId("");
    setDepartmentId("");
    setDepartments([]);
    setLocalities([]);
    setError("");
    setOpen(true);
  };
  const editBranch = async (branch) => {
    setError("");
    setEditing(branch.id);
    setForm({ razonSocial: branch.razonSocial, cuit: branch.cuit, calle: branch.calle, numeracion: branch.numeracion, barrio: branch.barrio || "", manzanaPiso: branch.manzanaPiso || "", casaDepartamento: branch.casaDepartamento || "", referencia: branch.referencia || "", localidadId: branch.localidadId, provinciaId: branch.provinciaId, departamentoId: branch.departamentoId, email: branch.email || "" });
    setPhotoFiles([]);
    setOpen(true);
    const province = provinces.find((item) => item.name === branch.provincia);
    if (!province) return;
    setProvinceId(province.id);
    const deps = await api.departments(province.id).catch(() => []);
    setDepartments(deps);
    const department = deps.find((item) => item.name === branch.departamento);
    if (!department) return;
    setDepartmentId(department.id);
    const locs = await api.localities(department.id).catch(() => []);
    setLocalities(locs);
  };
  const changeProvince = async (id) => {
    setProvinceId(id);
    setDepartmentId("");
    setDepartments([]);
    setLocalities([]);
    setForm((value) => ({ ...value, provinciaId: id, departamentoId: "", localidadId: "" }));
    if (id) setDepartments(await api.departments(id).catch((cause) => {
      setError(cause instanceof Error ? cause.message : "No se pudieron cargar los departamentos.");
      return [];
    }));
  };
  const changeDepartment = async (id) => {
    setDepartmentId(id);
    setLocalities([]);
    setForm((value) => ({ ...value, departamentoId: id, localidadId: "" }));
    if (id) setLocalities(await api.localities(id).catch((cause) => {
      setError(cause instanceof Error ? cause.message : "No se pudieron cargar las localidades.");
      return [];
    }));
  };
  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError("");
    try {
      const saved = editing ? await api.updateBranch(editing, form) : await api.createBranch(form);
      let photoError = "";
      for (const file of photoFiles) {
        try {
          await api.uploadBranchPhoto(saved.id, file);
        } catch (cause) {
          photoError = cause instanceof Error ? cause.message : "No se pudieron cargar todas las fotos.";
          break;
        }
      }
      setNotice(photoError
        ? `Sucursal ${saved.razonSocial} guardada, pero no se pudieron cargar todas las fotos: ${photoError}`
        : editing ? `Sucursal ${saved.razonSocial} actualizada.` : `Sucursal ${saved.razonSocial} creada.`);
      setPhotoFiles([]);
      setOpen(false);
      await loadBranches();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo guardar la sucursal.");
    } finally {
      setSaving(false);
    }
  };
  const remove = async (branch) => {
    if (!window.confirm(`Dar de baja la sucursal ${branch.razonSocial}?`)) return;
    try {
      await api.deleteBranch(branch.id);
      setNotice(`Sucursal ${branch.razonSocial} dada de baja.`);
      await loadBranches();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo dar de baja la sucursal.");
    }
  };
  const set = (key, value) => setForm((current) => ({ ...current, [key]: value }));
  return /* @__PURE__ */ jsxs(Fragment, { children: [
    /* @__PURE__ */ jsxs("section", { className: "orders-table-card branch-admin-card", children: [
      /* @__PURE__ */ jsxs("header", { className: "table-title", children: [
        /* @__PURE__ */ jsxs("div", { children: [
          /* @__PURE__ */ jsx("h2", { children: "Sucursales registradas" }),
          /* @__PURE__ */ jsx("p", { children: "Empresas de tipo sucursal segun el modelo Empresa" })
        ] }),
        /* @__PURE__ */ jsxs("b", { children: [
          branches.length,
          " sucursales"
        ] })
      ] }),
      /* @__PURE__ */ jsxs("div", { className: "table-toolbar", children: [
        /* @__PURE__ */ jsxs("label", { children: [
          /* @__PURE__ */ jsx(Search, {}),
          /* @__PURE__ */ jsx("input", { value: query, onChange: (event) => setQuery(event.target.value), placeholder: "Buscar por nombre, CUIT o localidad..." })
        ] }),
        /* @__PURE__ */ jsxs("button", { className: "ops-primary", onClick: newBranch, children: [
          /* @__PURE__ */ jsx(Plus, {}),
          " Nueva sucursal"
        ] })
      ] }),
      notice && /* @__PURE__ */ jsxs("div", { className: "ops-notice", children: [
        /* @__PURE__ */ jsx(Building2, {}),
        notice,
        /* @__PURE__ */ jsx("button", { onClick: () => setNotice(""), "aria-label": "Cerrar", children: /* @__PURE__ */ jsx(X, {}) })
      ] }),
      error && !open && /* @__PURE__ */ jsxs("div", { className: "ops-notice ops-notice-error", role: "alert", children: [
        error,
        /* @__PURE__ */ jsx("button", { onClick: () => void loadBranches(), children: "Reintentar" })
      ] }),
      loading ? /* @__PURE__ */ jsxs("div", { className: "ops-loading", children: [
        /* @__PURE__ */ jsx(LoaderCircle, { className: "spin" }),
        " Cargando sucursales..."
      ] }) : visible.length === 0 ? /* @__PURE__ */ jsx("div", { className: "ops-empty", children: branches.length ? "No hay sucursales que coincidan con la busqueda." : "Todavia no hay sucursales registradas." }) : /* @__PURE__ */ jsx("div", { className: "branch-admin-list", children: visible.map((branch) => /* @__PURE__ */ jsxs("article", { className: "branch-admin-item", children: [
        /* @__PURE__ */ jsx("span", { className: "branch-admin-icon", children: /* @__PURE__ */ jsx(Building2, {}) }),
        /* @__PURE__ */ jsxs("div", { className: "branch-admin-details", children: [
          /* @__PURE__ */ jsx("h3", { children: branch.razonSocial }),
          /* @__PURE__ */ jsxs("p", { children: [
            /* @__PURE__ */ jsx(MapPin, {}),
            " ",
            branch.calle,
            " ",
            branch.numeracion,
            branch.barrio ? `, ${branch.barrio}` : "",
            " \xB7 ",
            branch.localidad,
            ", ",
            branch.departamento,
            ", ",
            branch.provincia
          ] }),
          /* @__PURE__ */ jsxs("small", { children: [
            "CUIT ",
            branch.cuit,
            " \xB7 ",
            branch.email
          ] })
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "branch-admin-actions", children: [
          /* @__PURE__ */ jsxs("button", { onClick: () => void editBranch(branch), "aria-label": `Editar ${branch.razonSocial}`, children: [
            /* @__PURE__ */ jsx(Pencil, {}),
            " Editar"
          ] }),
          /* @__PURE__ */ jsxs("button", { className: "danger", onClick: () => void remove(branch), "aria-label": `Dar de baja ${branch.razonSocial}`, children: [
            /* @__PURE__ */ jsx(Trash2, {}),
            " Baja"
          ] })
        ] })
      ] }, branch.id)) })
    ] }),
    open && /* @__PURE__ */ jsx("div", { className: "manual-modal-backdrop", onMouseDown: (event) => {
      if (event.target === event.currentTarget) setOpen(false);
    }, children: /* @__PURE__ */ jsxs("section", { className: "manual-modal branch-form-modal", role: "dialog", "aria-modal": "true", "aria-labelledby": "branch-form-title", children: [
      /* @__PURE__ */ jsxs("header", { children: [
        /* @__PURE__ */ jsxs("div", { children: [
          /* @__PURE__ */ jsx("span", { className: "section-kicker", children: "EMPRESA \xB7 TIPO SUCURSAL" }),
          /* @__PURE__ */ jsx("h2", { id: "branch-form-title", children: editing ? "Editar sucursal" : "Nueva sucursal" })
        ] }),
        /* @__PURE__ */ jsx("button", { onClick: () => setOpen(false), "aria-label": "Cerrar", children: /* @__PURE__ */ jsx(X, {}) })
      ] }),
      /* @__PURE__ */ jsxs("form", { onSubmit: submit, children: [
        /* @__PURE__ */ jsxs("div", { className: "branch-form-grid", children: [
          /* @__PURE__ */ jsxs("label", { children: [
            "Nombre / razon social",
            /* @__PURE__ */ jsx("input", { required: true, value: form.razonSocial, onChange: (event) => set("razonSocial", event.target.value) })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "CUIT",
            /* @__PURE__ */ jsx("input", { required: true, inputMode: "numeric", value: form.cuit, onChange: (event) => set("cuit", event.target.value), placeholder: "30-71234567-8" })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Email de contacto",
            /* @__PURE__ */ jsx("input", { required: true, type: "email", value: form.email, onChange: (event) => set("email", event.target.value), placeholder: "sucursal@dulcemayor.com" })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Calle",
            /* @__PURE__ */ jsx("input", { required: true, value: form.calle, onChange: (event) => set("calle", event.target.value) })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Numeracion",
            /* @__PURE__ */ jsx("input", { required: true, value: form.numeracion, onChange: (event) => set("numeracion", event.target.value) })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Barrio",
            /* @__PURE__ */ jsx("input", { value: form.barrio || "", onChange: (event) => set("barrio", event.target.value) })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Provincia",
            /* @__PURE__ */ jsxs("select", { required: true, value: provinceId, onChange: (event) => void changeProvince(event.target.value), children: [
              /* @__PURE__ */ jsx("option", { value: "", children: "Seleccionar provincia" }),
              provinces.map((item) => /* @__PURE__ */ jsx("option", { value: item.id, children: item.name }, item.id))
            ] })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Departamento",
            /* @__PURE__ */ jsxs("select", { required: true, value: departmentId, disabled: !provinceId, onChange: (event) => void changeDepartment(event.target.value), children: [
              /* @__PURE__ */ jsx("option", { value: "", children: "Seleccionar departamento" }),
              departments.map((item) => /* @__PURE__ */ jsx("option", { value: item.id, children: item.name }, item.id))
            ] })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Localidad",
            /* @__PURE__ */ jsxs("select", { required: true, value: form.localidadId, disabled: !departmentId, onChange: (event) => set("localidadId", event.target.value), children: [
              /* @__PURE__ */ jsx("option", { value: "", children: "Seleccionar localidad" }),
              localities.map((item) => /* @__PURE__ */ jsx("option", { value: item.id, children: item.name }, item.id))
            ] })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Manzana / piso",
            /* @__PURE__ */ jsx("input", { value: form.manzanaPiso || "", onChange: (event) => set("manzanaPiso", event.target.value) })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Casa / departamento",
            /* @__PURE__ */ jsx("input", { value: form.casaDepartamento || "", onChange: (event) => set("casaDepartamento", event.target.value) })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Referencia",
            /* @__PURE__ */ jsx("input", { value: form.referencia || "", onChange: (event) => set("referencia", event.target.value) })
          ] })
        ] }),
        /* @__PURE__ */ jsxs("label", { className: "branch-photo-upload", children: [
          /* @__PURE__ */ jsxs("span", { children: [/* @__PURE__ */ jsx(ImagePlus, { size: 16 }), " Fotos de la sucursal"] }),
          /* @__PURE__ */ jsx("input", { type: "file", accept: "image/png,image/jpeg,image/webp", multiple: true, disabled: remainingPhotoSlots === 0, onChange: (event) => setPhotoFiles(Array.from(event.target.files || []).slice(0, remainingPhotoSlots)) }),
          /* @__PURE__ */ jsx("small", { children: `Podés agregar hasta ${remainingPhotoSlots} foto${remainingPhotoSlots === 1 ? "" : "s"}. PNG, JPG o WEBP; máximo 2 MB cada una.` }),
          photoFiles.length > 0 && /* @__PURE__ */ jsx("small", { children: `${photoFiles.length} foto${photoFiles.length === 1 ? " seleccionada" : "s seleccionadas"}` })
        ] }),
        error && /* @__PURE__ */ jsx("p", { className: "manual-error", role: "alert", children: error }),
        /* @__PURE__ */ jsxs("footer", { children: [
          /* @__PURE__ */ jsx("button", { type: "button", className: "ops-secondary", onClick: () => setOpen(false), children: "Cancelar" }),
          /* @__PURE__ */ jsxs("button", { className: "ops-primary", disabled: saving, children: [
            saving ? /* @__PURE__ */ jsx(LoaderCircle, { className: "spin" }) : /* @__PURE__ */ jsx(Plus, {}),
            saving ? "Guardando..." : editing ? "Guardar cambios" : "Crear sucursal"
          ] })
        ] })
      ] })
    ] }) })
  ] });
}
