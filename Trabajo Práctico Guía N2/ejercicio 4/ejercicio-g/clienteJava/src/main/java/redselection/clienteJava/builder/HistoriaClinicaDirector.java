package redselection.clienteJava.builder;

import redselection.clienteJava.dto.DetalleHistoriaClinicaDTO;
import redselection.clienteJava.dto.HistoriaClinicaDTO;

import java.time.LocalDate;

public class HistoriaClinicaDirector {

    private HistoriaClinicaBuilder builder;

    public HistoriaClinicaDirector(HistoriaClinicaBuilder builder) {
        this.builder = builder;
    }

    public void cambiarBuilder(HistoriaClinicaBuilder builder) {
        this.builder = builder;
    }


    public void construirNueva(String pacienteId, String usuarioId) {
        builder.reiniciar();
        builder.construirPaciente(pacienteId);
        builder.construirUsuario(usuarioId);
    }


    public void construirConNuevaCabecera(HistoriaClinicaDTO original, String pacienteId, String usuarioId) {
        builder.reiniciar();
        builder.construirId(original.getId());
        builder.construirPaciente(pacienteId);
        builder.construirUsuario(usuarioId);
        copiarDetalles(original, null);
    }


    public void construirConNuevaConsulta(HistoriaClinicaDTO original, LocalDate fecha,
                                          String texto, String medicoId) {
        builder.reiniciar();
        builder.construirId(original.getId());
        builder.construirPaciente(original.getPaciente().getId());
        builder.construirUsuario(original.getUsuario().getId());
        copiarDetalles(original, null);
        builder.agregarDetalle(fecha, texto, medicoId);
    }


    public void construirSinConsulta(HistoriaClinicaDTO original, String detalleIdAQuitar) {
        builder.reiniciar();
        builder.construirId(original.getId());
        builder.construirPaciente(original.getPaciente().getId());
        builder.construirUsuario(original.getUsuario().getId());
        copiarDetalles(original, detalleIdAQuitar);
    }

    private void copiarDetalles(HistoriaClinicaDTO original, String idAOmitir) {
        for (DetalleHistoriaClinicaDTO d : original.getDetalles()) {
            if (idAOmitir != null && idAOmitir.equals(d.getId())) continue;
            builder.agregarDetalle(d.getFechaHistoria(), d.getDetalleHistoria(), d.getMedico().getId());
        }
    }
}