package redSelection.dulceMayor.patrones.strategy.exportacion;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import redSelection.dulceMayor.dto.ReporteDTO;
import redSelection.dulceMayor.dto.TablaReporteDTO;
import redSelection.dulceMayor.enums.FormatoReporte;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Estrategia Excel (Apache POI, .xlsx). Una hoja por tabla del reporte. Los montos y cantidades se guardan como
 * NÚMEROS (no como texto) para que se puedan sumar, filtrar y graficar en Excel.
 * No se usa autoSizeColumn a propósito: necesita fuentes del sistema y falla en contenedores Docker mínimos;
 * el ancho de cada columna se calcula a partir del contenido.
 */
@Component
public class ExportadorExcel implements ExportadorReporte {

    private static final int FILA_ENCABEZADO = 3;

    @Override
    public FormatoReporte formato() { return FormatoReporte.EXCEL; }

    @Override
    public byte[] exportar(ReporteDTO reporte) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);
            Set<String> nombresUsados = new HashSet<>();
            for (TablaReporteDTO tabla : reporte.tablas()) {
                escribirHoja(libro, estilos, reporte, tabla, nombreHoja(tabla.nombre(), nombresUsados));
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el Excel del reporte.", e);
        }
    }

    private void escribirHoja(XSSFWorkbook libro, Estilos estilos, ReporteDTO reporte, TablaReporteDTO tabla, String nombre) {
        Sheet hoja = libro.createSheet(nombre);
        hoja.createRow(0).createCell(0).setCellValue(reporte.titulo() + " - " + tabla.nombre());
        hoja.getRow(0).getCell(0).setCellStyle(estilos.titulo);
        hoja.createRow(1).createCell(0).setCellValue(reporte.periodo());

        Row encabezado = hoja.createRow(FILA_ENCABEZADO);
        int[] anchos = new int[tabla.columnas().size()];
        for (int c = 0; c < tabla.columnas().size(); c++) {
            Cell celda = encabezado.createCell(c);
            celda.setCellValue(tabla.columnas().get(c));
            celda.setCellStyle(estilos.encabezado);
            anchos[c] = tabla.columnas().get(c).length();
        }
        int numeroFila = FILA_ENCABEZADO + 1;
        for (List<Object> fila : tabla.filas()) {
            escribirFila(hoja.createRow(numeroFila++), fila, estilos, false, anchos);
        }
        if (!tabla.totales().isEmpty()) {
            escribirFila(hoja.createRow(numeroFila), tabla.totales(), estilos, true, anchos);
        }
        for (int c = 0; c < anchos.length; c++) {
            hoja.setColumnWidth(c, Math.min(60, Math.max(12, anchos[c] + 2)) * 256);
        }
        hoja.createFreezePane(0, FILA_ENCABEZADO + 1);        // el encabezado queda fijo al desplazarse
    }

    private void escribirFila(Row fila, List<Object> valores, Estilos estilos, boolean total, int[] anchos) {
        for (int c = 0; c < valores.size(); c++) {
            Object valor = valores.get(c);
            Cell celda = fila.createCell(c);
            if (valor instanceof BigDecimal monto) {
                celda.setCellValue(monto.doubleValue());
                celda.setCellStyle(total ? estilos.montoTotal : estilos.monto);
            } else if (valor instanceof Long || valor instanceof Integer) {
                celda.setCellValue(((Number) valor).doubleValue());
                celda.setCellStyle(total ? estilos.cantidadTotal : estilos.cantidad);
            } else {
                String texto = valor == null ? "" : valor.toString();
                celda.setCellValue(texto);
                celda.setCellStyle(total ? estilos.textoTotal : estilos.texto);
                anchos[c] = Math.max(anchos[c], texto.length());
            }
        }
    }

    /** Excel exige nombres de hoja únicos, de hasta 31 caracteres y sin ciertos símbolos. */
    private String nombreHoja(String nombre, Set<String> usados) {
        String base = WorkbookUtil.createSafeSheetName(nombre);
        String candidato = base;
        int n = 2;
        while (!usados.add(candidato.toLowerCase())) {
            String sufijo = " (" + n++ + ")";
            candidato = base.substring(0, Math.min(base.length(), 31 - sufijo.length())) + sufijo;
        }
        return candidato;
    }

    /** Estilos creados una vez por libro (Excel limita la cantidad de estilos distintos). */
    private static final class Estilos {
        final CellStyle titulo;
        final CellStyle encabezado;
        final CellStyle texto;
        final CellStyle monto;
        final CellStyle cantidad;
        final CellStyle textoTotal;
        final CellStyle montoTotal;
        final CellStyle cantidadTotal;

        Estilos(XSSFWorkbook libro) {
            DataFormat formato = libro.createDataFormat();
            XSSFFont negrita = libro.createFont();
            negrita.setBold(true);
            XSSFFont grande = libro.createFont();
            grande.setBold(true);
            grande.setFontHeightInPoints((short) 14);
            XSSFFont blanca = libro.createFont();
            blanca.setBold(true);
            blanca.setColor(IndexedColors.WHITE.getIndex());

            titulo = libro.createCellStyle();
            titulo.setFont(grande);

            XSSFCellStyle cabecera = libro.createCellStyle();
            cabecera.setFont(blanca);
            cabecera.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 194, 24, (byte) 91}, null));
            cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            encabezado = cabecera;

            texto = libro.createCellStyle();
            monto = libro.createCellStyle();
            monto.setDataFormat(formato.getFormat("\"$\" #,##0.00"));
            cantidad = libro.createCellStyle();
            cantidad.setDataFormat(formato.getFormat("#,##0"));

            textoTotal = totalDe(libro, negrita, null, formato);
            montoTotal = totalDe(libro, negrita, "\"$\" #,##0.00", formato);
            cantidadTotal = totalDe(libro, negrita, "#,##0", formato);
        }

        private static CellStyle totalDe(XSSFWorkbook libro, XSSFFont fuente, String patron, DataFormat formato) {
            CellStyle estilo = libro.createCellStyle();
            estilo.setFont(fuente);
            estilo.setBorderTop(BorderStyle.THIN);
            if (patron != null) {
                estilo.setDataFormat(formato.getFormat(patron));
            }
            return estilo;
        }
    }
}