import { jsx, jsxs } from "react/jsx-runtime";
import { useEffect, useState } from "react";
import { ArrowLeft, BadgeCheck, Check, LoaderCircle, Save, UserRound } from "lucide-react";
import { api } from "../api";
import { PasswordChangeForm } from "../components/PasswordChangeForm.jsx";
const emptyProfile = {
  nombre: "",
  apellido: "",
  sexo: null,
  fechaNacimiento: "",
  tipoDocumento: "DNI",
  numeroDocumento: "",
  email: "",
  telefono: "",
  tipoTelefono: "CELULAR",
  provincia: "",
  departamento: "",
  localidad: "",
  codigoPostal: "",
  calle: "",
  numeroCalle: "",
  manzanaPiso: "",
  casaDepartamento: "",
  referencia: "",
  provinciaId: "",
  departamentoId: "",
  localidadId: ""
};
function normalizeProfile(data) {
  const values = { ...emptyProfile, ...data };
  return Object.fromEntries(Object.entries(values).map(([key, value]) => [key, key === "sexo" ? value ?? null : value ?? ""]));
}
export function ProfilePage({ onBack, onPasswordChanged }) {
  const [profile, setProfile] = useState(emptyProfile);
  const [provinces, setProvinces] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [localities, setLocalities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState(null);
  useEffect(() => {
    const preserveCurrent = (options, name) => name && !options.some((option) => option.name.toLocaleLowerCase() === name.toLocaleLowerCase()) ? [{ id: "", name }, ...options] : options;
    api.profile().then(async (data) => {
      const loaded = normalizeProfile(data);
      setProfile(loaded);
      const provinceOptions = await api.provinces();
      setProvinces(preserveCurrent(provinceOptions, loaded.provincia));
      const province = provinceOptions.find((option) => option.name.toLocaleLowerCase() === loaded.provincia.toLocaleLowerCase());
      if (!province) return;
      const departmentOptions = await api.departments(province.id);
      setDepartments(preserveCurrent(departmentOptions, loaded.departamento));
      const department = departmentOptions.find((option) => option.name.toLocaleLowerCase() === loaded.departamento.toLocaleLowerCase());
      if (!department) return;
      const localityOptions = await api.localities(department.id);
      setLocalities(preserveCurrent(localityOptions, loaded.localidad));
    }).catch((error) => setNotice({ type: "error", text: error instanceof Error ? error.message : "No pudimos cargar tu perfil y sus opciones de domicilio." })).finally(() => setLoading(false));
  }, []);
  const update = (event) => {
    const { name, value } = event.target;
    setProfile((previous) => ({ ...previous, [name]: value }));
  };
  const changeProvince = async (event) => {
    const name = event.target.value;
    const selected = provinces.find((option) => option.name === name);
    setProfile((previous) => ({ ...previous, provincia: name, provinciaId: selected?.id || "", departamento: "", departamentoId: "", localidad: "", localidadId: "", codigoPostal: "" }));
    setDepartments([]);
    setLocalities([]);
    if (!selected?.id) return;
    try {
      setDepartments(await api.departments(selected.id));
    } catch (error) {
      setNotice({ type: "error", text: error instanceof Error ? error.message : "No pudimos cargar los departamentos." });
    }
  };
  const changeDepartment = async (event) => {
    const name = event.target.value;
    const selected = departments.find((option) => option.name === name);
    setProfile((previous) => ({ ...previous, departamento: name, departamentoId: selected?.id || "", localidad: "", localidadId: "", codigoPostal: "" }));
    setLocalities([]);
    if (!selected?.id) return;
    try {
      setLocalities(await api.localities(selected.id));
    } catch (error) {
      setNotice({ type: "error", text: error instanceof Error ? error.message : "No pudimos cargar las localidades." });
    }
  };
  const changeLocality = (event) => {
    const selected = localities.find((option) => option.name === event.target.value);
    setProfile((previous) => ({ ...previous, localidad: event.target.value, localidadId: selected?.id || "", codigoPostal: "" }));
  };
  const save = async (event) => {
    event.preventDefault();
    setSaving(true);
    setNotice(null);
    try {
      const saved = await api.updateProfile(profile);
      setProfile(normalizeProfile(saved));
      setNotice({ type: "success", text: "Tus datos se guardaron correctamente." });
    } catch (error) {
      setNotice({ type: "error", text: error instanceof Error ? error.message : "No pudimos guardar los cambios." });
    } finally {
      setSaving(false);
    }
  };
  const initials = `${profile.nombre.charAt(0)}${profile.apellido.charAt(0)}`.trim().toUpperCase() || /* @__PURE__ */ jsx(UserRound, { size: 20 });
  return /* @__PURE__ */ jsxs("main", { className: "profile-page", children: [
    /* @__PURE__ */ jsxs("button", { className: "back-link", onClick: onBack, children: [
      /* @__PURE__ */ jsx(ArrowLeft, { size: 16 }),
      " Volver al cat\xE1logo"
    ] }),
    /* @__PURE__ */ jsxs("div", { className: "profile-heading", children: [
      /* @__PURE__ */ jsxs("div", { children: [
        /* @__PURE__ */ jsx("span", { className: "section-kicker", children: "TU CUENTA MAYORISTA" }),
        /* @__PURE__ */ jsx("h1", { children: "Mi perfil" }),
        /* @__PURE__ */ jsx("p", { children: "Consult\xE1 y actualiz\xE1 tus datos personales y de contacto." })
      ] }),
      /* @__PURE__ */ jsx("div", { className: "profile-avatar", children: initials })
    ] }),
    notice && /* @__PURE__ */ jsxs("div", { className: `profile-notice ${notice.type}`, role: "status", children: [
      notice.type === "success" ? /* @__PURE__ */ jsx(Check, { size: 17 }) : /* @__PURE__ */ jsx(BadgeCheck, { size: 17 }),
      notice.text
    ] }),
    loading ? /* @__PURE__ */ jsxs("div", { className: "profile-loading", children: [
      /* @__PURE__ */ jsx(LoaderCircle, { className: "spin" }),
      " Cargando tus datos..."
    ] }) : /* @__PURE__ */ jsxs("form", { className: "profile-form", onSubmit: save, children: [
      /* @__PURE__ */ jsxs("section", { className: "profile-section", children: [
        /* @__PURE__ */ jsxs("header", { children: [
          /* @__PURE__ */ jsx("span", { children: /* @__PURE__ */ jsx(UserRound, {}) }),
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("h2", { children: "Datos personales" }),
            /* @__PURE__ */ jsx("p", { children: "Esta informaci\xF3n identifica al titular de la cuenta." })
          ] })
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "profile-fields", children: [
          /* @__PURE__ */ jsxs("label", { children: [
            "Nombre",
            /* @__PURE__ */ jsx("input", { name: "nombre", value: profile.nombre, onChange: update, required: true })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Apellido",
            /* @__PURE__ */ jsx("input", { name: "apellido", value: profile.apellido, onChange: update, required: true })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Fecha de nacimiento",
            /* @__PURE__ */ jsx("input", { name: "fechaNacimiento", type: "date", value: profile.fechaNacimiento, onChange: update, required: true })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Sexo",
            /* @__PURE__ */ jsxs("select", { name: "sexo", value: profile.sexo ?? "", onChange: update, children: [
              /* @__PURE__ */ jsx("option", { value: "", children: "Prefiero no indicarlo" }),
              /* @__PURE__ */ jsx("option", { value: "FEMENINO", children: "Femenino" }),
              /* @__PURE__ */ jsx("option", { value: "MASCULINO", children: "Masculino" }),
              /* @__PURE__ */ jsx("option", { value: "OTRO", children: "Otro" })
            ] })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Tipo de documento",
            /* @__PURE__ */ jsxs("select", { name: "tipoDocumento", value: profile.tipoDocumento, onChange: update, children: [
              /* @__PURE__ */ jsx("option", { value: "DNI", children: "DNI" }),
              /* @__PURE__ */ jsx("option", { value: "PASAPORTE", children: "Pasaporte" })
            ] })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "N\xFAmero de documento",
            /* @__PURE__ */ jsx("input", { name: "numeroDocumento", value: profile.numeroDocumento, onChange: update, required: true })
          ] })
        ] })
      ] }),
      /* @__PURE__ */ jsxs("section", { className: "profile-section", children: [
        /* @__PURE__ */ jsxs("header", { children: [
          /* @__PURE__ */ jsx("span", { children: /* @__PURE__ */ jsx(BadgeCheck, {}) }),
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("h2", { children: "Contacto" }),
            /* @__PURE__ */ jsx("p", { children: "Usamos estos datos para novedades de pedidos y facturaci\xF3n." })
          ] })
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "profile-fields", children: [
          /* @__PURE__ */ jsxs("label", { children: [
            "Correo electr\xF3nico",
            /* @__PURE__ */ jsx("input", { value: profile.email, type: "email", readOnly: true, disabled: true })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Tipo de tel\xE9fono",
            /* @__PURE__ */ jsxs("select", { name: "tipoTelefono", value: profile.tipoTelefono, onChange: update, children: [
              /* @__PURE__ */ jsx("option", { value: "CELULAR", children: "Celular" }),
              /* @__PURE__ */ jsx("option", { value: "FIJO", children: "Fijo" })
            ] })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Tel\xE9fono",
            /* @__PURE__ */ jsx("input", { name: "telefono", type: "tel", value: profile.telefono, onChange: update, required: true })
          ] })
        ] })
      ] }),
      /* @__PURE__ */ jsxs("section", { className: "profile-section", children: [
        /* @__PURE__ */ jsxs("header", { children: [
          /* @__PURE__ */ jsx("span", { children: /* @__PURE__ */ jsx(BadgeCheck, {}) }),
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx("h2", { children: "Domicilio" }),
            /* @__PURE__ */ jsx("p", { children: "Direcci\xF3n principal asociada a tu cuenta." })
          ] })
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "profile-fields", children: [
          /* @__PURE__ */ jsxs("label", { children: [
            "Provincia",
            /* @__PURE__ */ jsxs("select", { name: "provincia", value: profile.provincia, onChange: changeProvince, required: true, children: [
              /* @__PURE__ */ jsx("option", { value: "", children: "Seleccion\xE1 una provincia" }),
              provinces.map((option) => /* @__PURE__ */ jsx("option", { value: option.name, children: option.name }, option.id || option.name))
            ] })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Departamento",
            /* @__PURE__ */ jsxs("select", { name: "departamento", value: profile.departamento, onChange: changeDepartment, disabled: !profile.provincia, required: true, children: [
              /* @__PURE__ */ jsx("option", { value: "", children: "Seleccion\xE1 un departamento" }),
              departments.map((option) => /* @__PURE__ */ jsx("option", { value: option.name, children: option.name }, option.id || option.name))
            ] })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Localidad",
            /* @__PURE__ */ jsxs("select", { name: "localidad", value: profile.localidad, onChange: changeLocality, disabled: !profile.departamento, required: true, children: [
              /* @__PURE__ */ jsx("option", { value: "", children: "Seleccion\xE1 una localidad" }),
              localities.map((option) => /* @__PURE__ */ jsx("option", { value: option.name, children: option.name }, option.id || option.name))
            ] })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "C\xF3digo postal",
            /* @__PURE__ */ jsx("input", { name: "codigoPostal", value: profile.codigoPostal, onChange: update, required: true })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Calle",
            /* @__PURE__ */ jsx("input", { name: "calle", value: profile.calle, onChange: update, required: true })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "N\xFAmero",
            /* @__PURE__ */ jsx("input", { name: "numeroCalle", value: profile.numeroCalle, onChange: update, required: true })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Piso / Manzana",
            /* @__PURE__ */ jsx("input", { name: "manzanaPiso", value: profile.manzanaPiso, onChange: update })
          ] }),
          /* @__PURE__ */ jsxs("label", { children: [
            "Casa / Departamento",
            /* @__PURE__ */ jsx("input", { name: "casaDepartamento", value: profile.casaDepartamento, onChange: update })
          ] }),
          /* @__PURE__ */ jsxs("label", { className: "profile-wide", children: [
            "Referencia",
            /* @__PURE__ */ jsx("textarea", { name: "referencia", value: profile.referencia, onChange: update, rows: 3 })
          ] })
        ] })
      ] }),
      /* @__PURE__ */ jsxs("footer", { className: "profile-form-footer", children: [
        /* @__PURE__ */ jsxs("span", { children: [
          /* @__PURE__ */ jsx(BadgeCheck, { size: 15 }),
          " Tus datos se guardan de forma segura."
        ] }),
        /* @__PURE__ */ jsxs("button", { className: "profile-save", type: "submit", disabled: saving, children: [
          saving ? /* @__PURE__ */ jsx(LoaderCircle, { className: "spin" }) : /* @__PURE__ */ jsx(Save, { size: 16 }),
          saving ? "Guardando..." : "Guardar cambios"
        ] })
      ] })
    ] }),
    !loading && /* @__PURE__ */ jsx(PasswordChangeForm, { onSuccess: onPasswordChanged })
  ] });
}
