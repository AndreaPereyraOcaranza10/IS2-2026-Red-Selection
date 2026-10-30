import React, { useState, useEffect, createContext, useContext } from "react";
import { createRoot } from "react-dom/client";
import {
  BrowserRouter,
  Routes,
  Route,
  NavLink,
  Navigate,
  useNavigate,
  useLocation,
  Link,
} from "react-router-dom";
import { useForm } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  ArrowUpRight,
  ArrowRight,
  X,
  LogOut,
  UserRound,
  KeyRound,
  ChevronRight,
  Menu,
  Check,
  LoaderCircle,
  ShieldCheck,
} from "lucide-react";
import { api, errorMessage, levels, labels } from "./api";
import type { User, Row } from "./api";
import "./styles.css";
type Values = Record<string, string>;
type Catalog = Record<string, Row[]>;
type Context = {
  user: User;
  catalog: Catalog;
  refresh: () => Promise<void>;
  notify: (s: string) => void;
  setUser: (u: User) => void;
  logout: () => void;
};
const AppContext = createContext<Context>(null!);
const useApp = () => useContext(AppContext);
function Spinner() {
  return (
    <div className="loading" role="status">
      <LoaderCircle className="spin" size={24} /> Cargando datos…
    </div>
  );
}
function App() {
  const [user, setUser] = useState<User | null>(null),
    [catalog, setCatalog] = useState<Catalog>({}),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(""),
    [toast, setToast] = useState("");
  const navigate = useNavigate();
  const refresh = async () => {
    const { data } = await api.get<Catalog>("/catalogos/geografia");
    setCatalog(data);
  };
  const logout = () => {
    sessionStorage.removeItem("atlas-token");
    setUser(null);
    setCatalog({});
    navigate("/login");
  };
  const load = async () => {
    setLoading(true);
    setError("");
    try {
      if (sessionStorage.getItem("atlas-token")) {
        const { data } = await api.get<User>("/usuarios/me");
        await refresh();
        setUser(data);
      }
    } catch (e) {
      if (sessionStorage.getItem("atlas-token")) setError(errorMessage(e));
      else setUser(null);
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    const hash = new URLSearchParams(window.location.hash.slice(1));
    if (hash.get("token")) {
      sessionStorage.setItem("atlas-token", hash.get("token")!);
      window.history.replaceState({}, "", window.location.pathname);
    }
    void load();
    const cb = () => {
      setUser(null);
      setCatalog({});
      navigate("/login");
    };
    window.addEventListener("atlas-logout", cb);
    return () => window.removeEventListener("atlas-logout", cb);
  }, []);
  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => setToast(""), 4500);
    return () => clearTimeout(timer);
  }, [toast]);
  if (loading) return <Spinner />;
  if (error)
    return (
      <main className="fatal">
        <h1>No pudimos cargar tus datos</h1>
        <p>{error}</p>
        <button onClick={() => void load()}>Reintentar</button>
        <button className="secondary" onClick={logout}>
          Volver al inicio
        </button>
      </main>
    );
  if (!user) return <Auth onLogin={load} />;
  return (
    <AppContext.Provider
      value={{ user, catalog, refresh, notify: setToast, setUser, logout }}
    >
      <Shell>
        <Routes>
          <Route path="/" element={<Navigate to="/mi-perfil" replace />} />
          <Route path="/mi-perfil" element={<Profile />} />
          <Route path="/cambiar-contrasena" element={<Password />} />
          <Route path="*" element={<Navigate to="/mi-perfil" replace />} />
        </Routes>
      </Shell>
      {toast && (
        <div className="toast" role="status">
          <Check size={18} />
          {toast}
          <button aria-label="Cerrar mensaje" onClick={() => setToast("")}>
            <X size={16} />
          </button>
        </div>
      )}
    </AppContext.Provider>
  );
}
function Auth({ onLogin }: { onLogin: () => Promise<void> }) {
  const [register, setRegister] = useState(false),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false),
    [google, setGoogle] = useState(false);
  const navigate = useNavigate();
  const schema = z.object({
    email: z.email("Ingresá un email válido"),
    password: z
      .string()
      .min(8, "Mínimo 8 caracteres")
      .max(72, "Máximo 72 caracteres")
      .refine(
        (value) => new TextEncoder().encode(value).length <= 72,
        "Máximo 72 bytes",
      )
      .refine(
        (value) => !register || /[a-zA-Z]/.test(value),
        "Debe incluir letras",
      )
      .refine(
        (value) => !register || /[0-9]/.test(value),
        "Debe incluir números",
      ),
  });
  const form = useForm<{ email: string; password: string }>({
    resolver: zodResolver(schema),
  });
  useEffect(() => {
    api
      .get("/auth/config")
      .then((r) => setGoogle(r.data.google))
      .catch(() => {});
    if (new URLSearchParams(location.search).has("error"))
      setError(
        "No se pudo completar el acceso con Google. Verificá tu cuenta y el método de acceso.",
      );
  }, []);
  const submit = form.handleSubmit(async (values) => {
    setBusy(true);
    setError("");
    try {
      const { data } = await api.post(
        "/auth/" + (register ? "register" : "login"),
        values,
      );
      sessionStorage.setItem("atlas-token", data.token);
      navigate("/mi-perfil");
      await onLogin();
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  });
  return (
    <div className="auth">
      <aside>
        <div className="brand">
          <span className="brand-icon">A</span> atlas
          <span className="brand-dot">.</span>
        </div>
        <div>
          <span className="eyebrow light">TU CUENTA PERSONAL</span>
          <h1>
            Tu información.
            <br />
            Tu cuenta.
            <br />
            <em>Siempre con vos.</em>
          </h1>
          <p>
            Tu información personal, organizada.
            <br />
            Un espacio para mantenerla al día.
          </p>
        </div>
        <span className="auth-caption">
          TU PERFIL · TU DIRECCIÓN · TU SEGURIDAD
        </span>
      </aside>
      <main>
        <div className="auth-form">
          <span className="eyebrow">TU CUENTA PERSONAL</span>
          <h2>{register ? "Creá tu cuenta" : "Bienvenido de nuevo"}</h2>
          <p>
            {register
              ? "Completá tus datos para comenzar."
              : "Ingresá para continuar a Atlas."}
          </p>
          <form onSubmit={submit}>
            <label>
              Email
              <input
                type="email"
                autoComplete="email"
                {...form.register("email")}
              />
              <small>{form.formState.errors.email?.message}</small>
            </label>
            <label>
              Contraseña
              <input
                type="password"
                autoComplete={register ? "new-password" : "current-password"}
                {...form.register("password")}
              />
              <small>{form.formState.errors.password?.message}</small>
            </label>
            {register && (
              <p className="hint">
                Al menos 8 caracteres, con letras y números.
              </p>
            )}
            {error && (
              <div className="error" role="alert">
                {error}
              </div>
            )}
            <button disabled={busy}>
              {busy
                ? "Conectando…"
                : register
                  ? "Crear cuenta"
                  : "Iniciar sesión"}
              <ArrowRight size={18} />
            </button>
          </form>
          {google && (
            <a
              className="button secondary google"
              href="/oauth2/authorization/google"
            >
              Continuar con Google
            </a>
          )}
          <p className="auth-switch">
            {register ? "¿Ya tenés una cuenta?" : "¿Primera vez en Atlas?"}{" "}
            <button
              className="text-button"
              onClick={() => {
                setRegister(!register);
                setError("");
              }}
            >
              {register ? "Iniciá sesión" : "Registrate"}
            </button>
          </p>
        </div>
      </main>
    </div>
  );
}
function Shell({ children }: { children: React.ReactNode }) {
  const { user, logout } = useApp();
  const [open, setOpen] = useState(false);
  const loc = useLocation();
  useEffect(() => setOpen(false), [loc.pathname]);
  const name = user.persona?.nombre || user.email.split("@")[0];
  return (
    <div className="app">
      <aside className={"sidebar " + (open ? "open" : "")}>
        <Link to="/mi-perfil" className="brand">
          <span className="brand-icon">A</span>atlas
          <span className="brand-dot">.</span>
        </Link>
        <span className="nav-caption">MI CUENTA</span>
        <nav>
          <NavLink to="/mi-perfil">
            <UserRound size={19} />
            Mi Perfil
          </NavLink>
          <NavLink to="/cambiar-contrasena">
            <KeyRound size={19} />
            Cambiar Contraseña
          </NavLink>
          <button className="logout-button" onClick={logout}>
            <LogOut size={19} />
            Cerrar Sesión
          </button>
        </nav>
        <div className="user-card">
          <span className="avatar">
            {String(name).slice(0, 2).toUpperCase()}
          </span>
          <div>
            <strong>{name}</strong>
            <small>Mi cuenta personal</small>
          </div>
          <ShieldCheck size={17} />
        </div>
      </aside>
      {open && (
        <button
          className="overlay"
          aria-label="Cerrar menú"
          onClick={() => setOpen(false)}
        />
      )}
      <div className="workspace">
        <header className="topbar">
          <div>
            <button
              className="menu-button"
              aria-label="Abrir menú"
              onClick={() => setOpen(true)}
            >
              <Menu />
            </button>
            <span>Mi cuenta</span>
            <ChevronRight size={14} />
            <strong>
              {loc.pathname === "/cambiar-contrasena"
                ? "Cambiar Contraseña"
                : "Mi Perfil"}
            </strong>
          </div>
          <Link to="/mi-perfil" className="top-user">
            <span className="status-dot" />
            Mi cuenta
            <span className="avatar small-avatar">
              {String(name).slice(0, 1).toUpperCase()}
            </span>
          </Link>
        </header>
        <main className="content">{children}</main>
        <footer>
          Atlas <span>Mi cuenta personal</span>
          <span>Tu información, en un solo lugar.</span>
        </footer>
      </div>
    </div>
  );
}
function Hierarchy({
  form,
  depth,
  catalog,
}: {
  form: UseFormReturn<Values>;
  depth: number;
  catalog: Catalog;
}) {
  const values = form.watch();
  return (
    <>
      {levels.slice(0, depth).map((l, i) => {
        const previous = i ? levels[i - 1] : null;
        const rows = (catalog[l.module] || []).filter(
          (r) => !previous || String(r[previous.key]) === values[previous.key],
        );
        return (
          <label key={l.key}>
            {l.label}
            <select
              {...form.register(l.key)}
              value={values[l.key] || ""}
              disabled={!!previous && !values[previous.key]}
              onChange={(e) => {
                form.setValue(l.key, e.target.value, { shouldValidate: true });
                levels
                  .slice(i + 1)
                  .forEach((next) => form.setValue(next.key, ""));
              }}
            >
              <option value="">Seleccionar {l.label.toLowerCase()}</option>
              {rows.map((r) => (
                <option value={r.id} key={r.id}>
                  {r.nombre || `${r.calle} ${r.numeracion}`}
                </option>
              ))}
            </select>
            <small>{form.formState.errors[l.key]?.message}</small>
          </label>
        );
      })}
    </>
  );
}
const required = z
  .string()
  .trim()
  .min(1, "Este campo es obligatorio")
  .max(150, "Máximo 150 caracteres");
