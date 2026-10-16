package redselection.ejercicio_c.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.entities.Persona;
import redselection.ejercicio_c.repositories.PersonaRepository;
import redselection.ejercicio_c.services.EmailService;
import redselection.ejercicio_c.services.PlantillaCorreo;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class TareasProgramadas {

    private static final Logger log = LoggerFactory.getLogger(TareasProgramadas.class);

    private final PersonaRepository personaRepository;
    private final EmailService emailService;
    private final ZoneId zona;
    private final String urlFacultad;

    public TareasProgramadas(PersonaRepository personaRepository, EmailService emailService,
                             @Value("${app.mail.zona}") String zona,
                             @Value("${app.facultad.url}") String urlFacultad) {
        this.personaRepository = personaRepository;
        this.emailService = emailService;
        this.zona = ZoneId.of(zona);
        this.urlFacultad = urlFacultad;
    }

    // Avisa 1 día antes del vencimiento. @Transactional porque se leen los libros
    @Scheduled(cron = "${app.mail.cron-vencimientos}", zone = "${app.mail.zona}")
    @Transactional(readOnly = true)
    public void avisarVencimientos() {
        LocalDate manana = LocalDate.now(zona).plusDays(1);
        for (Persona persona : personaRepository.findDistinctByLibrosFechaDevolucion(manana)) {
            if (persona.getEmail() == null || persona.getEmail().isBlank()) {
                continue;
            }
            for (Libro libro : persona.getLibros()) {
                if (manana.equals(libro.getFechaDevolucion())) {
                    try {
                        emailService.enviarHtml(persona.getEmail(), "Recordatorio: devolución de libro",
                                PlantillaCorreo.vencimiento(persona.getNombre(), libro.getTitulo(), manana));
                    } catch (Exception e) {
                        log.error("No se pudo avisar a {}: {}", persona.getEmail(), e.getMessage());
                    }
                }
            }
        }
    }

    @Scheduled(cron = "${app.mail.cron-cumpleanios}", zone = "${app.mail.zona}")
    public void saludarCumpleanios() {
        LocalDate hoy = LocalDate.now(zona);
        for (Persona persona : personaRepository.findCumpleaneros(hoy.getMonthValue(), hoy.getDayOfMonth())) {
            if (persona.getEmail() == null || persona.getEmail().isBlank()) {
                continue;
            }
            try {
                emailService.enviarHtml(persona.getEmail(), "¡Feliz cumpleaños, " + persona.getNombre() + "!",
                        PlantillaCorreo.cumpleanios(persona.getNombre(), urlFacultad));
            } catch (Exception e) {
                log.error("No se pudo saludar a {}: {}", persona.getEmail(), e.getMessage());
            }
        }
    }
}