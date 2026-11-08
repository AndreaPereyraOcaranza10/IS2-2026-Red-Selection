package redSelection.dulceMayor.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.model.ResponsableComercio;

import java.util.List;

@Repository
public interface ResponsableComercioRepository extends BaseRepository<ResponsableComercio> {

    boolean existsByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(TipoDocumento tipoDocumento, String numeroDocumento);

    /** Búsqueda por nombre, apellido (en cualquier orden) o parte del documento. */
    @Query("""
            select r from ResponsableComercio r
            where r.eliminado = false
              and (lower(concat(r.nombre, ' ', r.apellido)) like lower(concat('%', :texto, '%'))
                or lower(concat(r.apellido, ' ', r.nombre)) like lower(concat('%', :texto, '%'))
                or r.numeroDocumento like concat('%', :texto, '%'))
            order by r.apellido, r.nombre
            """)
    List<ResponsableComercio> buscar(@Param("texto") String texto, Pageable pageable);
}