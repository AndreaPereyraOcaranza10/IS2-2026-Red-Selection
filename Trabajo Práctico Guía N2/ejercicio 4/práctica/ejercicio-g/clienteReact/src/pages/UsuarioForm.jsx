import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { usuariosApi } from '../api/recursos.js'
import { useAvisos } from '../components/Avisos.jsx'
import Encabezado from '../components/Encabezado.jsx'
import { IlustracionUsuarios } from '../components/Ilustraciones.jsx'

const VACIO = { nombreUsuario: '', clave: '' }

export default function UsuarioForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const avisos = useAvisos()
  const [usuario, setUsuario] = useState(VACIO)
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  useEffect(() => {
    if (!id) return
    usuariosApi.obtener(id).then(setUsuario).catch((e) => setError(e.message))
  }, [id])

  function cambiar(e) {
    setUsuario({ ...usuario, [e.target.name]: e.target.value })
  }

  async function guardar(e) {
    e.preventDefault()
    setGuardando(true)
    setError('')
    try {
      if (id) {
        await usuariosApi.actualizar(id, usuario)
        avisos.exito('Usuario actualizado correctamente')
      } else {
        await usuariosApi.crear(usuario)
        avisos.exito('Usuario creado correctamente')
      }
      navigate('/usuarios')
    } catch (err) {
      setError(err.message)
      setGuardando(false)
    }
  }

  return (
    <>
      <Encabezado titulo={id ? 'Editar usuario' : 'Nuevo usuario'} />
      <Encabezado ilustracion={<IlustracionUsuarios />} titulo={id ? 'Editar usuario' : 'Nuevo usuario'} />
      <form className="tarjeta formulario" onSubmit={guardar}>
        {error && <div className="aviso aviso-error">{error}</div>}
        <label>Nombre de usuario
          <input name="nombreUsuario" value={usuario.nombreUsuario ?? ''} onChange={cambiar} />
        </label>
        <label>Clave
          <input name="clave" type="password" value={usuario.clave ?? ''} onChange={cambiar} />
        </label>
        <div className="acciones">
          <button className="btn btn-primario" disabled={guardando}>Guardar</button>
          <Link className="btn btn-borde" to="/usuarios">Cancelar</Link>
        </div>
      </form>
    </>
  )
}