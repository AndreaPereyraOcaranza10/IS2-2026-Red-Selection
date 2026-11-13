package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.exception.ReglaNegocioException;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Filtros de un reporte: período (ambos extremos incluidos) y, opcionalmente, un departamento.
 * Si no se indica período, son los últimos 30 días; si no se indica departamento, se incluyen todos.
 * Se valida acá, una sola vez, para que todos los reportes acepten los mismos filtros.
 */
public record PeriodoReporte(LocalDate desde, LocalDate hasta, String departamento) {

    private static final int DIAS_POR_DEFECTO = 30;
    private static final long MAXIMO_DIAS = 366L * 5;
    private static final int MAXIMO_LARGO_DEPARTAMENTO = 100;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static PeriodoReporte de(LocalDate desde, LocalDate hasta) {
        return de(desde, hasta, null);
    }

    public static PeriodoReporte de(LocalDate desde, LocalDate hasta, String departamento) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusDays(DIAS_POR_DEFECTO - 1L);
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }
        if (ChronoUnit.DAYS.between(inicio, fin) > MAXIMO_DIAS) {
            throw new ReglaNegocioException("El período no puede superar los 5 años.");
        }
        String filtro = departamento == null || departamento.isBlank() ? null : departamento.trim();
        if (filtro != null && filtro.length() > MAXIMO_LARGO_DEPARTAMENTO) {
            throw new ReglaNegocioException("El nombre del departamento es demasiado largo.");
        }
        return new PeriodoReporte(inicio, fin, filtro);
    }

    /** true si no hay filtro de departamento o si el nombre coincide (sin distinguir mayúsculas ni tildes). */
    public boolean incluyeDepartamento(String nombreDepartamento) {
        return departamento == null
                || (nombreDepartamento != null && normalizar(nombreDepartamento).equals(normalizar(departamento)));
    }

    /** Agrega " · Departamento: X" a un texto si el reporte está filtrado por departamento. */
    public String conDepartamento(String texto) {
        return departamento == null ? texto : texto + " · Departamento: " + departamento;
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase();
    }

    public LocalDateTime inicio() { return desde.atStartOfDay(); }

    /** Último instante del día hasta, con precisión de microsegundos: MySQL redondea los 9 decimales de LocalTime.MAX al día siguiente. */
    public LocalDateTime fin() { return hasta.atTime(LocalTime.of(23, 59, 59, 999_999_000)); }

    public String texto() { return conDepartamento("Del " + desde.format(FORMATO) + " al " + hasta.format(FORMATO)); }

    /** Para el nombre del archivo: 2026-11-01_2026-11-11. */
    public String sufijoArchivo() {
        String base = desde + "_" + hasta;
        return departamento == null ? base : base + "_" + normalizar(departamento).replaceAll("[^a-z0-9]+", "-");
    }
}