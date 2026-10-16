package redselection.clienteJava.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import redselection.clienteJava.dto.HistoriaClinicaDTO;
import redselection.clienteJava.builder.HistoriaClinicaBuilderImpl;
import redselection.clienteJava.builder.HistoriaClinicaDirector;
import redselection.clienteJava.service.HistoriaClinicaClientService;
import redselection.clienteJava.service.MedicoClientService;
import redselection.clienteJava.service.PacienteClientService;
import redselection.clienteJava.service.UsuarioClientService;
import redselection.clienteJava.util.MensajesError;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/historias")
public class HistoriaClinicaViewController {

    private final HistoriaClinicaClientService historias;
    private final PacienteClientService pacientes;
    private final UsuarioClientService usuarios;
    private final MedicoClientService medicos;

    public HistoriaClinicaViewController(HistoriaClinicaClientService historias,
                                         PacienteClientService pacientes,
                                         UsuarioClientService usuarios,
                                         MedicoClientService medicos) {
        this.historias = historias;
        this.pacientes = pacientes;
        this.usuarios = usuarios;
        this.medicos = medicos;
    }

    // ---------- LISTA (con filtro opcional por paciente) ----------

    @GetMapping
    public String lista(@RequestParam(required = false) String pacienteId, Model model) {
        try {
            List<HistoriaClinicaDTO> lista = StringUtils.hasText(pacienteId)
                    ? historias.listarPorPaciente(pacienteId)
                    : historias.listar();
            model.addAttribute("historias", lista);
            model.addAttribute("pacientes", pacientes.listar());
            model.addAttribute("pacienteSel", pacienteId);
        } catch (Exception e) {
            model.addAttribute("historias", List.of());
            model.addAttribute("pacientes", List.of());
            model.addAttribute("error", MensajesError.de(e));
        }
        return "historias/lista";
    }

    // ---------- ALTA / EDICIÓN DE LA CABECERA (paciente + usuario) ----------

    @GetMapping("/nuevo")
    public String nuevo(Model model, RedirectAttributes ra) {
        try {
            cargarCombos(model);
            return "historias/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
            return "redirect:/historias";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes ra) {
        try {
            HistoriaClinicaDTO h = historias.obtener(id);
            cargarCombos(model);
            model.addAttribute("historiaId", h.getId());
            model.addAttribute("pacienteSel", h.getPaciente().getId());
            model.addAttribute("usuarioSel", h.getUsuario().getId());
            return "historias/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
            return "redirect:/historias";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) String id,
                          @RequestParam String pacienteId,
                          @RequestParam String usuarioId,
                          RedirectAttributes ra) {
        try {
            HistoriaClinicaBuilderImpl builder = new HistoriaClinicaBuilderImpl();
            HistoriaClinicaDirector director = new HistoriaClinicaDirector(builder);

            if (!StringUtils.hasText(id)) {
                director.construirNueva(pacienteId, usuarioId);
                HistoriaClinicaDTO creada = historias.crear(builder.obtenerResultado());
                ra.addFlashAttribute("exito", "Historia clínica creada. Ahora podés agregar consultas.");
                return "redirect:/historias/" + creada.getId();
            }

            HistoriaClinicaDTO existente = historias.obtener(id);
            director.construirConNuevaCabecera(existente, pacienteId, usuarioId);
            historias.actualizar(id, builder.obtenerResultado());
            ra.addFlashAttribute("exito", "Historia clínica actualizada");
            return "redirect:/historias/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
            return "redirect:/historias";
        }
    }

    // ---------- DETALLE DE UNA HISTORIA ----------

    @GetMapping("/{id}")
    public String ver(@PathVariable String id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("historia", historias.obtener(id));
            model.addAttribute("medicos", medicos.listar());
            model.addAttribute("hoy", LocalDate.now());
            return "historias/detalle";
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
            return "redirect:/historias";
        }
    }

    @PostMapping("/{id}/detalles")
    public String agregarDetalle(@PathVariable String id,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHistoria,
                                 @RequestParam String detalleHistoria,
                                 @RequestParam String medicoId,
                                 RedirectAttributes ra) {
        try {
            HistoriaClinicaDTO existente = historias.obtener(id);

            HistoriaClinicaBuilderImpl builder = new HistoriaClinicaBuilderImpl();
            new HistoriaClinicaDirector(builder)
                    .construirConNuevaConsulta(existente, fechaHistoria, detalleHistoria, medicoId);

            historias.actualizar(id, builder.obtenerResultado());
            ra.addFlashAttribute("exito", "Consulta agregada");
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/historias/" + id;
    }

    @PostMapping("/{id}/detalles/{detalleId}/eliminar")
    public String eliminarDetalle(@PathVariable String id, @PathVariable String detalleId,
                                  RedirectAttributes ra) {
        try {
            HistoriaClinicaDTO existente = historias.obtener(id);

            HistoriaClinicaBuilderImpl builder = new HistoriaClinicaBuilderImpl();
            new HistoriaClinicaDirector(builder).construirSinConsulta(existente, detalleId);

            historias.actualizar(id, builder.obtenerResultado());
            ra.addFlashAttribute("exito", "Consulta eliminada");
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/historias/" + id;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        try {
            historias.eliminar(id);
            ra.addFlashAttribute("exito", "Historia clínica eliminada");
        } catch (Exception e) {
            ra.addFlashAttribute("error", MensajesError.de(e));
        }
        return "redirect:/historias";
    }

    private void cargarCombos(Model model) {
        model.addAttribute("pacientes", pacientes.listar());
        model.addAttribute("usuarios", usuarios.listar());
    }
}