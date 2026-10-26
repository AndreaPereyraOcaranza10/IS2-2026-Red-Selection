package com.tienda.zero.dto;

import java.util.List;

public record UbicacionDTO(
        List<ProvinciaDTO> provincias,
        List<DepartamentoDTO> departamentos,
        List<LocalidadDTO> localidades) {

    public record ProvinciaDTO(String id, String nombre) {}

    public record DepartamentoDTO(String id, String nombre, String idProvincia) {}

    public record LocalidadDTO(String id, String nombre, String idDepartamento) {}
}