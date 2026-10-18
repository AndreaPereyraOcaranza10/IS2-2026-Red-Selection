package redselection.cliente.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import redselection.cliente.dto.LibroDTO;
import redselection.cliente.dto.PersonaDTO;
import redselection.cliente.service.AutorClientService;
import redselection.cliente.service.LocalidadClientService;
import redselection.cliente.service.PersonaClientService;
import redselection.cliente.util.MensajesError;
import redselection.cliente.service.LibroClientService;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/personas")
public class PersonaViewController {

    private final PersonaClientService personaService;
    private final LocalidadClientService localidadService;
    private final AutorClientService autorService;
    private final LibroClientService libroService;

    public PersonaViewController(PersonaClientService personaService, LocalidadClientService localidadService, AutorClientService autorService, LibroClientService libroService) {
        this.personaService = personaService;
        this.localidadService = localidadService;
        this.autorService = autorService;
        this.libroService = libroService;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) String filtro, Model model) {
        try {
            List<PersonaDTO> personas = (filtro == null || filtro.isBlank())
                    ? personaService.listar()
                    : personaService.buscar(filtro);
            model.addAttribute("personas", personas);
        } catch (RestClientException e) {
            model.addAttribute("personas", List.of());
            model.addAttribute("error", MensajesError.de(e));
        }
        model.addAttribute("filtro", filtro);
        return "personas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("persona", new PersonaDTO());
        cargarLocalidades(model);
        cargarLibros(model);
        return "personas/form";
    }

    @PostMapping
    public String crear(
            @ModelAttribute("persona") PersonaDTO persona,
            @RequestParam(required = false) List<Long> libroIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDevolucion,
            Model model,
            RedirectAttributes redirect) {

        PersonaDTO personaCreada;
        try {
            personaCreada = personaService.crear(persona);
        } catch (RestClientException e) {
            model.addAttribute("error", MensajesError.de(e));
            cargarLocalidades(model);
            cargarLibros(model);
            return "personas/form";
        }

        // La persona ya existe; los libros se le asignan del catálogo (no se crean libros nuevos)
        if (libroIds != null) {
            try {
                for (Long libroId : libroIds) {
                    personaService.asociarLibro(personaCreada.getId(), libroId, fechaDevolucion);
                }
            } catch (RestClientException e) {
                redirect.addFlashAttribute("error",
                        "La persona se creó, pero no se pudieron asignar todos los libros: " + MensajesError.de(e));
                return "redirect:/personas/" + personaCreada.getId();
            }
        }

        redirect.addFlashAttribute("mensaje", "Persona creada correctamente.");
        return "redirect:/personas";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            PersonaDTO persona = personaService.obtener(id);
            persona.asegurarDomicilio();
            model.addAttribute("persona", persona);
            model.addAttribute("librosDisponibles", libroService.listarDisponibles());
            return "personas/detalle";
        } catch (RestClientException e) {
            redirect.addFlashAttribute("error", MensajesError.de(e));
            return "redirect:/personas";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            PersonaDTO persona = personaService.obtener(id);
            persona.asegurarDomicilio();
            model.addAttribute("persona", persona);
            cargarLocalidades(model);
            return "personas/form";
        } catch (RestClientException e) {
            redirect.addFlashAttribute("error", MensajesError.de(e));
            return "redirect:/personas";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute("persona") PersonaDTO persona,
                             Model model, RedirectAttributes redirect) {
        try {
            personaService.actualizar(id, persona);
            redirect.addFlashAttribute("mensaje", "Persona actualizada correctamente.");
            return "redirect:/personas";
        } catch (RestClientException e) {
            persona.setId(id);
            model.addAttribute("error", MensajesError.de(e));
            cargarLocalidades(model);
            return "personas/form";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            personaService.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Persona eliminada correctamente.");
        } catch (RestClientException e) {
            redirect.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/personas";
    }

    @PostMapping("/{personaId}/libros")
    public String agregarLibro(
            @PathVariable Long personaId,
            @RequestParam Long libroId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDevolucion,
            RedirectAttributes redirect) {

        try {
            personaService.asociarLibro(personaId, libroId, fechaDevolucion);
            redirect.addFlashAttribute("mensaje", "Libro asignado correctamente.");
        } catch (RestClientException e) {
            redirect.addFlashAttribute("error", MensajesError.de(e));
        }

        return "redirect:/personas/" + personaId;
    }

    @PostMapping("/{id}/libros/{libroId}/eliminar")
    public String quitarLibro(@PathVariable Long id, @PathVariable Long libroId, RedirectAttributes redirect) {
        try {
            personaService.quitarLibro(id, libroId);
            redirect.addFlashAttribute("mensaje", "Libro desasignado correctamente. Sigue disponible en el catálogo.");
        } catch (RestClientException e) {
            redirect.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/personas/" + id;
    }

    private void cargarAutores(Model model) {
        try {
            model.addAttribute("autores", autorService.listar());
        } catch (RestClientException e) {
            model.addAttribute("autores", List.of());
            model.addAttribute("error", MensajesError.de(e));
        }
    }

    private void cargarLocalidades(Model model) {
        try {
            model.addAttribute("localidades", localidadService.listar());
        } catch (RestClientException e) {
            model.addAttribute("localidades", List.of());
            model.addAttribute("error", MensajesError.de(e));
        }
    }

    private void cargarLibros(Model model) {
        try {
            model.addAttribute("librosDisponibles", libroService.listarDisponibles());
        } catch (RestClientException e) {
            model.addAttribute("librosDisponibles", List.of());
            model.addAttribute("error", MensajesError.de(e));
        }
    }
}