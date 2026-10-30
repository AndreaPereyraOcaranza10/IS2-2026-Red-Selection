package com.concurso.abm.exception;

import org.springframework.http.HttpStatus;

public class DomainException extends RuntimeException {
  public final HttpStatus status;

  public DomainException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

  public static DomainException missing() {
    return new DomainException(HttpStatus.NOT_FOUND, "Registro activo no encontrado");
  }

  public static DomainException conflict(String message) {
    return new DomainException(HttpStatus.CONFLICT, message);
  }

  public static DomainException invalid(String message) {
    return new DomainException(HttpStatus.BAD_REQUEST, message);
  }
}
