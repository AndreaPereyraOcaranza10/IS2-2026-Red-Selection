package com.concurso.abm.mapper;

import com.concurso.abm.dto.response.RegistroResponse;
import com.concurso.abm.entity.*;

public final class RegistroMapper {
  private RegistroMapper() {}

  public static RegistroResponse map(BaseEntity e) {
    Persona pe = e instanceof Persona p ? p : null;
    Direccion di = e instanceof Direccion d ? d : pe != null ? pe.getDireccion() : null;
    Localidad lo = e instanceof Localidad l ? l : di != null ? di.getLocalidad() : null;
    Departamento de = e instanceof Departamento d ? d : lo != null ? lo.getDepartamento() : null;
    Provincia pr = e instanceof Provincia p ? p : de != null ? de.getProvincia() : null;
    Pais pa = e instanceof Pais p ? p : pr != null ? pr.getPais() : null;
    String nombre =
        pe != null
            ? pe.getNombre()
            : e instanceof Pais p
                ? p.getNombre()
                : e instanceof Provincia p
                    ? p.getNombre()
                    : e instanceof Departamento d
                        ? d.getNombre()
                        : e instanceof Localidad l ? l.getNombre() : null;
    String ubicacion =
        String.join(
            " · ",
            java.util.stream.Stream.of(
                    di == null ? null : di.getCalle() + " " + di.getNumeracion(),
                    lo == null ? null : lo.getNombre(),
                    de == null ? null : de.getNombre(),
                    pr == null ? null : pr.getNombre(),
                    pa == null ? null : pa.getNombre())
                .filter(java.util.Objects::nonNull)
                .toList());
    return new RegistroResponse(
        e.getId(),
        nombre,
        pe == null ? null : pe.getApellido(),
        pe == null ? null : pe.getDni(),
        pe == null ? null : pe.getFechaNacimiento(),
        lo == null ? null : lo.getCodigoPostal(),
        di == null ? null : di.getCalle(),
        di == null ? null : di.getNumeracion(),
        di == null ? null : di.getBarrio(),
        di == null ? null : di.getManzanaPiso(),
        di == null ? null : di.getCasaDepartamento(),
        di == null ? null : di.getReferencia(),
        pa == null ? null : pa.getId(),
        pr == null ? null : pr.getId(),
        de == null ? null : de.getId(),
        lo == null ? null : lo.getId(),
        di == null ? null : di.getId(),
        ubicacion);
  }
}
