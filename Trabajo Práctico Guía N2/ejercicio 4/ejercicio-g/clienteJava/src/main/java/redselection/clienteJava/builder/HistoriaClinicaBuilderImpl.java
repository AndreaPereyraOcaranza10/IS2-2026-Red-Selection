package redselection.clienteJava.builder;

import redselection.clienteJava.dto.DetalleHistoriaClinicaDTO;
import redselection.clienteJava.dto.HistoriaClinicaDTO;
import redselection.clienteJava.dto.MedicoDTO;
import redselection.clienteJava.dto.PacienteDTO;
import redselection.clienteJava.dto.UsuarioDTO;

import java.time.LocalDate;

public class HistoriaClinicaBuilderImpl implements HistoriaClinicaBuilder {

    private HistoriaClinicaDTO historia;

    public HistoriaClinicaBuilderImpl() {
        reiniciar();
    }

    @Override
    public void reiniciar() {
        historia = new HistoriaClinicaDTO();
    }

    @Override
    public void construirId(String id) {
        historia.setId(id);
    }

    // solo manda el id para vincularlo al servidor con la referencia
    @Override
    public void construirPaciente(String pacienteId) {
        PacienteDTO paciente = new PacienteDTO();
        paciente.setId(pacienteId);
        historia.setPaciente(paciente);
    }

    @Override
    public void construirUsuario(String usuarioId) {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(usuarioId);
        historia.setUsuario(usuario);
    }

    @Override
    public void agregarDetalle(LocalDate fecha, String texto, String medicoId) {
        MedicoDTO medico = new MedicoDTO();
        medico.setId(medicoId);

        DetalleHistoriaClinicaDTO detalle = new DetalleHistoriaClinicaDTO();
        detalle.setFechaHistoria(fecha);
        detalle.setDetalleHistoria(texto);
        detalle.setMedico(medico);

        historia.getDetalles().add(detalle);
    }

    public HistoriaClinicaDTO obtenerResultado() {
        HistoriaClinicaDTO resultado = historia;
        reiniciar();
        return resultado;
    }
}