package redselection.servidor.patrones.builder;

import redselection.servidor.model.DetalleHistoriaClinica;
import redselection.servidor.model.HistoriaClinica;

/** Coordina las recetas de construcción de historias clínicas en el servidor. */
public class HistoriaClinicaDirector {

    private final HistoriaClinicaBuilder builder;

    public HistoriaClinicaDirector(HistoriaClinicaBuilder builder) {
        this.builder = builder;
    }

    /** Construye una historia a partir de los datos recibidos por la API. */
    public HistoriaClinica construirDesde(HistoriaClinica origen) {
        builder.reiniciar();
        builder.construirId(origen.getId());
        builder.construirPaciente(origen.getPaciente());
        builder.construirUsuario(origen.getUsuario());
        if (origen.getDetalles() != null) {
            for (DetalleHistoriaClinica detalle : origen.getDetalles()) {
                builder.agregarDetalle(detalle);
            }
        }
        return builder.obtenerResultado();
    }
}
