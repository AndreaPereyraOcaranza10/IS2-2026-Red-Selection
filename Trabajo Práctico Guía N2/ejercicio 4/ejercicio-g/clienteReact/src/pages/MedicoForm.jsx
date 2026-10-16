import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { medicosApi } from '../api/recursos.js'
import { useAvisos } from '../components/Avisos.jsx'
import Encabezado from '../components/Encabezado.jsx'
import { IlustracionMedicos } from '../components/Ilustraciones.jsx'

const VACIO = { nombre: '', apellido: '', documento: '' }

export default function MedicoForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const avisos = useAvisos()
  const [medico, setMedico] = useState(VACIO)
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  useEffect(() => {
    if (!id) return
    medicosApi.obtener(id).then(setMedico).catch((e) => setError(e.message))
  }, [id])

  function cambiar(e) {
    setMedico({ ...medico, [e.target.name]: e.target.value })
  }

  async function guardar(e) {
    e.preventDefault()
    setGuardando(true)
    setError('')
    try {
      if (id) {
        await medicosApi.actualizar(id, medico)
        avisos.exito('Médico actualizado correctamente')
      } else {
        await medicosApi.crear(medico)
        avisos.exito('Médico creado correctamente')
      }
      navigate('/medicos')
    } catch (err) {
      setError(err.message)
      setGuardando(false)
    }
  }

  return (
    <>
      <Encabezado titulo={id ? 'Editar médico' : 'Nuevo médico'} />
      <Encabezado ilustracion={<IlustracionMedicos />} titulo={id ? 'Editar médico' : 'Nuevo médico'} />
      <form className="tarjeta formulario" onSubmit={guardar}>
        {error && <div className="aviso aviso-error">{error}</div>}
        <label>Nombre
          <input name="nombre" value={medico.nombre ?? ''} onChange={cambiar} />
        </label>
        <label>Apellido
          <input name="apellido" value={medico.apellido ?? ''} onChange={cambiar} />
        </label>
        <label>Documento
          <input name="documento" value={medico.documento ?? ''} onChange={cambiar} />
        </label>
        <div className="acciones">
          <button className="btn btn-primario" disabled={guardando}>Guardar</button>
          <Link className="btn btn-borde" to="/medicos">Cancelar</Link>
        </div>
      </form>
    </>
  )
}