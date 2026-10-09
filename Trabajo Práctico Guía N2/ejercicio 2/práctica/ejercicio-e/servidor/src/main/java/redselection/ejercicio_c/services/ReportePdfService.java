package redselection.ejercicio_c.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.entities.Persona;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ReportePdfService {

    public ByteArrayInputStream generarReporteAlquileres(List<Persona> todasLasPersonas) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Paragraph titulo = new Paragraph("Listado de Personas con Alquiler de Libros", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph(" "));

            //Filtrar solo personas que alquilaron al menos un libro
            List<Persona> personasConAlquiler = todasLasPersonas.stream()
                    .filter(p -> p.getLibros() != null && !p.getLibros().isEmpty())
                    .toList();

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 3f, 3f, 4f}); // Anchos proporcionales

            Stream.of("DNI", "Persona", "Libro Alquilado", "Autores / Género")
                    .forEach(colTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(1);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        header.setPadding(6);
                        header.setPhrase(new Phrase(colTitle, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                        table.addCell(header);
                    });

            //filas por cada libro alquilado
            for (Persona p : personasConAlquiler) {
                String nombreCompleto = p.getNombre() + " " + p.getApellido();
                String dni = String.valueOf(p.getDni());

                for (Libro libro : p.getLibros()) {

                    PdfPCell cellDni = new PdfPCell(new Phrase(dni));
                    cellDni.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cellDni.setPadding(5);
                    table.addCell(cellDni);

                    PdfPCell cellNombre = new PdfPCell(new Phrase(nombreCompleto));
                    cellNombre.setPadding(5);
                    table.addCell(cellNombre);

                    PdfPCell cellLibro = new PdfPCell(new Phrase(libro.getTitulo() != null ? libro.getTitulo() : "-"));
                    cellLibro.setPadding(5);
                    table.addCell(cellLibro);

                    String genero = libro.getGenero() != null ? libro.getGenero() : "-";
                    PdfPCell cellGenero = new PdfPCell(new Phrase(genero));
                    cellGenero.setPadding(5);
                    table.addCell(cellGenero);
                }
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
