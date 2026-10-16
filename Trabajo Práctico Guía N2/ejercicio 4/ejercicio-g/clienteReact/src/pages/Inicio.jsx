import { Link } from 'react-router-dom'
import Encabezado from '../components/Encabezado.jsx'
import { IlustracionClinica, IlustracionPacientes, IlustracionMedicos, IlustracionUsuarios, IlustracionHistorias } from '../components/Ilustraciones.jsx'

const TARJETAS = [
  { a: '/pacientes', titulo: 'Pacientes', texto: 'Alta, edición y foto de cada paciente.', Ilu: IlustracionPacientes },
  { a: '/medicos', titulo: 'Médicos', texto: 'Profesionales que atienden las consultas.', Ilu: IlustracionMedicos },
  { a: '/usuarios', titulo: 'Usuarios', texto: 'Quienes cargan las historias clínicas.', Ilu: IlustracionUsuarios },
  { a: '/historias', titulo: 'Historias clínicas', texto: 'Consultas registradas por paciente.', Ilu: IlustracionHistorias },
]

export default function Inicio() {
  return (
    <>
      <Encabezado titulo="Consultorio médico" descripcion="Gestión de pacientes, médicos y historias clínicas" ilustracion={<IlustracionClinica />} />
      <div className="grilla">
        {TARJETAS.map((t) => (
          <Link key={t.a} to={t.a} className="tarjeta tarjeta-link">
            <div className="tarjeta-ilu"><t.Ilu /></div>
            <h3>{t.titulo}</h3>
            <p>{t.texto}</p>
          </Link>
        ))}
      </div>
    </>
  )
}