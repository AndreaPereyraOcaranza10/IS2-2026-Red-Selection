package redSelection.dulceMayor.patrones.adapter;

import redSelection.dulceMayor.dto.DireccionNormalizadaDTO;
import redSelection.dulceMayor.dto.UnidadTerritorialDTO;
import redSelection.dulceMayor.dto.UbicacionInversaDTO;

import java.util.List;
import java.util.Optional;

/**
 * PATRÓN: Adapter (estructural) - interfaz destino (Target).
 * Es lo que el sistema necesita saber de ubicaciones, sin conocer de dónde salen los datos.
 */
public interface UbicacionProvider {

    List<UnidadTerritorialDTO> provincias();

    List<UnidadTerritorialDTO> departamentos(String provinciaId);

    List<UnidadTerritorialDTO> localidades(String departamentoId);

    /** Normaliza y geocodifica. Vacío si no se encontró o si el servicio no respondió. */
    Optional<DireccionNormalizadaDTO> normalizarDireccion(String calle, String numeracion,
                                                          String provinciaId, String departamentoId);
    /** Provincia y departamento de unas coordenadas. Vacío si no se pudo determinar o si el servicio no respondió. */
    Optional<UbicacionInversaDTO> ubicarCoordenadas(double latitud, double longitud);
}
