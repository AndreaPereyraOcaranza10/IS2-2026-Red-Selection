package redselection.servidor.patrones.builder;

import redselection.servidor.model.DetalleHistoriaClinica;
import redselection.servidor.model.HistoriaClinica;
import redselection.servidor.model.Medico;
import redselection.servidor.model.Paciente;
import redselection.servidor.model.Usuario;

import java.time.LocalDate;
import java.util.ArrayList;

/** Construye una HistoriaClinica paso a paso y mantiene la relación con sus detalles. */
public class HistoriaClinicaBuilderImpl implements HistoriaClinicaBuilder {

    private HistoriaClinica historia;

    public HistoriaClinicaBuilderImpl() {
        reiniciar();
    }

    @Override
    public void reiniciar() {
        historia = new HistoriaClinica();
        historia.setDetalles(new ArrayList<>());
    }

    @Override
    public void construirId(String id) {
        historia.setId(id);
    }

    @Override
    public void construirPaciente(Paciente paciente) {
        historia.setPaciente(paciente);
    }

    @Override
    public void construirUsuario(Usuario usuario) {
        historia.setUsuario(usuario);
    }

    @Override
    public void agregarDetalle(LocalDate fecha, String texto, Medico medico) {
        DetalleHistoriaClinica detalle = new DetalleHistoriaClinica();
        detalle.setFechaHistoria(fecha);
        detalle.setDetalleHistoria(texto);
        detalle.setMedico(medico);
        historia.getDetalles().add(detalle);
    }

    @Override
    public void agregarDetalle(DetalleHistoriaClinica origen) {
        agregarDetalle(origen.getFechaHistoria(), origen.getDetalleHistoria(), origen.getMedico());
    }

    @Override
    public HistoriaClinica obtenerResultado() {
        HistoriaClinica resultado = historia;
        reiniciar();
        return resultado;
    }
}
