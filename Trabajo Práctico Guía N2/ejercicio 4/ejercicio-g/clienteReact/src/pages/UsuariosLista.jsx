import { Link } from 'react-router-dom'
import { usuariosApi } from '../api/recursos.js'
import { useCargar } from '../utils/useCargar.js'
import { useAvisos } from '../components/Avisos.jsx'
import Encabezado from '../components/Encabezado.jsx'
import { IlustracionUsuarios } from '../components/Ilustraciones.jsx'
import Estado from '../components/Estado.jsx'
import BotonesFila from '../components/BotonesFila.jsx'

export default function UsuariosLista() {
  const { datos, cargando, error, recargar } = useCargar(() => usuariosApi.listar())
  const avisos = useAvisos()

  async function eliminar(u) {
    if (!window.confirm(`¿Eliminar al usuario ${u.nombreUsuario}?`)) return
    try {
      await usuariosApi.eliminar(u.id)
      avisos.exito('Usuario eliminado')
      recargar()
    } catch (e) {
      avisos.error(e.message)
    }
  }

  return (
    <>
      <Encabezado
        ilustracion={<IlustracionUsuarios />}
        titulo="Usuarios"
        descripcion="Quienes cargan las historias clínicas"
        acciones={<Link className="btn btn-primario" to="/usuarios/nuevo">Nuevo usuario</Link>}
      />
      <Estado cargando={cargando} error={error}>
        <div className="tabla-caja">
          <table>
            <thead>
              <tr><th>Usuario</th><th className="der">Acciones</th></tr>
            </thead>
            <tbody>
              {datos?.map((u) => (
                <tr key={u.id}>
                  <td>{u.nombreUsuario}</td>
                  <td className="der"><BotonesFila editarA={`/usuarios/${u.id}/editar`} onEliminar={() => eliminar(u)} /></td>
                </tr>
              ))}
              {datos?.length === 0 && <tr><td colSpan="2" className="vacio">Todavía no hay usuarios</td></tr>}
            </tbody>
          </table>
        </div>
      </Estado>
    </>
  )
}