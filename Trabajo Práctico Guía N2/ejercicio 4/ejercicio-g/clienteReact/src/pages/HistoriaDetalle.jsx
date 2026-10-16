import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { historiasApi, medicosApi } from '../api/recursos.js'
import { useCargar } from '../utils/useCargar.js'
import { useAvisos } from '../components/Avisos.jsx'
import Encabezado from '../components/Encabezado.jsx'
import { IlustracionHistorias } from '../components/Ilustraciones.jsx'
import Estado from '../components/Estado.jsx'
import Avatar from '../components/Avatar.jsx'
import { armarHistoria } from '../utils/historias.js'
import { formatearFecha, hoyISO } from '../utils/fechas.js'

const NUEVA_CONSULTA = () => ({ fechaHistoria: hoyISO(), medicoId: '', detalleHistoria: '' })

export default function HistoriaDetalle() {
  const { id } = useParams()
  const avisos = useAvisos()

  const historia = useCargar(() => historiasApi.obtener(id), [id])
  const medicos = useCargar(() => medicosApi.listar())

  const [consulta, setConsulta] = useState(NUEVA_CONSULTA)
  const [guardando, setGuardando] = useState(false)

  function cambiar(e) {
    setConsulta({ ...consulta, [e.target.name]: e.target.value })
  }

    // Agregar y quitar consultas: una sola llamada a la fachada del servidor.
  async function agregar(e) {
    e.preventDefault()
    setGuardando(true)
    try {
      await historiasApi.agregarConsulta(id, {
        fechaHistoria: consulta.fechaHistoria,
        detalleHistoria: consulta.detalleHistoria,
        medicoId: consulta.medicoId,
      })
      avisos.exito('Consulta agregada')
      setConsulta(NUEVA_CONSULTA())
      historia.recargar()
    } catch (err) {
      avisos.error(err.message)
    } finally {
      setGuardando(false)
    }
  }

  async function quitar(detalleId) {
    if (!window.confirm('¿Quitar esta consulta?')) return
    try {
      await historiasApi.quitarConsulta(id, detalleId)
      avisos.exito('Consulta eliminada')
      historia.recargar()
    } catch (err) {
      avisos.error(err.message)
    }
  }

  const h = historia.datos

  return (
    <>
      <Encabezado titulo="Historia clínica" descripcion="Consultas del paciente" />
      <Encabezado ilustracion={<IlustracionHistorias />} titulo="Historia clínica" descripcion="Consultas del paciente" />
      <Estado cargando={historia.cargando} error={historia.error}>
        {h && (
          <>
            <div className="tarjeta ficha">
              <Avatar foto={h.paciente.fotoPaciente} tam={72} />
              <div>
                <h2>{h.paciente.apellido}, {h.paciente.nombre}</h2>
                <p className="suave">
                  Documento: {h.paciente.documento} · Cargada por: {h.usuario.nombreUsuario}
                </p>
              </div>
              <div className="acciones ficha-acciones">
                <Link className="btn btn-chico btn-borde" to={`/historias/${h.id}/editar`}>Editar</Link>
                <Link className="btn btn-chico btn-primario" to="/historias">Volver</Link>
              </div>
            </div>

            <h3>Consultas</h3>
            <div className="tabla-caja">
              <table>
                <thead>
                  <tr><th>Fecha</th><th>Médico</th><th>Detalle</th><th className="der">Quitar</th></tr>
                </thead>
                <tbody>
                  {h.detalles.map((d) => (
                    <tr key={d.id}>
                      <td>{formatearFecha(d.fechaHistoria)}</td>
                      <td>{d.medico.apellido}, {d.medico.nombre}</td>
                      <td>{d.detalleHistoria}</td>
                      <td className="der">
                        <button className="btn btn-chico btn-peligro" onClick={() => quitar(d.id)}>Quitar</button>
                      </td>
                    </tr>
                  ))}
                  {h.detalles.length === 0 && (
                    <tr><td colSpan="4" className="vacio">Todavía no hay consultas registradas</td></tr>
                  )}
                </tbody>
              </table>
            </div>

            <h3>Agregar consulta</h3>
            <form className="tarjeta formulario" onSubmit={agregar}>
              <div className="fila-doble">
                <label>Fecha
                  <input type="date" name="fechaHistoria" value={consulta.fechaHistoria} onChange={cambiar} required />
                </label>
                <label>Médico
                  <select name="medicoId" value={consulta.medicoId} onChange={cambiar} required>
                    <option value="">Seleccioná un médico…</option>
                    {medicos.datos?.map((m) => (
                      <option key={m.id} value={m.id}>{m.apellido}, {m.nombre}</option>
                    ))}
                  </select>
                </label>
              </div>
              <label>Detalle
                <textarea name="detalleHistoria" rows="3" value={consulta.detalleHistoria} onChange={cambiar} required />
              </label>
              <div className="acciones">
                <button className="btn btn-primario" disabled={guardando}>Agregar consulta</button>
              </div>
            </form>
          </>
        )}
      </Estado>
    </>
  )
}