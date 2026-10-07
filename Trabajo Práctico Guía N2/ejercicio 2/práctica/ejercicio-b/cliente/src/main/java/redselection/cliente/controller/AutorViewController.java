package redselection.cliente.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import redselection.cliente.dto.AutorDTO;
import redselection.cliente.service.AutorClientService;
import redselection.cliente.util.MensajesError;

import java.util.List;

@Controller
@RequestMapping("/autores")
public class AutorViewController {

    private final AutorClientService autorService;

    public AutorViewController(AutorClientService autorService) {
        this.autorService = autorService;
    }

    @GetMapping
    public String lista(Model model) {
        try {
            model.addAttribute("autores", autorService.listar());
        } catch (RestClientException e) {
            model.addAttribute("autores", List.of());
            model.addAttribute("error", MensajesError.de(e));
        }
        model.addAttribute("nuevoAutor", new AutorDTO());
        return "autores/lista";
    }

    @PostMapping
    public String crear(@ModelAttribute("nuevoAutor") AutorDTO autor, RedirectAttributes redirect) {
        try {
            autorService.crear(autor);
            redirect.addFlashAttribute("mensaje", "Autor agregado correctamente.");
        } catch (RestClientException e) {
            redirect.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/autores";
    }
}
