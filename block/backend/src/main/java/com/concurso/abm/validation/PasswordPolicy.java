package com.concurso.abm.validation;

import com.concurso.abm.exception.DomainException;

public final class PasswordPolicy {
  private PasswordPolicy() {}

  public static void validate(String password) {
    if (password == null
        || password.length() < 8
        || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72
        || !password.matches("(?s).*[a-zA-Z].*")
        || !password.matches("(?s).*[0-9].*"))
      throw DomainException.invalid("La contraseña requiere entre 8 y 72 bytes, letras y números");
  }
}
