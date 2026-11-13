package redSelection.dulceMayor.patrones;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redSelection.dulceMayor.dto.PeriodoReporte;
import redSelection.dulceMayor.exception.ReglaNegocioException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** HU-02: el filtro por departamento (opcional) de los reportes. */
class PeriodoReporteTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 11, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 11, 12);

    @Test
    @DisplayName("Sin departamento incluye a todos")
    void sinDepartamentoIncluyeTodos() {
        PeriodoReporte periodo = PeriodoReporte.de(DESDE, HASTA);
        assertNull(periodo.departamento());
        assertTrue(periodo.incluyeDepartamento("Godoy Cruz"));
        assertTrue(periodo.incluyeDepartamento(null));
    }

    @Test
    @DisplayName("Un departamento en blanco equivale a no filtrar")
    void departamentoEnBlancoNoFiltra() {
        PeriodoReporte periodo = PeriodoReporte.de(DESDE, HASTA, "   ");
        assertNull(periodo.departamento());
        assertTrue(periodo.incluyeDepartamento("Maipú"));
    }

    @Test
    @DisplayName("Con departamento incluye solo ese, sin importar mayúsculas ni tildes")
    void filtraPorDepartamento() {
        PeriodoReporte periodo = PeriodoReporte.de(DESDE, HASTA, "Maipú");
        assertTrue(periodo.incluyeDepartamento("MAIPU"));
        assertTrue(periodo.incluyeDepartamento("maipú"));
        assertFalse(periodo.incluyeDepartamento("Godoy Cruz"));
        assertFalse(periodo.incluyeDepartamento(null));
    }

    @Test
    @DisplayName("El texto y el nombre del archivo reflejan el departamento")
    void textoYArchivoIncluyenDepartamento() {
        PeriodoReporte periodo = PeriodoReporte.de(DESDE, HASTA, "Godoy Cruz");
        assertEquals("Del 01/11/2026 al 12/11/2026 · Departamento: Godoy Cruz", periodo.texto());
        assertEquals("2026-11-01_2026-11-12_godoy-cruz", periodo.sufijoArchivo());
    }

    @Test
    @DisplayName("Un nombre de departamento exagerado se rechaza")
    void rechazaNombreMuyLargo() {
        assertThrows(ReglaNegocioException.class, () -> PeriodoReporte.de(DESDE, HASTA, "x".repeat(101)));
    }
}
