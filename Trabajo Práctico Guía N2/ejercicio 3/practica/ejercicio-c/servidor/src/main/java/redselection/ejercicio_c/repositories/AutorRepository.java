package redselection.ejercicio_c.repositories;

import org.springframework.stereotype.Repository;
import redselection.ejercicio_c.entities.Autor;

@Repository
public interface AutorRepository extends BaseRepository<Autor, Long> {
}
