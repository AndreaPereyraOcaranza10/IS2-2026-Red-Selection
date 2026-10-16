import { Link } from 'react-router-dom'
import { pacientesApi } from '../api/recursos.js'
import { useCargar } from '../utils/useCargar.js'
import { useAvisos } from '../components/Avisos.jsx'
import Encabezado from '../components/Encabezado.jsx'
import { IlustracionPacientes } from '../components/Ilustraciones.jsx'
import Estado from '../components/Estado.jsx'
import BotonesFila from '../components/BotonesFila.jsx'
import Avatar from '../components/Avatar.jsx'

export default function PacientesLista() {
  const { datos, cargando, error, recargar } = useCargar(() => pacientesApi.listar())
  const avisos = useAvisos()

  async function eliminar(p) {
    if (!window.confirm(`¿Eliminar al paciente ${p.apellido}, ${p.nombre}?`)) return
    try {
      await pacientesApi.eliminar(p.id)
      avisos.exito('Paciente eliminado')
      recargar()
    } catch (e) {
      avisos.error(e.message)
    }
  }

  return (
    <>
      <Encabezado
        ilustracion={<IlustracionPacientes />}
        titulo="Pacientes"
        descripcion="Pacientes registrados"
        acciones={<Link className="btn btn-primario" to="/pacientes/nuevo">Nuevo paciente</Link>}
      />
      <Estado cargando={cargando} error={error}>
        <div className="tabla-caja">
          <table>
            <thead>
              <tr><th>Foto</th><th>Apellido</th><th>Nombre</th><th>Documento</th><th className="der">Acciones</th></tr>
            </thead>
            <tbody>
              {datos?.map((p) => (
                <tr key={p.id}>
                  <td><Avatar foto={p.fotoPaciente} /></td>
                  <td>{p.apellido}</td>
                  <td>{p.nombre}</td>
                  <td>{p.documento}</td>
                  <td className="der"><BotonesFila editarA={`/pacientes/${p.id}/editar`} onEliminar={() => eliminar(p)} /></td>
                </tr>
              ))}
              {datos?.length === 0 && <tr><td colSpan="5" className="vacio">Todavía no hay pacientes</td></tr>}
            </tbody>
          </table>
        </div>
      </Estado>
    </>
  )
}