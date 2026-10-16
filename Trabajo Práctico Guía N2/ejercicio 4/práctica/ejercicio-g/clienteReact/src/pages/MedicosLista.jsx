import { Link } from 'react-router-dom'
import { medicosApi } from '../api/recursos.js'
import { useCargar } from '../utils/useCargar.js'
import { useAvisos } from '../components/Avisos.jsx'
import Encabezado from '../components/Encabezado.jsx'
import Estado from '../components/Estado.jsx'
import BotonesFila from '../components/BotonesFila.jsx'
import { IlustracionMedicos } from '../components/Ilustraciones.jsx'

export default function MedicosLista() {
  const { datos, cargando, error, recargar } = useCargar(() => medicosApi.listar())
  const avisos = useAvisos()

  async function eliminar(m) {
    if (!window.confirm(`¿Eliminar al médico ${m.apellido}, ${m.nombre}?`)) return
    try {
      await medicosApi.eliminar(m.id)
      avisos.exito('Médico eliminado')
      recargar()
    } catch (e) {
      avisos.error(e.message)
    }
  }

  return (
    <>
      <Encabezado
        ilustracion={<IlustracionMedicos />}
        titulo="Médicos"
        descripcion="Profesionales registrados"
        acciones={<Link className="btn btn-primario" to="/medicos/nuevo">Nuevo médico</Link>}
      />
      <Estado cargando={cargando} error={error}>
        <div className="tabla-caja">
          <table>
            <thead>
              <tr><th>Apellido</th><th>Nombre</th><th>Documento</th><th className="der">Acciones</th></tr>
            </thead>
            <tbody>
              {datos?.map((m) => (
                <tr key={m.id}>
                  <td>{m.apellido}</td>
                  <td>{m.nombre}</td>
                  <td>{m.documento}</td>
                  <td className="der"><BotonesFila editarA={`/medicos/${m.id}/editar`} onEliminar={() => eliminar(m)} /></td>
                </tr>
              ))}
              {datos?.length === 0 && <tr><td colSpan="4" className="vacio">Todavía no hay médicos</td></tr>}
            </tbody>
          </table>
        </div>
      </Estado>
    </>
  )
}