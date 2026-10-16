package redselection.servidor.patrones.builder;

import redselection.servidor.model.DetalleHistoriaClinica;
import redselection.servidor.model.HistoriaClinica;
import redselection.servidor.model.Medico;
import redselection.servidor.model.Paciente;
import redselection.servidor.model.Usuario;

import java.time.LocalDate;

/** Define los pasos para construir una entidad HistoriaClinica en el servidor. */
public interface HistoriaClinicaBuilder {
    void reiniciar();
    void construirId(String id);
    void construirPaciente(Paciente paciente);
    void construirUsuario(Usuario usuario);
    void agregarDetalle(LocalDate fecha, String texto, Medico medico);
    void agregarDetalle(DetalleHistoriaClinica detalle);
    HistoriaClinica obtenerResultado();
}
