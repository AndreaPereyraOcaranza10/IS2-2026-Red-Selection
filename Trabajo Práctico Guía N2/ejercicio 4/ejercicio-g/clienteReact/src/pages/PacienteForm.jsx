import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { pacientesApi } from '../api/recursos.js'
import { useAvisos } from '../components/Avisos.jsx'
import Encabezado from '../components/Encabezado.jsx'
import { IlustracionPacientes } from '../components/Ilustraciones.jsx'
import Avatar from '../components/Avatar.jsx'
import { archivoAFoto } from '../utils/fotos.js'

const VACIO = { nombre: '', apellido: '', documento: '', fotoPaciente: null }
const MAX_FOTO = 5 * 1024 * 1024 // mismo límite que el cliente Java (5MB)

export default function PacienteForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const avisos = useAvisos()
  const [paciente, setPaciente] = useState(VACIO)
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  useEffect(() => {
    if (!id) return
    pacientesApi.obtener(id).then(setPaciente).catch((e) => setError(e.message))
  }, [id])

  function cambiar(e) {
    setPaciente({ ...paciente, [e.target.name]: e.target.value })
  }

  async function elegirFoto(e) {
    const archivo = e.target.files[0]
    if (!archivo) return
    if (!archivo.type.startsWith('image/')) {
      setError('El archivo tiene que ser una imagen')
      return
    }
    if (archivo.size > MAX_FOTO) {
      setError('La foto supera los 5MB')
      return
    }
    setError('')
    try {
      const nueva = await archivoAFoto(archivo)
      setPaciente({ ...paciente, fotoPaciente: { ...paciente.fotoPaciente, ...nueva } })
    } catch (err) {
      setError(err.message)
    }
  }

  async function guardar(e) {
    e.preventDefault()
    setGuardando(true)
    setError('')
    try {
      if (id) {
        await pacientesApi.actualizar(id, paciente)
        avisos.exito('Paciente actualizado correctamente')
      } else {
        await pacientesApi.crear(paciente)
        avisos.exito('Paciente creado correctamente')
      }
      navigate('/pacientes')
    } catch (err) {
      setError(err.message)
      setGuardando(false)
    }
  }

  return (
    <>
      <Encabezado titulo={id ? 'Editar paciente' : 'Nuevo paciente'} />
            <Encabezado ilustracion={<IlustracionPacientes />} titulo={id ? 'Editar paciente' : 'Nuevo paciente'} />
      <form className="tarjeta formulario" onSubmit={guardar}>
        {error && <div className="aviso aviso-error">{error}</div>}
        <label>Nombre
          <input name="nombre" value={paciente.nombre ?? ''} onChange={cambiar} />
        </label>
        <label>Apellido
          <input name="apellido" value={paciente.apellido ?? ''} onChange={cambiar} />
        </label>
        <label>Documento
          <input name="documento" value={paciente.documento ?? ''} onChange={cambiar} />
        </label>
        <div className="foto-fila">
          <Avatar foto={paciente.fotoPaciente} tam={72} />
          <label>Foto (opcional)
            <input type="file" accept="image/*" onChange={elegirFoto} />
          </label>
        </div>
        <div className="acciones">
          <button className="btn btn-primario" disabled={guardando}>Guardar</button>
          <Link className="btn btn-borde" to="/pacientes">Cancelar</Link>
        </div>
      </form>
    </>
  )
}