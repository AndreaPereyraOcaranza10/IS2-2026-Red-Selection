import { Link } from 'react-router-dom'

export default function BotonesFila({ editarA, onEliminar }) {
  return (
    <div className="acciones">
      <Link className="btn btn-chico btn-borde" to={editarA}>Editar</Link>
      <button className="btn btn-chico btn-peligro" onClick={onEliminar}>Eliminar</button>
    </div>
  )
}