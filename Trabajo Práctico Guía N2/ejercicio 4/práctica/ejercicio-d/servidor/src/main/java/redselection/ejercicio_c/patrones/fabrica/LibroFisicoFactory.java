package redselection.ejercicio_c.patrones.fabrica;

import org.springframework.stereotype.Component;
import redselection.ejercicio_c.dto.LibroSolicitud;
import redselection.ejercicio_c.entities.LibroFisico;

// Creador concreto: fabrica libros físicos
@Component
public class LibroFisicoFactory extends LibroFactory<LibroFisico> {

    @Override
    public TipoLibro getTipo() {
        return TipoLibro.FISICO;
    }

    @Override
    public LibroFisico nuevaInstancia() {
        return new LibroFisico();
    }

    @Override
    protected void completarDatosPropios(LibroFisico libro, LibroSolicitud solicitud) {
        String ubicacion = solicitud.getUbicacion();
        if (ubicacion == null || ubicacion.isBlank()) {
            libro.setUbicacion(null);
        } else {
            libro.setUbicacion(ubicacion.trim());
        }
    }
}