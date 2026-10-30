package com.concurso.abm.dto.request;

import jakarta.validation.constraints.*;

public record PasswordRequest(
    @NotBlank @Size(max = 72) String actual,
    @NotBlank @Size(min = 8, max = 72) String nueva,
    @NotBlank String confirmacion) {
  @Override
  public String toString() {
    return "PasswordRequest[credentials=REDACTED]";
  }
}
