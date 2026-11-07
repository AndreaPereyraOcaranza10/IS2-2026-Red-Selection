package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.EstadoRuta;
import redSelection.dulceMayor.enums.TipoRuta;

import java.time.LocalDate;
import java.util.List;

public record RutaDTO(
        String id,
        LocalDate fecha,
        TipoRuta tipoRuta,
        EstadoRuta estado,
        /** Kilómetros en línea recta de lo que falta recorrer (paradas pendientes con coordenadas). */
        double kmRestantes,
        List<ParadaRutaDTO> paradas
) {}
