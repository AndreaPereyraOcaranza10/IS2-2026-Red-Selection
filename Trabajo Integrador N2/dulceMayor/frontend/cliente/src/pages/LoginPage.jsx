import { Fragment, jsx, jsxs } from "react/jsx-runtime";
import { useState } from "react";
import { ArrowRight, Check, Eye, EyeOff, Headphones, ShieldCheck } from "lucide-react";
import { Link } from "react-router-dom";
import { api } from "../api";
import { Auth0GoogleButton } from "../components/Auth0GoogleButton.jsx";
export function LoginPage({ auth0Configured, onSuccess, onStaff, onAuth0Success }) {
  const [mode, setMode] = useState("login");
  const [visible, setVisible] = useState(false);
  const [notice, setNotice] = useState("");
  const submit = async (event) => {
    event.preventDefault();
    setNotice("");
    const form = new FormData(event.currentTarget);
    const email = String(form.get("email") || "");
    const password = String(form.get("password") || "");
    try {
      if (mode === "login") {
        await onSuccess(email, password);
        return;
      }
      const result = await api.register(email, password);
      setNotice(result.message);
    } catch (error) {
      setNotice(error instanceof Error ? error.message : "No se pudo completar la solicitud.");
    }
  };
  const signInWithAuth0 = async (idToken) => {
    setNotice("");
    try {
      const session = await api.loginWithAuth0(idToken);
      await onAuth0Success?.(session);
    } catch (error) {
      setNotice(error instanceof Error ? error.message : "No se pudo ingresar con Google via Auth0.");
    }
  };
  return /* @__PURE__ */ jsxs("main", { className: "login-page", children: [
    /* @__PURE__ */ jsxs("div", { className: "login-topline", children: [
      /* @__PURE__ */ jsxs("span", { children: [
        /* @__PURE__ */ jsx("i", {}),
        " Portal para comercios registrados \xB7 Cuyo"
      ] }),
      /* @__PURE__ */ jsxs("span", { className: "login-topline-info", children: [
        "Corte de pedidos: ",
        /* @__PURE__ */ jsx("b", { children: "13:30 hs" })
      ] })
    ] }),
    /* @__PURE__ */ jsxs("div", { className: "login-layout", children: [
      /* @__PURE__ */ jsxs("section", { className: "login-pitch", children: [
        /* @__PURE__ */ jsx("span", { className: "section-kicker", children: "ABASTECIMIENTO DIRECTO" }),
        /* @__PURE__ */ jsxs("h1", { children: [
          "Portal B2B",
          /* @__PURE__ */ jsx("br", {}),
          /* @__PURE__ */ jsx("em", { children: "Mayorista." })
        ] }),
        /* @__PURE__ */ jsx("p", { children: "Abastecimiento directo y listas de f\xE1brica para kioscos, autoservicios y distribuidores en Gran Mendoza y San Juan." }),
        /* @__PURE__ */ jsxs("div", { className: "login-benefits", children: [
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx(Check, {}),
            " Facturaci\xF3n fiscal autom\xE1tica con validaci\xF3n online de CUIT."
          ] }),
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx(Check, {}),
            " Precios oficiales por bulto cerrado y pallets consolidados."
          ] }),
          /* @__PURE__ */ jsxs("div", { children: [
            /* @__PURE__ */ jsx(Check, {}),
            " Retiro prioritario en dep\xF3sitos o reparto programado."
          ] })
        ] }),
        /* @__PURE__ */ jsxs("div", { className: "login-pitch-bottom", children: [
          /* @__PURE__ */ jsx(ShieldCheck, {}),
          /* @__PURE__ */ jsxs("span", { children: [
            /* @__PURE__ */ jsx("b", { children: "Una cuenta para tu negocio." }),
            /* @__PURE__ */ jsx("small", { children: "Precios y condiciones pensados para comercios." })
          ] })
        ] })
      ] }),
      /* @__PURE__ */ jsxs("section", { className: "login-card", children: [
        /* @__PURE__ */ jsxs("div", { className: "login-tabs", children: [
          /* @__PURE__ */ jsx("button", { className: mode === "login" ? "selected" : "", onClick: () => {
            setMode("login");
            setNotice("");
          }, children: "Iniciar sesi\xF3n" }),
          /* @__PURE__ */ jsx("button", { className: mode === "register" ? "selected" : "", onClick: () => {
            setMode("register");
            setNotice("");
          }, children: "Crear cuenta mayorista" })
        ] }),
        mode === "login" ? /* @__PURE__ */ jsxs(Fragment, { children: [
          /* @__PURE__ */ jsxs("div", { className: "login-card-heading", children: [
            /* @__PURE__ */ jsx("h2", { children: "Ingreso de clientes" }),
            /* @__PURE__ */ jsx("p", { children: "Ingres\xE1 con el correo comercial registrado." })
          ] }),
          /* @__PURE__ */ jsxs("form", { onSubmit: submit, children: [
            /* @__PURE__ */ jsxs("label", { children: [
              "Correo comercial",
              /* @__PURE__ */ jsx("input", { name: "email", type: "email", required: true, placeholder: "comercio@correo.com" })
            ] }),
            /* @__PURE__ */ jsxs("label", { children: [
              /* @__PURE__ */ jsxs("span", { className: "label-row", children: [
                "Contrase\xF1a ",
                /* @__PURE__ */ jsx("button", { type: "button", onClick: () => setNotice("Solicit&aacute; la recuperaci&oacute;n de tu clave al equipo de Atenci&oacute;n Mayorista."), children: "\xBFOlvidaste tu clave?" })
              ] }),
              /* @__PURE__ */ jsxs("span", { className: "password-field", children: [
                /* @__PURE__ */ jsx("input", { name: "password", required: true, minLength: 4, type: visible ? "text" : "password", placeholder: "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022" }),
                /* @__PURE__ */ jsx("button", { type: "button", "aria-label": visible ? "Ocultar contrasena" : "Mostrar contrasena", onClick: () => setVisible(!visible), children: visible ? /* @__PURE__ */ jsx(EyeOff, {}) : /* @__PURE__ */ jsx(Eye, {}) })
              ] })
            ] }),
            /* @__PURE__ */ jsxs("label", { className: "remember", children: [
              /* @__PURE__ */ jsx("input", { type: "checkbox" }),
              " Recordar en este dispositivo"
            ] }),
            /* @__PURE__ */ jsxs("button", { className: "login-submit", children: [
              "Ingresar al portal ",
              /* @__PURE__ */ jsx(ArrowRight, {})
            ] })
          ] }),
          /* @__PURE__ */ jsx(Auth0GoogleButton, { configured: auth0Configured, mode: "login", onCredential: signInWithAuth0, onError: setNotice }),
          /* @__PURE__ */ jsx("p", { className: "login-assurance", children: "Habilitacion automatica tras validacion fiscal." }),
          /* @__PURE__ */ jsxs("button", { className: "staff-access", onClick: onStaff, children: [
            "\xBFTrabajas en deposito? ",
            /* @__PURE__ */ jsx("b", { children: "Abrir centro de mando \u2192" })
          ] })
        ] }) : /* @__PURE__ */ jsxs(Fragment, { children: [
          /* @__PURE__ */ jsxs("div", { className: "login-card-heading", children: [
            /* @__PURE__ */ jsx("h2", { children: "Crear cuenta mayorista" }),
            /* @__PURE__ */ jsx("p", { children: "Registrate con tu correo y una clave. Luego podrás completar tus datos y domicilio en Mi perfil." })
          ] }),
          /* @__PURE__ */ jsxs("form", { onSubmit: submit, children: [
            /* @__PURE__ */ jsxs("label", { children: [
              "Correo comercial",
              /* @__PURE__ */ jsx("input", { name: "email", required: true, type: "email", placeholder: "nombre@comercio.com" })
            ] }),
            /* @__PURE__ */ jsxs("label", { children: [
              "Contrasena",
              /* @__PURE__ */ jsx("input", { name: "password", required: true, type: "password", minLength: 6, placeholder: "Al menos 6 caracteres" })
            ] }),
            /* @__PURE__ */ jsxs("button", { className: "login-submit", children: [
              "Solicitar alta mayorista ",
              /* @__PURE__ */ jsx(Check, {})
            ] })
          ] }),
          /* @__PURE__ */ jsx(Auth0GoogleButton, { configured: auth0Configured, mode: "register", onCredential: signInWithAuth0, onError: setNotice }),
          /* @__PURE__ */ jsxs("button", { className: "staff-access", onClick: onStaff, children: [
            "\xBFTrabajas en deposito? ",
            /* @__PURE__ */ jsx("b", { children: "Abrir centro de mando \u2192" })
          ] })
        ] }),
        notice && /* @__PURE__ */ jsx("p", { className: "login-notice", role: "status", children: notice })
      ] })
    ] }),
    /* @__PURE__ */ jsxs("footer", { className: "login-footer", children: [
      /* @__PURE__ */ jsxs(Link, { className: "brand", to: "/#inicio", children: [
        /* @__PURE__ */ jsx("span", { className: "brand-mark", children: "DM" }),
        /* @__PURE__ */ jsxs("span", { className: "brand-copy", children: [
          /* @__PURE__ */ jsx("strong", { children: "Dulce Mayor" }),
          /* @__PURE__ */ jsx("small", { children: "MAYORISTA B2B \xB7 MENDOZA" })
        ] })
      ] }),
      /* @__PURE__ */ jsxs("span", { children: ["\xA9 ", new Date().getFullYear(), " Dulce Mayor. Todos los derechos reservados."] }),
      /* @__PURE__ */ jsxs("a", { href: "tel:+542614250800", children: [
        /* @__PURE__ */ jsx(Headphones, {}),
        " Soporte comercial"
      ] })
    ] })
  ] });
}
