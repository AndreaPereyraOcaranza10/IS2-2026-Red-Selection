package redselection.ejercicio_c.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import redselection.ejercicio_c.entities.Libro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReporteExcelService {

    public ByteArrayInputStream generarReporteLibrosDisponibles(List<Libro> librosDisponibles) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // 1. Crear la hoja (como en el Video 1)
            Sheet hoja = workbook.createSheet("Libros Disponibles");

            // 2. Fila 0: Título principal (como en el Video 2)
            Row filaTitulo = hoja.createRow(0);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("LISTADO DE LIBROS DISPONIBLES");

            // 3. Fila 2: Cabeceras de columnas (como en el Video 2)
            Row filaCabecera = hoja.createRow(2);
            String[] columnas = {"ID", "Título", "Género", "Páginas", "Fecha / Año"};

            for (int i = 0; i < columnas.length; i++) {
                Cell celda = filaCabecera.createCell(i);
                celda.setCellValue(columnas[i]);
            }

            // 4. Filas de datos a partir del índice 3 (como en el Video 2)
            int numFila = 3;
            for (Libro libro : librosDisponibles) {
                Row filaData = hoja.createRow(numFila);

                filaData.createCell(0).setCellValue(libro.getId() != null ? libro.getId() : 0);
                filaData.createCell(1).setCellValue(libro.getTitulo() != null ? libro.getTitulo() : "-");
                filaData.createCell(2).setCellValue(libro.getGenero() != null ? libro.getGenero() : "-");
                filaData.createCell(3).setCellValue(libro.getPaginas());
                filaData.createCell(4).setCellValue(libro.getFecha());

                numFila++;
            }

            // Ajustar automáticamente el ancho de las columnas
            for (int i = 0; i < columnas.length; i++) {
                hoja.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel: " + e.getMessage());
        }
    }
}