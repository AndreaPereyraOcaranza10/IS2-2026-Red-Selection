package redselection.ejercicio_c.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.entities.Persona;
import redselection.ejercicio_c.repositories.PersonaRepository;
import redselection.ejercicio_c.services.PersonaServiceImpl;
import redselection.ejercicio_c.services.ReporteExcelService;
import redselection.ejercicio_c.services.ReportePdfService;

import jakarta.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/v1/personas")
public class PersonaController extends BaseControllerImpl<Persona, PersonaServiceImpl> {

    @Autowired
    private ReportePdfService reportePdfService;

    @Autowired
    private ReporteExcelService reporteExcelService;

    @Autowired
    private EntityManager entityManager;

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String filtro) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.search(filtro));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":" + e.getMessage() + "}");
        }
    }

    @GetMapping("/alquileres/pdf")
    public ResponseEntity<InputStreamResource> descargarReporteAlquileresPdf() {
        try {
            List<Persona> personas = servicio.findAll();

            ByteArrayInputStream pdfStream = reportePdfService.generarReporteAlquileres(personas);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=personas_con_alquileres.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(pdfStream));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/libros-disponibles/excel")
    public ResponseEntity<InputStreamResource> descargarLibrosDisponiblesExcel() {
        try {
            // Trae directamente los libros cuyo ID NO figure en la lista de libros de ninguna persona
            String jpql = "SELECT l FROM Libro l WHERE l.id NOT IN " +
                    "(SELECT pl.id FROM Persona p JOIN p.libros pl)";

            List<Libro> disponibles = entityManager.createQuery(jpql, Libro.class)
                    .getResultList();

            // Si la tabla persona_libro está vacía, la subconsulta anterior puede devolver null en SQL estándar.
            // Si no trajo nada pero sí hay libros en la base de datos, traemos todos:
            if (disponibles.isEmpty()) {
                disponibles = entityManager.createQuery("SELECT l FROM Libro l", Libro.class)
                        .getResultList();
            }

            System.out.println("Libros disponibles encontrados: " + disponibles.size()); // Log para consola

            ByteArrayInputStream in = reporteExcelService.generarReporteLibrosDisponibles(disponibles);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=libros_disponibles.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));

        } catch (Exception e) {
            e.printStackTrace(); // Ver el error en consola si falla
            return ResponseEntity.internalServerError().build();
        }
    }
}