import { useEffect, useState } from "react";
import { api } from "../api";
export function useDollarQuote() {
  const [quote, setQuote] = useState(null);
  const [error, setError] = useState(false);
  useEffect(() => {
    let active = true;
    const refresh = () => {
      const loadQuote = async () => {
        try {
          return await api.exchangeRate();
        } catch {
          const response = await fetch("https://monedapi.ar/api/v2/usd/bna", { headers: { Accept: "application/json" } });
          if (!response.ok) throw new Error(`Cotizacion no disponible (${response.status})`);
          const source = await response.json();
          if (typeof source.sell !== "number" || source.sell <= 0) throw new Error("La fuente no devolvio un precio valido.");
          return {
            currency: source.currency || "USD",
            source: source.name || "Banco Nacion",
            buy: Number(source.buy) || 0,
            sell: source.sell,
            updatedAt: source.updatedAt || "",
            lastScrapedAt: source.lastScrapedAt || "",
            stale: false,
            sourceUrl: "https://www.bna.com.ar/Personas"
          };
        }
      };
      loadQuote().then((value) => {
        if (active) {
          setQuote(value);
          setError(false);
        }
      }).catch(() => {
        if (active) setError(true);
      });
    };
    refresh();
    const timer = window.setInterval(refresh, 5 * 60 * 1e3);
    return () => {
      active = false;
      window.clearInterval(timer);
    };
  }, []);
  return { quote, error };
}
