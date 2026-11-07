package redSelection.dulceMayor.repository;

import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.Categoria;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends BaseRepository<Categoria> {
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}