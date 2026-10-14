package redselection.ejercicio_c.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import redselection.ejercicio_c.entities.Libro;

import java.util.List;

@Repository
public interface LibroRepository extends BaseRepository<Libro, Long> {

    // Libros del catálogo que no están asignados a ninguna persona
    @Query("SELECT l FROM Libro l WHERE l.id NOT IN (SELECT lb.id FROM Persona p JOIN p.libros lb)")
    List<Libro> findDisponibles();

}