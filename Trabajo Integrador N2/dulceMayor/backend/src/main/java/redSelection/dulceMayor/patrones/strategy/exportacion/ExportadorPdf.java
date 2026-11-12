package redSelection.dulceMayor.patrones.strategy.exportacion;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import redSelection.dulceMayor.dto.ReporteDTO;
import redSelection.dulceMayor.dto.TablaReporteDTO;
import redSelection.dulceMayor.enums.FormatoReporte;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Estrategia PDF (OpenPDF, la misma librería de la factura). Hoja A4 apaisada porque algunos reportes tienen
 * muchas columnas (la matriz de ventas tiene una por preventista). Usa la fuente Helvetica incluida en el PDF.
 */
@Component
public class ExportadorPdf implements ExportadorReporte {

    private static final Color MARCA = new Color(194, 24, 91);
    private static final Color MARCA_SUAVE = new Color(244, 205, 220);
    private static final Color ZEBRA = new Color(250, 244, 247);
    private static final Color TEXTO = new Color(40, 40, 40);
    private static final Color GRIS = new Color(120, 120, 120);

    private static final Font TITULO = new Font(Font.HELVETICA, 18, Font.BOLD, MARCA);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 11, Font.NORMAL, TEXTO);
    private static final Font PIE = new Font(Font.HELVETICA, 8, Font.NORMAL, GRIS);
    private static final Font NOMBRE_TABLA = new Font(Font.HELVETICA, 12, Font.BOLD, TEXTO);
    private static final Font ENCABEZADO = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font CELDA = new Font(Font.HELVETICA, 9, Font.NORMAL, TEXTO);
    private static final Font TOTAL = new Font(Font.HELVETICA, 9, Font.BOLD, TEXTO);

    @Override
    public FormatoReporte formato() { return FormatoReporte.PDF; }

    @Override
    public byte[] exportar(ReporteDTO reporte) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(documento, salida);
            documento.open();
            documento.add(new Paragraph("Dulce Mayor", PIE));
            documento.add(new Paragraph(reporte.titulo(), TITULO));
            documento.add(new Paragraph(reporte.periodo(), SUBTITULO));
            Paragraph generado = new Paragraph("Generado el "
                    + reporte.generadoEn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), PIE);
            generado.setSpacingAfter(12);
            documento.add(generado);
            for (TablaReporteDTO tabla : reporte.tablas()) {
                agregarTabla(documento, tabla);
            }
            documento.close();
            return salida.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte.", e);
        }
    }

    private void agregarTabla(Document documento, TablaReporteDTO datos) throws DocumentException {
        Paragraph nombre = new Paragraph(datos.nombre(), NOMBRE_TABLA);
        nombre.setSpacingBefore(8);
        nombre.setSpacingAfter(6);
        documento.add(nombre);

        if (datos.filas().isEmpty()) {
            documento.add(new Paragraph("No hay datos para el período elegido.", SUBTITULO));
            return;
        }
        PdfPTable tabla = new PdfPTable(datos.columnas().size());
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);                       // el encabezado se repite en cada página
        for (String columna : datos.columnas()) {
            tabla.addCell(celda(columna, ENCABEZADO, MARCA, false));
        }
        boolean par = false;
        for (List<Object> fila : datos.filas()) {
            for (Object valor : fila) {
                tabla.addCell(celda(valor, CELDA, par ? ZEBRA : Color.WHITE, FormatoCeldas.esNumero(valor)));
            }
            par = !par;
        }
        if (!datos.totales().isEmpty()) {
            for (Object valor : datos.totales()) {
                tabla.addCell(celda(valor, TOTAL, MARCA_SUAVE, FormatoCeldas.esNumero(valor)));
            }
        }
        documento.add(tabla);
    }

    private PdfPCell celda(Object valor, Font fuente, Color fondo, boolean derecha) {
        PdfPCell celda = new PdfPCell(new Phrase(FormatoCeldas.texto(valor), fuente));
        celda.setBackgroundColor(fondo);
        celda.setBorderColor(MARCA_SUAVE);
        celda.setPadding(5);
        celda.setHorizontalAlignment(derecha ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        return celda;
    }
}