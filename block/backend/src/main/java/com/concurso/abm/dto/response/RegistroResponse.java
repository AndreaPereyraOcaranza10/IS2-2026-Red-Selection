package com.concurso.abm.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RegistroResponse(
    Long id,
    String nombre,
    String apellido,
    String dni,
    LocalDate fechaNacimiento,
    String codigoPostal,
    String calle,
    String numeracion,
    String barrio,
    String manzanaPiso,
    String casaDepartamento,
    String referencia,
    Long paisId,
    Long provinciaId,
    Long departamentoId,
    Long localidadId,
    Long direccionId,
    String ubicacion) {}