const optionalFields = [
  "barrio",
  "manzanaPiso",
  "casaDepartamento",
  "referencia",
];
function makeSchema(fields: string[], depth = 0) {
  const shape: Record<string, z.ZodString> = {};
  fields.forEach(
    (f) =>
      (shape[f] = optionalFields.includes(f)
        ? z.string().max(f === "referencia" ? 500 : 150)
        : required),
  );
  levels.slice(0, depth).forEach((l) => (shape[l.key] = required));
  return z.object(shape);
}
function Fields({
  form,
  fields,
}: {
  form: UseFormReturn<Values>;
  fields: string[];
}) {
  return (
    <>
      {fields.map((f) => (
        <label className={f === "referencia" ? "wide" : ""} key={f}>
          {labels[f]}
          {optionalFields.includes(f) && (
            <span className="optional">Opcional</span>
          )}
          <input
            {...form.register(f)}
            type={f === "fechaNacimiento" ? "date" : "text"}
            inputMode={f === "dni" ? "numeric" : undefined}
            placeholder={f === "dni" ? "Sin puntos ni espacios" : undefined}
            max={f === "fechaNacimiento" ? todayDate() : undefined}
            autoComplete={f === "fechaNacimiento" ? "bday" : undefined}
            maxLength={f === "dni" ? 8 : f === "referencia" ? 500 : 150}
          />
          <small>{form.formState.errors[f]?.message}</small>
        </label>
      ))}
    </>
  );
}
function todayDate() {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}
function Profile() {
  const { user, setUser, catalog, refresh, notify } = useApp(),
    [error, setError] = useState("");
  const personalFields = ["nombre", "apellido", "dni", "fechaNacimiento"];
  const addressFields = [
    "calle",
    "numeracion",
    "barrio",
    "manzanaPiso",
    "casaDepartamento",
    "referencia",
  ];
  const fields = [...personalFields, ...addressFields];
  const schema = makeSchema([...fields, "email"], 4).extend({
    email: z.email("Ingresá un email válido"),
    dni: z
      .string()
      .trim()
      .regex(/^[0-9]{7,8}$/, "Ingresá un DNI de 7 u 8 dígitos, sin puntos"),
    fechaNacimiento: z.iso
      .date("Ingresá una fecha válida")
      .refine(
        (value) => value <= todayDate(),
        "La fecha de nacimiento no puede ser futura",
      ),
  });
  const defaults = Object.fromEntries(
    [...fields, ...levels.slice(0, 4).map((l) => l.key)].map((f) => [
      f,
      String(user.persona?.[f] ?? ""),
    ]),
  );
  defaults.email = user.email;
  const form = useForm<Values>({
    defaultValues: defaults,
    resolver: zodResolver(schema),
  });
  const submit = form.handleSubmit(async (values) => {
    setError("");
    try {
      const direccion: Record<string, unknown> = Object.fromEntries(
        addressFields.map((f) => [f, values[f]]),
      );
      direccion.localidadId = Number(values.localidadId);
      const { data } = await api.put<User>("/usuarios/me", {
        email: values.email,
        nombre: values.nombre,
        apellido: values.apellido,
        dni: values.dni,
        fechaNacimiento: values.fechaNacimiento,
        direccion,
      });
      setUser(data);
      await refresh();
      notify("Tu perfil se actualizó correctamente");
    } catch (e) {
      setError(errorMessage(e));
    }
  });
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">MI CUENTA</span>
          <h1>
            Mi Perfil<span className="accent">.</span>
          </h1>
          <p>Tu información personal y tu lugar en el mapa.</p>
        </div>
      </div>
      <form className="profile-form" onSubmit={submit}>
        <section className="panel">
          <h2>Datos de la cuenta</h2>
          <p className="section-copy">
            Administrá la información de acceso a tu cuenta.
          </p>
          <div className="form-grid">
            <label>
              Email
              <input
                type="email"
                readOnly={user.proveedorAutenticacion !== "LOCAL"}
                {...form.register("email")}
              />
              <small>{form.formState.errors.email?.message}</small>
            </label>
            <label>
              Proveedor de autenticación
              <input
                readOnly
                value={
                  user.proveedorAutenticacion === "GOOGLE"
                    ? "Google"
                    : "Email y contraseña"
                }
              />
            </label>
          </div>
        </section>
        <section className="panel">
          <h2>Datos personales</h2>
          <div className="form-grid">
            <Fields form={form} fields={personalFields} />
          </div>
        </section>
        <section className="panel">
          <h2>Tu dirección</h2>
          <p className="section-copy">
            Elegí la ubicación y completá los detalles del domicilio.
          </p>
          <div className="form-grid">
            <Hierarchy form={form} depth={4} catalog={catalog} />
            <Fields form={form} fields={addressFields} />
          </div>
        </section>
        {error && (
          <div className="error" role="alert">
            {error}
          </div>
        )}
        <div className="form-actions">
          <button disabled={form.formState.isSubmitting}>
            {form.formState.isSubmitting ? "Guardando…" : "Guardar cambios"}
            <Check size={18} />
          </button>
        </div>
      </form>
    </>
  );
}
function Password() {
  const { user, logout } = useApp();
  const [error, setError] = useState(""),
    [success, setSuccess] = useState(false);
  const schema = z
    .object({
      actual: z.string().min(1, "Ingresá tu contraseña actual"),
      nueva: z
        .string()
        .min(8, "Mínimo 8 caracteres")
        .max(72)
        .refine(
          (value) => new TextEncoder().encode(value).length <= 72,
          "Máximo 72 bytes",
        )
        .regex(/[a-zA-Z]/, "Debe incluir letras")
        .regex(/[0-9]/, "Debe incluir números"),
      confirmacion: z.string().min(1, "Confirmá tu nueva contraseña"),
    })
    .refine((v) => v.nueva === v.confirmacion, {
      path: ["confirmacion"],
      message: "Las contraseñas no coinciden",
    })
    .refine((v) => v.actual !== v.nueva, {
      path: ["nueva"],
      message: "Debe ser diferente a la actual",
    });
  const form = useForm<{ actual: string; nueva: string; confirmacion: string }>(
    { resolver: zodResolver(schema) },
  );
  const submit = form.handleSubmit(async (values) => {
    setError("");
    try {
      await api.put("/usuarios/me/password", values);
      sessionStorage.removeItem("atlas-token");
      setSuccess(true);
    } catch (e) {
      setError(errorMessage(e));
    }
  });
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">SEGURIDAD DE LA CUENTA</span>
          <h1>
            Cambiar Contraseña<span className="accent">.</span>
          </h1>
          <p>Mantené protegido tu acceso al cuenta personal.</p>
        </div>
      </div>
      <section className="panel password-panel">
        <span className="stat-icon">
          <KeyRound size={24} />
        </span>
        {success ? (
          <>
            <h2>Contraseña actualizada</h2>
            <p>
              Tus sesiones anteriores fueron invalidadas. Iniciá sesión con tu
              nueva contraseña.
            </p>
            <button onClick={logout}>
              Volver a iniciar sesión
              <ArrowRight size={18} />
            </button>
          </>
        ) : !user.tienePassword ? (
          <>
            <h2>Tu cuenta utiliza Google</h2>
            <p>
              Iniciás sesión mediante autenticación externa. Administrá tu
              contraseña desde tu cuenta de Google.
            </p>
            <a
              className="button secondary"
              href="https://myaccount.google.com/security"
              target="_blank"
              rel="noreferrer"
            >
              Seguridad de Google
              <ArrowUpRight size={18} />
            </a>
          </>
        ) : (
          <>
            <h2>Una contraseña segura, una cuenta protegida</h2>
            <p className="section-copy">
              Usá entre 8 y 72 caracteres, con letras y números. Después del
              cambio deberás iniciar sesión nuevamente.
            </p>
            <form onSubmit={submit}>
              {(["actual", "nueva", "confirmacion"] as const).map((f) => (
                <label key={f}>
                  {labels[f]}
                  <input
                    type="password"
                    autoComplete={
                      f === "actual" ? "current-password" : "new-password"
                    }
                    {...form.register(f)}
                  />
                  <small>{form.formState.errors[f]?.message}</small>
                </label>
              ))}
              {error && (
                <div className="error" role="alert">
                  {error}
                </div>
              )}
              <button disabled={form.formState.isSubmitting}>
                {form.formState.isSubmitting
                  ? "Actualizando…"
                  : "Actualizar contraseña"}
                <ArrowRight size={18} />
              </button>
            </form>
          </>
        )}
      </section>
    </>
  );
}
createRoot(document.getElementById("root")!).render(
  <BrowserRouter>
    <App />
  </BrowserRouter>,
);
