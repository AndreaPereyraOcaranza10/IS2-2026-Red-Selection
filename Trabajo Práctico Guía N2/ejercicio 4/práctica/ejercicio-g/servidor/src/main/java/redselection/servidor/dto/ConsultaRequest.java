package redselection.servidor.dto;

import java.time.LocalDate;

// Datos mínimos que necesita el servidor para registrar una consulta nueva.
public record ConsultaRequest(LocalDate fechaHistoria, String detalleHistoria, String medicoId) {
}