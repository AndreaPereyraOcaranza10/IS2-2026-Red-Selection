package redselection.ejercicio_c.patrones.fabrica;

import org.springframework.stereotype.Component;
import redselection.ejercicio_c.dto.LibroSolicitud;
import redselection.ejercicio_c.entities.LibroDigital;

import java.util.List;

// Creador concreto: fabrica libros digitales
@Component
public class LibroDigitalFactory extends LibroFactory<LibroDigital> {

    private static final List<String> FORMATOS_VALIDOS = List.of("PDF", "EPUB", "MOBI");

    @Override
    public TipoLibro getTipo() {
        return TipoLibro.DIGITAL;
    }

    @Override
    public LibroDigital nuevaInstancia() {
        return new LibroDigital();
    }

    @Override
    protected void completarDatosPropios(LibroDigital libro, LibroSolicitud solicitud) {
        String formato = solicitud.getFormato();
        if (formato == null || formato.isBlank()) {
            throw new IllegalArgumentException("El formato del libro digital es obligatorio (PDF, EPUB o MOBI).");
        }
        formato = formato.trim().toUpperCase();
        if (!FORMATOS_VALIDOS.contains(formato)) {
            throw new IllegalArgumentException("Formato inválido: " + formato + ". Los válidos son PDF, EPUB y MOBI.");
        }
        libro.setFormato(formato);

        Double tamanioMb = solicitud.getTamanioMb();
        if (tamanioMb != null && tamanioMb <= 0) {
            throw new IllegalArgumentException("El tamaño del archivo debe ser mayor a 0 MB.");
        }
        libro.setTamanioMb(tamanioMb);

        String url = solicitud.getUrlDescarga();
        if (url == null || url.isBlank()) {
            libro.setUrlDescarga(null);
        } else {
            libro.setUrlDescarga(url.trim());
        }
    }
}