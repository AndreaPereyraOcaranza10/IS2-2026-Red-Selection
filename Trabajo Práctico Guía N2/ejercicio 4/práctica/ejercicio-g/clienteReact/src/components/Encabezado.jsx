export default function Encabezado({ titulo, descripcion, acciones, ilustracion }) {
  return (
    <div className="titulo-pagina">
      <div className="titulo-texto">
        <h1>{titulo}</h1>
        {descripcion && <p>{descripcion}</p>}
        {acciones && <div className="acciones titulo-acciones">{acciones}</div>}
      </div>
      {ilustracion && <div className="titulo-ilustracion">{ilustracion}</div>}
    </div>
  )
}