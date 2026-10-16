package redselection.ejercicio_c.patrones.fabrica;

import redselection.ejercicio_c.dto.LibroSolicitud;
import redselection.ejercicio_c.entities.Libro;

import java.util.ArrayList;

// Creador abstracto del patrón Factory Method.
// Define el "esqueleto" para crear un libro (crearLibro) y deja que cada fábrica concreta decida
// qué clase instanciar (nuevaInstancia) y cómo completar sus datos propios (completarDatosPropios).
public abstract class LibroFactory<L extends Libro> {

    // Qué tipo de libro produce esta fábrica
    public abstract TipoLibro getTipo();

    // FACTORY METHOD: cada fábrica concreta devuelve su propio producto
    public abstract L nuevaInstancia();

    // Datos y validaciones que solo tiene este tipo de libro
    protected abstract void completarDatosPropios(L libro, LibroSolicitud solicitud);

    // Lo que no cambia entre tipos: validar lo común, instanciar, cargar lo común
    public final L crearLibro(LibroSolicitud solicitud) {
        validarDatosComunes(solicitud);

        L libro = nuevaInstancia();
        libro.setTitulo(solicitud.getTitulo().trim());
        libro.setFechaPublicacion(solicitud.getFechaPublicacion());
        libro.setGenero(solicitud.getGenero().trim());
        libro.setPaginas(solicitud.getPaginas());

        if (solicitud.getAutores() == null) {
            libro.setAutores(new ArrayList<>());
        } else {
            libro.setAutores(new ArrayList<>(solicitud.getAutores()));
        }

        completarDatosPropios(libro, solicitud);
        return libro;
    }

    private void validarDatosComunes(LibroSolicitud solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("Faltan los datos del libro.");
        }
        if (solicitud.getTitulo() == null || solicitud.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio.");
        }
        if (solicitud.getGenero() == null || solicitud.getGenero().isBlank()) {
            throw new IllegalArgumentException("El género es obligatorio.");
        }
        if (solicitud.getPaginas() == null || solicitud.getPaginas() < 1) {
            throw new IllegalArgumentException("La cantidad de páginas debe ser mayor a 0.");
        }
    }
}