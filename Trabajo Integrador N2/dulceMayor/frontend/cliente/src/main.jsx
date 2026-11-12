import { jsx } from "react/jsx-runtime";
import React from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { Auth0Provider } from "@auth0/auth0-react";
import App from "./App";
import "./styles.css";
import "./assistant.css";

const auth0Domain = import.meta.env.VITE_AUTH0_DOMAIN?.trim() || "";
const auth0ClientId = import.meta.env.VITE_AUTH0_CLIENT_ID?.trim() || "";
const auth0Configured = Boolean(auth0Domain && auth0ClientId);
const app = /* @__PURE__ */ jsx(BrowserRouter, { children: /* @__PURE__ */ jsx(App, { auth0Configured }) });

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
