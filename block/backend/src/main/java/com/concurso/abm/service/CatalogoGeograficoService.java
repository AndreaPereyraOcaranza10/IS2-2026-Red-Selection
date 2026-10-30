package com.concurso.abm.service;

import com.concurso.abm.dto.response.CatalogoGeograficoResponse;
import com.concurso.abm.dto.response.CatalogoGeograficoResponse.*;
import com.concurso.abm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogoGeograficoService {
  private final PaisRepository paises;
  private final ProvinciaRepository provincias;
  private final DepartamentoRepository departamentos;
  private final LocalidadRepository localidades;

  public CatalogoGeograficoService(
      PaisRepository paises,
      ProvinciaRepository provincias,
      DepartamentoRepository departamentos,
      LocalidadRepository localidades) {
    this.paises = paises;
    this.provincias = provincias;
    this.departamentos = departamentos;
    this.localidades = localidades;
  }

  @Transactional(readOnly = true)
  public CatalogoGeograficoResponse obtener() {
    return new CatalogoGeograficoResponse(
        paises.findByEliminadoFalseOrderByIdAsc().stream()
            .map(p -> new PaisItem(p.getId(), p.getNombre()))
            .toList(),
        provincias.findByEliminadoFalseOrderByIdAsc().stream()
            .filter(p -> !p.getPais().getEliminado())
            .map(p -> new ProvinciaItem(p.getId(), p.getNombre(), p.getPais().getId()))
            .toList(),
        departamentos.findByEliminadoFalseOrderByIdAsc().stream()
            .filter(
                d -> !d.getProvincia().getEliminado() && !d.getProvincia().getPais().getEliminado())
            .map(d -> new DepartamentoItem(d.getId(), d.getNombre(), d.getProvincia().getId()))
            .toList(),
        localidades.findByEliminadoFalseOrderByIdAsc().stream()
            .filter(
                l ->
                    !l.getDepartamento().getEliminado()
                        && !l.getDepartamento().getProvincia().getEliminado()
                        && !l.getDepartamento().getProvincia().getPais().getEliminado())
            .map(
                l ->
                    new LocalidadItem(
                        l.getId(), l.getNombre(), l.getCodigoPostal(), l.getDepartamento().getId()))
            .toList());
  }
}
