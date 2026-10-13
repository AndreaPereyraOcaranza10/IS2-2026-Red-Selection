package redselection.ejercicio_c.services;

import redselection.ejercicio_c.entities.Libro;

import java.util.List;

public interface LibroService extends BaseService<Libro, Long> {
    List<Libro> listarPorAutor(Long autorId);
}