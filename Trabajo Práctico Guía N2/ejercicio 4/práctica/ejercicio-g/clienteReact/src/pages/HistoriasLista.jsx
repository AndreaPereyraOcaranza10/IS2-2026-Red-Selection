import { Link, useSearchParams } from 'react-router-dom'
import { historiasApi, pacientesApi } from '../api/recursos.js'
import { useCargar } from '../utils/useCargar.js'
import { useAvisos } from '../components/Avisos.jsx'
import Encabezado from '../components/Encabezado.jsx'
import { IlustracionHistorias } from '../components/Ilustraciones.jsx'
import Estado from '../components/Estado.jsx'
import Avatar from '../components/Avatar.jsx'


export default function HistoriasLista() {
  const [params, setParams] = useSearchParams()
  const pacienteId = params.get('pacienteId') ?? ''
  const avisos = useAvisos()

  const pacientes = useCargar(() => pacientesApi.listar())
  const historias = useCargar(
    () => (pacienteId ? historiasApi.listarPorPaciente(pacienteId) : historiasApi.listar()),
    [pacienteId],
  )

  function filtrar(e) {
    const valor = e.target.value
    setParams(valor ? { pacienteId: valor } : {})
  }

  async function eliminar(h) {
    if (!window.confirm('¿Eliminar esta historia clínica?')) return
    try {
      await historiasApi.eliminar(h.id)
      avisos.exito('Historia clínica eliminada')
      historias.recargar()
    } catch (e) {
      avisos.error(e.message)
    }
  }

  return (
    <>
      <Encabezado
        ilustracion={<IlustracionHistorias />}
        titulo="Historias clínicas"
        descripcion="Consultas registradas por paciente"
        acciones={<Link className="btn btn-primario" to="/historias/nuevo">Nueva historia</Link>}
      />

      <div className="filtro">
        <label>Filtrar por paciente
          <select value={pacienteId} onChange={filtrar}>
            <option value="">Todos</option>
            {pacientes.datos?.map((p) => (
              <option key={p.id} value={p.id}>{p.apellido}, {p.nombre}</option>
            ))}
          </select>
        </label>
      </div>

      <Estado cargando={historias.cargando} error={historias.error}>
        <div className="tabla-caja">
          <table>
            <thead>
              <tr><th>Paciente</th><th>Documento</th><th>Cargada por</th><th>Consultas</th><th className="der">Acciones</th></tr>
            </thead>
            <tbody>
              {historias.datos?.map((h) => (
                <tr key={h.id}>
                  <td>
                    <div className="celda-persona">
                      <Avatar foto={h.paciente?.fotoPaciente} />
                      {h.paciente?.apellido}, {h.paciente?.nombre}
                    </div>
                  </td>
                  <td>{h.paciente?.documento}</td>
                  <td>{h.usuario?.nombreUsuario}</td>
                  <td>{h.detalles?.length ?? 0}</td>
                  <td className="der">
                    <div className="acciones">
                      <Link className="btn btn-chico btn-primario" to={`/historias/${h.id}`}>Ver</Link>
                      <Link className="btn btn-chico btn-borde" to={`/historias/${h.id}/editar`}>Editar</Link>
                      <button className="btn btn-chico btn-peligro" onClick={() => eliminar(h)}>Eliminar</button>
                    </div>
                  </td>
                </tr>
              ))}
              {historias.datos?.length === 0 && <tr><td colSpan="5" className="vacio">No hay historias clínicas</td></tr>}
            </tbody>
          </table>
        </div>
      </Estado>
    </>
  )
}