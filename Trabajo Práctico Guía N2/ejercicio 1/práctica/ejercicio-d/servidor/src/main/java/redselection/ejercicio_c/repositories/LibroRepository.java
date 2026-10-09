package redselection.ejercicio_c.repositories;

import org.springframework.stereotype.Repository;
import redselection.ejercicio_c.entities.Libro;

@Repository
public interface LibroRepository extends BaseRepository<Libro, Long>{
}
