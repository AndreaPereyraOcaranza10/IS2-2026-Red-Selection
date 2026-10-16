package redselection.clienteJava.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import redselection.clienteJava.dto.FotoPacienteDTO;
import redselection.clienteJava.dto.PacienteDTO;
import redselection.clienteJava.service.PacienteClientService;
import redselection.clienteJava.util.MensajesError;


import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/pacientes")
public class PacienteViewController {

    private final PacienteClientService servicio;

    public PacienteViewController(PacienteClientService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String lista(Model model) {
        try {
            model.addAttribute("pacientes", servicio.listar());
        } catch (Exception e) {
            model.addAttribute("pacientes", List.of());
            model.addAttribute("error", MensajesError.de(e));
        }
        return "pacientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("paciente", new PacienteDTO());
        return "pacientes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("paciente", servicio.obtener(id));
            return "pacientes/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
            return "redirect:/pacientes";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("paciente") PacienteDTO paciente,
                          @RequestParam(value = "foto", required = false) MultipartFile foto,
                          RedirectAttributes ra) {
        try {
            boolean esNuevo = !StringUtils.hasText(paciente.getId());

            if (foto != null && !foto.isEmpty()) {
                FotoPacienteDTO f = new FotoPacienteDTO();
                f.setNombre(foto.getOriginalFilename());
                f.setMime(foto.getContentType());
                f.setContenido(foto.getBytes());
                paciente.setFotoPaciente(f);
            } else if (!esNuevo) {
                // Sin foto nueva: conservamos la que ya tenía
                paciente.setFotoPaciente(servicio.obtener(paciente.getId()).getFotoPaciente());
            }

            if (esNuevo) {
                paciente.setId(null);
                servicio.crear(paciente);
                ra.addFlashAttribute("exito", "Paciente creado correctamente");
            } else {
                servicio.actualizar(paciente.getId(), paciente);
                ra.addFlashAttribute("exito", "Paciente actualizado correctamente");
            }
        } catch (IOException e) {
            ra.addFlashAttribute("error", "No se pudo leer la foto");
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/pacientes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        try {
            servicio.eliminar(id);
            ra.addFlashAttribute("exito", "Paciente eliminado");
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/pacientes";
    }
}
