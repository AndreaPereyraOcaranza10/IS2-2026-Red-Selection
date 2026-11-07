package redSelection.dulceMayor.repository;

import org.springframework.stereotype.Repository;
import redSelection.dulceMayor.enums.TipoRuta;
import redSelection.dulceMayor.model.RutaDiaria;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RutaDiariaRepository extends BaseRepository<RutaDiaria> {
    Optional<RutaDiaria> findByResponsableIdAndFechaAndTipoRuta(String responsableId, LocalDate fecha, TipoRuta tipoRuta);
}