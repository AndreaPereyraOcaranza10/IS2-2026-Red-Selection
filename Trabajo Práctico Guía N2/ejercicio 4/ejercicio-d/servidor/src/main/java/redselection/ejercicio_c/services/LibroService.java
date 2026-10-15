package redselection.ejercicio_c.services;

import redselection.ejercicio_c.dto.LibroSolicitud;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.patrones.fabrica.TipoLibro;

import java.util.List;

public interface LibroService extends BaseService<Libro, Long> {
    List<Libro> listarPorAutor(Long autorId);

    // Libros que no están asignados a ninguna persona
    List<Libro> listarDisponibles();

    // Busca libros por título, género o autor (cada criterio lo resuelve una estrategia)
    List<Libro> buscar(String criterio, String valor);

    // Crea un libro del tipo pedido (físico o digital) usando la fábrica correspondiente
    Libro crearLibro(TipoLibro tipo, LibroSolicitud solicitud) throws Exception;
}