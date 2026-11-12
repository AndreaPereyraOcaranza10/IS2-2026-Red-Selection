package redSelection.dulceMayor.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.model.Producto;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends BaseRepository<Producto> {
    Optional<Producto> findByCodigoIgnoreCase(String codigo);
    List<Producto> findBySubCategoriaIdAndEliminadoFalse(String subCategoriaId);
    List<Producto> findByEnOfertaTrueAndEliminadoFalse();
    /*
     Bloqueo pesimista: dos facturaciones simultáneas del mismo producto se ejecutan en fila,
     así no se vende dos veces el mismo stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id and p.eliminado = false")
    Optional<Producto> buscarConBloqueo(@Param("id") String id);
}
