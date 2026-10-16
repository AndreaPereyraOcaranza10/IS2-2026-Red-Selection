export function armarHistoria(original, detalles) {
  return {
    id: original.id,
    paciente: { id: original.paciente.id },
    usuario: { id: original.usuario.id },
    detalles: detalles.map((d) => ({
      fechaHistoria: d.fechaHistoria,
      detalleHistoria: d.detalleHistoria,
      medico: { id: d.medico.id },
    })),
  }
}