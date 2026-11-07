package redSelection.dulceMayor.repository;

import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.SubCategoria;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubCategoriaRepository extends BaseRepository<SubCategoria> {
    List<SubCategoria> findByCategoriaIdAndEliminadoFalse(String categoriaId);
    Optional<SubCategoria> findByNombreIgnoreCaseAndCategoriaId(String nombre, String categoriaId);
}

