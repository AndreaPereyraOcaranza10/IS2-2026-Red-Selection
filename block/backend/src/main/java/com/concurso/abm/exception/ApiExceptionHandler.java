package com.concurso.abm.exception;

import java.util.*;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(DomainException.class)
  ResponseEntity<?> domain(DomainException e) {
    return ResponseEntity.status(e.status).body(Map.of("message", e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException e) {
    Map<String, String> fields = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> fields.put(f.getField(), f.getDefaultMessage()));
    return ResponseEntity.badRequest()
        .body(Map.of("message", "Revisá los campos indicados", "fields", fields));
  }

  @ExceptionHandler({
    DataIntegrityViolationException.class,
    OptimisticLockingFailureException.class
  })
  ResponseEntity<?> conflict(Exception e) {
    return ResponseEntity.status(409)
        .body(
            Map.of(
                "message",
                "Registro duplicado, relación en uso o modificación concurrente. Actualizá el"
                    + " listado."));
  }

  @ExceptionHandler({
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<?> bad(Exception e) {
    return ResponseEntity.badRequest().body(Map.of("message", "Solicitud inválida"));
  }
}
