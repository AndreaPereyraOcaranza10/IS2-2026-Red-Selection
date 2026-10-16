package redselection.clienteJava.builder;

import java.time.LocalDate;

public interface HistoriaClinicaBuilder {
    void reiniciar();
    void construirId(String id);
    void construirPaciente(String pacienteId);
    void construirUsuario(String usuarioId);
    void agregarDetalle(LocalDate fecha, String texto, String medicoId);
}