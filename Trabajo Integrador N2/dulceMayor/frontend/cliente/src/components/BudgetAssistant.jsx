import { useEffect, useRef, useState } from "react";
import { Check, LoaderCircle, MessageCircle, Send, X } from "lucide-react";
import { api } from "../api";

const currency = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 2 });

export function BudgetAssistant({ open, onClose, onAddItems }) {
  const [messages, setMessages] = useState([
    { role: "assistant", text: "¡Hola! Contame qué productos y cantidades necesitás y te ayudo a armar un presupuesto." }
  ]);
  const [draft, setDraft] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const endRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    if (open) inputRef.current?.focus();
  }, [open]);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages, loading]);

  async function submit(event) {
    event.preventDefault();
    const mensaje = draft.trim();
    if (!mensaje || loading) return;
    setMessages((current) => [...current, { role: "user", text: mensaje }]);
    setDraft("");
    setError("");
    setLoading(true);
    try {
      const presupuesto = await api.chatbotBudget(mensaje);
      setMessages((current) => [...current, { role: "assistant", text: presupuesto.mensaje || "Preparé esta propuesta con el catálogo disponible.", presupuesto }]);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "No se pudo conectar con el asistente.");
    } finally {
      setLoading(false);
    }
  }

  if (!open) return null;

  return (
    <section className="budget-assistant" role="dialog" aria-modal="false" aria-labelledby="budget-assistant-title">
      <header className="budget-assistant-head">
        <span className="budget-assistant-avatar"><MessageCircle size={18} /></span>
        <div>
          <b id="budget-assistant-title">Asistente de presupuestos</b>
          <small>Productos, stock y precios del catálogo</small>
        </div>
        <button type="button" className="icon-button" onClick={onClose} aria-label="Cerrar asistente"><X size={18} /></button>
      </header>

      <div className="budget-assistant-messages" aria-live="polite">
        {messages.map((message, index) => (
          <article className={`assistant-message ${message.role}`} key={`${message.role}-${index}`}>
            <p>{message.text}</p>
            {message.presupuesto && <BudgetResult presupuesto={message.presupuesto} onAddItems={onAddItems} />}
          </article>
        ))}
        {loading && <div className="assistant-loading"><LoaderCircle className="spin" size={16} /> Buscando en el catálogo...</div>}
        {error && <p className="assistant-error" role="alert">{error}</p>}
        <div ref={endRef} />
      </div>

      <form className="budget-assistant-form" onSubmit={submit}>
        <label className="sr-only" htmlFor="budget-assistant-input">¿Qué productos necesitás?</label>
        <textarea
          id="budget-assistant-input"
          ref={inputRef}
          value={draft}
          onChange={(event) => setDraft(event.target.value)}
          placeholder="Ej.: 5 bultos de gomitas y 2 de chicles"
          maxLength={1000}
          rows={2}
          disabled={loading}
        />
        <button type="submit" aria-label="Enviar consulta" disabled={loading || !draft.trim()}><Send size={17} /></button>
      </form>
      <small className="budget-assistant-note">El presupuesto es estimado y no reserva stock.</small>
    </section>
  );
}

function BudgetResult({ presupuesto, onAddItems }) {
  const lineas = presupuesto.items || [];
  return (
    <div className="assistant-result">
      {lineas.length > 0 && (
        <div className="assistant-result-lines">
          {lineas.map((linea, index) => (
            <div className={`assistant-result-line ${linea.valida ? "valid" : "invalid"}`} key={`${linea.productoId}-${index}`}>
              <div><b>{linea.nombre}</b><small>{linea.cantidad} bulto{linea.cantidad === 1 ? "" : "s"} · {linea.stockDisponible} disponibles</small></div>
              <strong>{linea.subtotal == null ? "—" : currency.format(linea.subtotal)}</strong>
              {linea.observacion && <small className="assistant-line-warning">{linea.observacion}</small>}
            </div>
          ))}
        </div>
      )}
      {presupuesto.total != null && <div className="assistant-result-total"><span>Total estimado</span><b>{currency.format(presupuesto.total)}</b></div>}
      {!lineas.length && presupuesto.observaciones?.map((observacion, index) => <p className="assistant-line-warning" key={index}>{observacion}</p>)}
      {presupuesto.validado && lineas.length > 0 && (
        <button type="button" className="assistant-add-button" onClick={() => onAddItems(lineas)}>
          <Check size={15} /> Agregar al presupuesto
        </button>
      )}
    </div>
  );
}
