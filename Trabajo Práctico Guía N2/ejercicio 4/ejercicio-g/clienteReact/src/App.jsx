import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout.jsx'
import Inicio from './pages/Inicio.jsx'
import MedicosLista from './pages/MedicosLista.jsx'
import MedicoForm from './pages/MedicoForm.jsx'
import UsuariosLista from './pages/UsuariosLista.jsx'
import UsuarioForm from './pages/UsuarioForm.jsx'
import PacientesLista from './pages/PacientesLista.jsx'
import PacienteForm from './pages/PacienteForm.jsx'
import HistoriasLista from './pages/HistoriasLista.jsx'
import HistoriaForm from './pages/HistoriaForm.jsx'
import HistoriaDetalle from './pages/HistoriaDetalle.jsx'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Inicio />} />

        <Route path="/medicos" element={<MedicosLista />} />
        <Route path="/medicos/nuevo" element={<MedicoForm />} />
        <Route path="/medicos/:id/editar" element={<MedicoForm />} />

        <Route path="/usuarios" element={<UsuariosLista />} />
        <Route path="/usuarios/nuevo" element={<UsuarioForm />} />
        <Route path="/usuarios/:id/editar" element={<UsuarioForm />} />

        <Route path="/pacientes" element={<PacientesLista />} />
        <Route path="/pacientes/nuevo" element={<PacienteForm />} />
        <Route path="/pacientes/:id/editar" element={<PacienteForm />} />

        <Route path="/historias" element={<HistoriasLista />} />
        <Route path="/historias/nuevo" element={<HistoriaForm />} />
        <Route path="/historias/:id/editar" element={<HistoriaForm />} />
        <Route path="/historias/:id" element={<HistoriaDetalle />} />

        <Route path="*" element={<p className="vacio">Esa página no existe.</p>} />
      </Route>
    </Routes>
  )
}