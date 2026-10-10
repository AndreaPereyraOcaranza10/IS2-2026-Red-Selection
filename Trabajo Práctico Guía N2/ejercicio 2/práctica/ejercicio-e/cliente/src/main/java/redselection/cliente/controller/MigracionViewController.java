package redselection.cliente.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;
import redselection.cliente.service.PersonaClientService;
import redselection.cliente.util.MensajesError;

@Controller
public class MigracionViewController {

    private final PersonaClientService personaService;

    public MigracionViewController(PersonaClientService personaService) {
        this.personaService = personaService;
    }

    @GetMapping("/migracion")
    public String pagina() {
        return "personas/migracion";
    }

    @PostMapping("/migracion")
    public String migrar(RedirectAttributes redirect) {
        try {
            redirect.addFlashAttribute("mensaje", personaService.migrarDesdeArchivo());
        } catch (RestClientResponseException e) {
            redirect.addFlashAttribute("error", e.getResponseBodyAsString());
        } catch (RestClientException e) {
            redirect.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/migracion";
    }
}
