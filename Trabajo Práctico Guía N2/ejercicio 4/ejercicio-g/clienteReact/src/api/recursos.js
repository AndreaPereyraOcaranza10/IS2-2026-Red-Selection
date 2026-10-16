import { http } from './http.js'

function crearRecurso(nombre) {
  const base = '/' + nombre
  return {
    listar: () => http(base),
    obtener: (id) => http(`${base}/${id}`),
    crear: (dto) => http(base, { method: 'POST', body: dto }),
    actualizar: (id, dto) => http(`${base}/${id}`, { method: 'PUT', body: dto }),
    eliminar: (id) => http(`${base}/${id}`, { method: 'DELETE' }),
  }
}

export const medicosApi = crearRecurso('medicos')
export const usuariosApi = crearRecurso('usuarios')
export const pacientesApi = crearRecurso('pacientes')

export const historiasApi = {
  ...crearRecurso('historias-clinicas'),
  listarPorPaciente: (pacienteId) => http(`/historias-clinicas/paciente/${pacienteId}`),
}