import { jsx } from "react/jsx-runtime";
import React from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { Auth0Provider, useAuth0 } from "@auth0/auth0-react";
import App from "./App";
import "./styles.css";
import "./assistant.css";

const auth0Domain = import.meta.env.VITE_AUTH0_DOMAIN?.trim() || "";
const auth0ClientId = import.meta.env.VITE_AUTH0_CLIENT_ID?.trim() || "";
const auth0Configured = Boolean(auth0Domain && auth0ClientId);

function Auth0App() {
  const { logout } = useAuth0();
  const auth0Logout = () => logout({ logoutParams: { returnTo: window.location.origin } });
  return /* @__PURE__ */ jsx(App, { auth0Configured: true, auth0Logout });
}

const app = /* @__PURE__ */ jsx(BrowserRouter, { children: auth0Configured
  ? /* @__PURE__ */ jsx(Auth0App, {})
  : /* @__PURE__ */ jsx(App, { auth0Configured: false }) });

createRoot(document.getElementById("root")).render(/* @__PURE__ */ jsx(React.StrictMode, {
  children: auth0Configured ? /* @__PURE__ */ jsx(Auth0Provider, {
    domain: auth0Domain,
    clientId: auth0ClientId,
    authorizationParams: {
      redirect_uri: `${window.location.origin}/ingresar`,
      scope: "openid profile email"
    },
    children: app
  }) : app
}));
