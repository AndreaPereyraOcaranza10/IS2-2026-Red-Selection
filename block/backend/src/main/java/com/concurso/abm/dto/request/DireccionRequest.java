package com.concurso.abm.dto.request;

import jakarta.validation.constraints.*;

public record DireccionRequest(
    @NotBlank @Size(max = 150) String calle,
    @NotBlank @Size(max = 150) String numeracion,
    @Size(max = 150) String barrio,
    @Size(max = 150) String manzanaPiso,
    @Size(max = 150) String casaDepartamento,
    @Size(max = 500) String referencia,
    @NotNull @Positive Long localidadId) {}
