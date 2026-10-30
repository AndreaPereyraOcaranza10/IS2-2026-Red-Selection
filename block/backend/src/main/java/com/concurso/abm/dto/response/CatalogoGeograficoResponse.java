package com.concurso.abm.dto.response;

import java.util.List;

public record CatalogoGeograficoResponse(
    List<PaisItem> paises,
    List<ProvinciaItem> provincias,
    List<DepartamentoItem> departamentos,
    List<LocalidadItem> localidades) {
  public record PaisItem(Long id, String nombre) {}

  public record ProvinciaItem(Long id, String nombre, Long paisId) {}

  public record DepartamentoItem(Long id, String nombre, Long provinciaId) {}

  public record LocalidadItem(Long id, String nombre, String codigoPostal, Long departamentoId) {}
}
