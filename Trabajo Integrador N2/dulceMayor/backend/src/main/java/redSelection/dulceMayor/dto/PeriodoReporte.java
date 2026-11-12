package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.exception.ReglaNegocioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Período de un reporte (ambos extremos incluidos). Si no se indica, son los últimos 30 días.
 * Se valida acá, una sola vez, para que todos los reportes acepten las mismas fechas.
 */
public record PeriodoReporte(LocalDate desde, LocalDate hasta) {

    private static final int DIAS_POR_DEFECTO = 30;
    private static final long MAXIMO_DIAS = 366L * 5;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static PeriodoReporte de(LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusDays(DIAS_POR_DEFECTO - 1L);
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }
        if (ChronoUnit.DAYS.between(inicio, fin) > MAXIMO_DIAS) {
            throw new ReglaNegocioException("El período no puede superar los 5 años.");
        }
        return new PeriodoReporte(inicio, fin);
    }

    public LocalDateTime inicio() { return desde.atStartOfDay(); }

    /** Último instante del día hasta, con precisión de microsegundos: MySQL redondea los 9 decimales de LocalTime.MAX al día siguiente. */
    public LocalDateTime fin() { return hasta.atTime(LocalTime.of(23, 59, 59, 999_999_000)); }

    public String texto() { return "Del " + desde.format(FORMATO) + " al " + hasta.format(FORMATO); }

    /** Para el nombre del archivo: 2026-11-01_2026-11-11. */
    public String sufijoArchivo() { return desde + "_" + hasta; }
}