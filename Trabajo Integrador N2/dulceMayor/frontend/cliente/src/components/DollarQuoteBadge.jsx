import { jsx, jsxs } from "react/jsx-runtime";
import { useDollarQuote } from "../hooks/useDollarQuote";
const money = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", minimumFractionDigits: 2, maximumFractionDigits: 2 });
export function DollarQuoteBadge({ compact = false }) {
  const { quote, error } = useDollarQuote();
  const timestamp = quote?.lastScrapedAt || quote?.updatedAt;
  const parsedTimestamp = timestamp ? Date.parse(timestamp) : Number.NaN;
  const time = Number.isFinite(parsedTimestamp) ? new Intl.DateTimeFormat("es-AR", { hour: "2-digit", minute: "2-digit" }).format(new Date(parsedTimestamp)) : "";
  const label = quote ? `${quote.source}: cotizaci\xF3n de venta ${money.format(quote.sell)}${time ? `, confirmada a las ${time}` : ""}${quote.stale ? ", dato anterior por falta de conexi\xF3n" : ""}` : error ? "Cotizaci\xF3n del Banco Naci\xF3n no disponible" : "Consultando cotizaci\xF3n del Banco Naci\xF3n";
  return /* @__PURE__ */ jsxs("span", { className: `usd usd-quote${compact ? " usd-compact" : ""}${quote?.stale ? " usd-stale" : ""}`, title: label, "aria-live": "polite", children: [
    /* @__PURE__ */ jsx("i", {}),
    /* @__PURE__ */ jsx("span", { children: "USD BNA" }),
    /* @__PURE__ */ jsx("b", { children: quote ? money.format(quote.sell) : "\u2014" }),
    /* @__PURE__ */ jsx("small", { children: quote ? `${quote.stale ? "Dato previo" : "Venta"}${time ? ` \xB7 ${time}` : ""}` : error ? "Sin cotizaci\xF3n" : "Actualizando\u2026" })
  ] });
}
