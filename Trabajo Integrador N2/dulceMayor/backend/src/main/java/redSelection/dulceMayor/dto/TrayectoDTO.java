package redSelection.dulceMayor.dto;

import java.util.List;

/** Camino en auto entre dos puntos. `geometria` son pares [latitud, longitud] para dibujar en el mapa. */
public record TrayectoDTO(
        double distanciaKm,
        int duracionMin,
        List<double[]> geometria
) {}
