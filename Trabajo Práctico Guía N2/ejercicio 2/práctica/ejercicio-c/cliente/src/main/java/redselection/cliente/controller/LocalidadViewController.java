package redselection.cliente.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import redselection.cliente.dto.LocalidadDTO;
import redselection.cliente.service.LocalidadClientService;
import redselection.cliente.util.MensajesError;

import java.util.List;

@Controller
@RequestMapping("/localidades")
public class LocalidadViewController {

    private final LocalidadClientService localidadService;

    public LocalidadViewController(LocalidadClientService localidadService) {
        this.localidadService = localidadService;
    }

    @GetMapping
    public String lista(Model model) {
        try {
            model.addAttribute("localidades", localidadService.listar());
        } catch (RestClientException e) {
            model.addAttribute("localidades", List.of());
            model.addAttribute("error", MensajesError.de(e));
        }
        model.addAttribute("nuevaLocalidad", new LocalidadDTO());
        return "localidades/lista";
    }

    @PostMapping
    public String crear(@ModelAttribute("nuevaLocalidad") LocalidadDTO localidad, RedirectAttributes redirect) {
        try {
            localidadService.crear(localidad);
            redirect.addFlashAttribute("mensaje", "Localidad agregada correctamente.");
        } catch (RestClientException e) {
            redirect.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/localidades";
    }
}
