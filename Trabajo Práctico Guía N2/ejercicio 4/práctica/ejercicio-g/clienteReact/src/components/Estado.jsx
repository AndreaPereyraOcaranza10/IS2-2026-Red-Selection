export default function Estado({ cargando, error, children }) {
  if (cargando) return <p className="vacio">Cargando…</p>
  if (error) return <div className="aviso aviso-error">{error}</div>
  return children
}