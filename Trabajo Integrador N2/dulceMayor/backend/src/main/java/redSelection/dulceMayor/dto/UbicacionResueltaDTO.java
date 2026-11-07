package redSelection.dulceMayor.dto;

/** Provincia, departamento y localidad ya verificadas contra la API, con sus nombres oficiales. */
public record UbicacionResueltaDTO(UnidadTerritorialDTO provincia,
                                   UnidadTerritorialDTO departamento,
                                   UnidadTerritorialDTO localidad) { }