package redselection.cliente.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import redselection.cliente.service.UsuarioClientService;

@Controller
public class RegistroController {

    private final UsuarioClientService usuarioService;

    public RegistroController(UsuarioClientService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String formulario() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String username,
                            @RequestParam String password,
                            @RequestParam String confirmar,
                            Model model) {
        model.addAttribute("username", username);

        if (!password.equals(confirmar)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "registro";
        }
        try {
            usuarioService.registrar(username.trim(), password);
            return "redirect:/login?registrado";
        } catch (HttpClientErrorException.Conflict e) {
            model.addAttribute("error", "Ese nombre de usuario ya existe.");
        } catch (HttpClientErrorException.BadRequest e) {
            model.addAttribute("error", "Datos inválidos: usuario de 3 a 50 caracteres y contraseña de al menos 8.");
        } catch (ResourceAccessException e) {
            model.addAttribute("error", "No se pudo conectar con el servidor. Verificá que esté levantado.");
        } catch (RestClientException e) {
            model.addAttribute("error", "No se pudo crear el usuario.");
        }
        return "registro";
    }
}
