package com.concurso.abm.dto.request;

import jakarta.validation.constraints.*;

public record AuthRequest(
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(min = 8, max = 72) String password) {
  @Override
  public String toString() {
    return "AuthRequest[credentials=REDACTED]";
  }
}
