package redselection.ejercicio_c.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.entities.Domicilio;
import redselection.ejercicio_c.entities.Localidad;
import redselection.ejercicio_c.entities.Persona;
import redselection.ejercicio_c.repositories.LocalidadRepository;
import redselection.ejercicio_c.repositories.PersonaRepository;
import redselection.ejercicio_c.services.PersonaServiceImpl;
import redselection.ejercicio_c.services.ReporteExcelService;
import redselection.ejercicio_c.services.ReportePdfService;

import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.StringTokenizer;

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

    @Autowired
    private LocalidadRepository localidadRepository;

    @PostMapping("/migracion")
    public ResponseEntity<?> migrarDesdeArchivo() {
        Path archivo = Path.of("migración.txt");
        if (!Files.isRegularFile(archivo)) {
            Path archivoDelModulo = Path.of("servidor", "migración.txt");
            if (Files.isRegularFile(archivoDelModulo)) archivo = archivoDelModulo;
        }
        try {
            if (!Files.isRegularFile(archivo)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontró el archivo " + archivo.toAbsolutePath());
            }
            List<Localidad> localidades = localidadRepository.findAll();
            if (localidades.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Debe existir al menos una localidad para asignar los domicilios migrados.");
            }

            List<Persona> paraMigrar = new java.util.ArrayList<>();
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            for (int indice = 0; indice < lineas.size(); indice++) {
                String linea = lineas.get(indice);
                if (linea.isBlank()) continue;
                StringTokenizer tokenizer = new StringTokenizer(linea, ";");
                if (tokenizer.countTokens() != 5) {
                    throw new IllegalArgumentException("La línea " + (indice + 1)
                            + " debe contener NOMBRE;APELLIDO;DNI;CALLE;NÚMERO.");
                }
                String[] campos = new String[5];
                for (int campo = 0; campo < campos.length; campo++) {
                    campos[campo] = tokenizer.nextToken().trim();
                }
                Persona persona = new Persona();
                persona.setNombre(campos[0].trim());
                persona.setApellido(campos[1].trim());
                try {
                    persona.setDni(Integer.parseInt(campos[2].trim()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("El DNI de la línea " + (indice + 1) + " debe ser numérico.");
                }
                Domicilio domicilio = new Domicilio();
                domicilio.setCalle(campos[3].trim());
                try {
                    domicilio.setNumero(Integer.parseInt(campos[4].trim()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("El número de domicilio de la línea " + (indice + 1) + " debe ser numérico.");
                }
                domicilio.setLocalidad(localidades.get(0));
                persona.setDomicilio(domicilio);
                paraMigrar.add(persona);
            }
            for (Persona persona : paraMigrar) servicio.save(persona);
            return ResponseEntity.ok("Migración completada: " + paraMigrar.size() + " persona(s) ingresada(s).");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No se pudo leer migración.txt: " + e.getMessage());
        }
    }

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
