package redselection.clienteJava.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import redselection.clienteJava.dto.UsuarioDTO;
import redselection.clienteJava.service.UsuarioClientService;
import redselection.clienteJava.util.MensajesError;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioViewController {

    private final UsuarioClientService servicio;

    public UsuarioViewController(UsuarioClientService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String lista(Model model) {
        try {
            model.addAttribute("usuarios", servicio.listar());
        } catch (Exception e) {
            model.addAttribute("usuarios", List.of());
            model.addAttribute("error", MensajesError.de(e));
        }
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new UsuarioDTO());
        return "usuarios/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("usuario", servicio.obtener(id));
            return "usuarios/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
            return "redirect:/usuarios";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("usuario") UsuarioDTO usuario, RedirectAttributes ra) {
        try {
            if (!StringUtils.hasText(usuario.getId())) {
                usuario.setId(null);
                servicio.crear(usuario);
                ra.addFlashAttribute("exito", "Usuario creado correctamente");
            } else {
                servicio.actualizar(usuario.getId(), usuario);
                ra.addFlashAttribute("exito", "Usuario actualizado correctamente");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        try {
            servicio.eliminar(id);
            ra.addFlashAttribute("exito", "Usuario eliminado");
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/usuarios";
    }
}