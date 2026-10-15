package redselection.cliente.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import redselection.cliente.service.AutorClientService;
import redselection.cliente.service.LibroClientService;
import redselection.cliente.service.PersonaClientService;
import redselection.cliente.dto.PersonaDTO;
import redselection.cliente.util.MensajesError;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.client.RestClientException;
import redselection.cliente.dto.LibroDTO;
import redselection.cliente.dto.AutorDTO;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/libros")
public class LibroViewController {

    private final LibroClientService libroService;
    private final AutorClientService autorService;
    private final PersonaClientService personaService;

    public LibroViewController(
            LibroClientService libroService,
            AutorClientService autorService,
            PersonaClientService personaService) {
        this.libroService = libroService;
        this.autorService = autorService;
        this.personaService = personaService;
    }

    @GetMapping
    public String listar(
            @RequestParam(name = "criterio", required = false, defaultValue = "TITULO") String criterio,
            @RequestParam(name = "valor", required = false) String valor,
            Model model) {

        boolean hayBusqueda = valor != null && !valor.isBlank();

        try {
            List<LibroDTO> libros = hayBusqueda
                    ? libroService.buscar(criterio, valor.trim())
                    : libroService.listar();

            model.addAttribute("libros", libros);
            model.addAttribute("asignaciones", asignaciones());

        } catch (RestClientException e) {
            model.addAttribute("libros", List.of());
            model.addAttribute("asignaciones", Map.of());
            model.addAttribute("error", MensajesError.de(e));
        }

        model.addAttribute("criterio", criterio);
        model.addAttribute("valor", valor);
        model.addAttribute("etiquetaCriterio", etiqueta(criterio));

        return "libros/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("libro", new LibroDTO());
        model.addAttribute("autores", autorService.listar());
        return "libros/form";
    }

    @PostMapping
    public String crear(
            @ModelAttribute("libro") LibroDTO libro,
            @RequestParam(name = "autorIds", required = false) List<Long> autorIds,
            Model model,
            RedirectAttributes redirect) {

        asignarAutores(libro, autorIds);

        try {
            libroService.crear(libro);
            redirect.addFlashAttribute(
                    "mensaje", "Libro creado correctamente.");
            return "redirect:/libros";

        } catch (RestClientException e) {
            model.addAttribute("autores", autorService.listar());
            model.addAttribute(
                    "error", MensajesError.de(e));
            return "libros/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("libro", libroService.obtener(id));
        model.addAttribute("autores", autorService.listar());
        return "libros/form";
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable Long id,
            @ModelAttribute("libro") LibroDTO libro,
            @RequestParam(name = "autorIds", required = false) List<Long> autorIds,
            Model model,
            RedirectAttributes redirect) {

        asignarAutores(libro, autorIds);

        try {
            libroService.actualizar(id, libro);
            redirect.addFlashAttribute(
                    "mensaje", "Libro actualizado correctamente.");
            return "redirect:/libros";

        } catch (RestClientException e) {
            libro.setId(id);
            model.addAttribute("autores", autorService.listar());
            model.addAttribute(
                    "error", MensajesError.de(e));
            return "libros/form";
        }
    }
    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Long id,
            RedirectAttributes redirect) {
        try {
            libroService.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Libro eliminado correctamente.");
        } catch (RestClientException e) {
            redirect.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/libros";
    }

    private String etiqueta(String criterio) {
        return switch (criterio) {
            case "GENERO" -> "género";
            case "AUTOR" -> "autor";
            default -> "título";
        };
    }

    // libroId -> "Nombre Apellido" de la persona que lo tiene asignado (solo es informativo)
    private Map<Long, String> asignaciones() {
        Map<Long, String> asignaciones = new HashMap<>();
        for (PersonaDTO persona : personaService.listar()) {
            if (persona.getLibros() == null) {
                continue;
            }
            for (LibroDTO libro : persona.getLibros()) {
                asignaciones.put(libro.getId(), persona.getNombre() + " " + persona.getApellido());
            }
        }
        return asignaciones;
    }

    private void asignarAutores(LibroDTO libro, List<Long> autorIds) {
        List<AutorDTO> autoresSeleccionados = new ArrayList<>();

        if (autorIds != null) {
            for (Long id : autorIds) {
                AutorDTO autor = new AutorDTO();
                autor.setId(id);
                autoresSeleccionados.add(autor);
            }
        }

        libro.setAutores(autoresSeleccionados);
    }

}