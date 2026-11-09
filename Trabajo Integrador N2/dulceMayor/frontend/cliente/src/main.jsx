import { jsx } from "react/jsx-runtime";
import React from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import "./styles.css";
import "./assistant.css";
createRoot(document.getElementById("root")).render(/* @__PURE__ */ jsx(React.StrictMode, { children: /* @__PURE__ */ jsx(BrowserRouter, { children: /* @__PURE__ */ jsx(App, {}) }) }));
