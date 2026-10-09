package ar.edu.is2.ejercicioe.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice //permite manejar excepciones de manera global para todos los controladores
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class) //indica que este método manejará las excepciones de tipo ResponseStatusException
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException exception) {
        String message = exception.getReason() == null ? "No se pudo completar la consulta." : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("message", message));
    }
}
