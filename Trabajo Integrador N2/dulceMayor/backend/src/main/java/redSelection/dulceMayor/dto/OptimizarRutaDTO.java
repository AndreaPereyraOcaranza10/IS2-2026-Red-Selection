package redSelection.dulceMayor.dto;

/** Ubicación actual del dispositivo, opcional. Sin ella se parte del último comercio atendido o del depósito. */
public record OptimizarRutaDTO(
        Double latitud,
        Double longitud
) {}
