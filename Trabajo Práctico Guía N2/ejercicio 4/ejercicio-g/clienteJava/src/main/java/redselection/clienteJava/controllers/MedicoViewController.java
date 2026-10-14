package redselection.clienteJava.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import redselection.clienteJava.dto.MedicoDTO;
import redselection.clienteJava.service.MedicoClientService;
import redselection.clienteJava.util.MensajesError;

import java.util.List;

@Controller
@RequestMapping("/medicos")
public class MedicoViewController {

    private final MedicoClientService servicio;

    public MedicoViewController(MedicoClientService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String lista(Model model) {
        try {
            model.addAttribute("medicos", servicio.listar());
        } catch (Exception e) {
            model.addAttribute("medicos", List.of());
            model.addAttribute("error", MensajesError.de(e));
        }
        return "medicos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("medico", new MedicoDTO());
        return "medicos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("medico", servicio.obtener(id));
            return "medicos/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
            return "redirect:/medicos";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("medico") MedicoDTO medico, RedirectAttributes ra) {
        try {
            if (!StringUtils.hasText(medico.getId())) {
                medico.setId(null);
                servicio.crear(medico);
                ra.addFlashAttribute("exito", "Médico creado correctamente");
            } else {
                servicio.actualizar(medico.getId(), medico);
                ra.addFlashAttribute("exito", "Médico actualizado correctamente");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/medicos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        try {
            servicio.eliminar(id);
            ra.addFlashAttribute("exito", "Médico eliminado");
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/medicos";
    }
}