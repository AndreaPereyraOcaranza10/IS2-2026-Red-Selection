import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { historiasApi, pacientesApi, usuariosApi } from '../api/recursos.js'
import { useCargar } from '../utils/useCargar.js'
import { useAvisos } from '../components/Avisos.jsx'
import Encabezado from '../components/Encabezado.jsx'
import { IlustracionHistorias } from '../components/Ilustraciones.jsx'
import Estado from '../components/Estado.jsx'
import { armarHistoria } from '../utils/historias.js'

export default function HistoriaForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const avisos = useAvisos()

  const combos = useCargar(async () => {
    const [pacientes, usuarios] = await Promise.all([pacientesApi.listar(), usuariosApi.listar()])
    return { pacientes, usuarios }
  })

  const [existente, setExistente] = useState(null)
  const [pacienteId, setPacienteId] = useState('')
  const [usuarioId, setUsuarioId] = useState('')
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  useEffect(() => {
    if (!id) return
    historiasApi
      .obtener(id)
      .then((h) => {
        setExistente(h)
        setPacienteId(h.paciente.id)
        setUsuarioId(h.usuario.id)
      })
      .catch((e) => setError(e.message))
  }, [id])

  async function guardar(e) {
    e.preventDefault()
    setGuardando(true)
    setError('')
    try {
      if (!id) {
        const creada = await historiasApi.crear({
          paciente: { id: pacienteId },
          usuario: { id: usuarioId },
          detalles: [],
        })
        avisos.exito('Historia clínica creada. Ahora podés agregar consultas.')
        navigate(`/historias/${creada.id}`)
      } else {
        const base = armarHistoria(existente, existente.detalles)
        base.paciente = { id: pacienteId }
        base.usuario = { id: usuarioId }
        await historiasApi.actualizar(id, base)
        avisos.exito('Historia clínica actualizada')
        navigate(`/historias/${id}`)
      }
    } catch (err) {
      setError(err.message)
      setGuardando(false)
    }
  }

  return (
    <>
      <Encabezado titulo={id ? 'Editar historia clínica' : 'Nueva historia clínica'} />
      <Encabezado ilustracion={<IlustracionHistorias />} titulo={id ? 'Editar historia clínica' : 'Nueva historia clínica'} />
      <Estado cargando={combos.cargando} error={combos.error}>
        <form className="tarjeta formulario" onSubmit={guardar}>
          {error && <div className="aviso aviso-error">{error}</div>}
          <label>Paciente
            <select value={pacienteId} onChange={(e) => setPacienteId(e.target.value)} required>
              <option value="">Seleccioná un paciente…</option>
              {combos.datos?.pacientes.map((p) => (
                <option key={p.id} value={p.id}>{p.apellido}, {p.nombre} ({p.documento})</option>
              ))}
            </select>
          </label>
          <label>Usuario que la carga
            <select value={usuarioId} onChange={(e) => setUsuarioId(e.target.value)} required>
              <option value="">Seleccioná un usuario…</option>
              {combos.datos?.usuarios.map((u) => (
                <option key={u.id} value={u.id}>{u.nombreUsuario}</option>
              ))}
            </select>
          </label>
          <div className="acciones">
            <button className="btn btn-primario" disabled={guardando}>Guardar</button>
            <Link className="btn btn-borde" to="/historias">Cancelar</Link>
          </div>
        </form>
      </Estado>
    </>
  )
}