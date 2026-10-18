package redselection.clienteJava.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import redselection.clienteJava.dto.HistoriaClinicaDTO;
import redselection.clienteJava.dto.PacienteDTO;
import redselection.clienteJava.dto.UsuarioDTO;
import redselection.clienteJava.dto.MedicoDTO;
import redselection.clienteJava.dto.DetalleHistoriaClinicaDTO;
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
            if (!StringUtils.hasText(id)) {
                HistoriaClinicaDTO nueva = nuevaHistoria(pacienteId, usuarioId);
                HistoriaClinicaDTO creada = historias.crear(nueva);
                ra.addFlashAttribute("exito", "Historia clínica creada. Ahora podés agregar consultas.");
                return "redirect:/historias/" + creada.getId();
            }

            HistoriaClinicaDTO existente = historias.obtener(id);
            existente.setPaciente(referenciaPaciente(pacienteId));
            existente.setUsuario(referenciaUsuario(usuarioId));
            historias.actualizar(id, existente);
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
            DetalleHistoriaClinicaDTO detalle = new DetalleHistoriaClinicaDTO();
            detalle.setFechaHistoria(fechaHistoria);
            detalle.setDetalleHistoria(detalleHistoria);
            MedicoDTO medico = new MedicoDTO();
            medico.setId(medicoId);
            detalle.setMedico(medico);
            existente.getDetalles().add(detalle);

            historias.actualizar(id, existente);
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
            existente.getDetalles().removeIf(detalle -> detalleId.equals(detalle.getId()));
            historias.actualizar(id, existente);
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

    // El cliente solo prepara DTOs para la petición HTTP; la construcción de entidades
    // del dominio mediante Builder queda centralizada en el servidor.
    private HistoriaClinicaDTO nuevaHistoria(String pacienteId, String usuarioId) {
        HistoriaClinicaDTO historia = new HistoriaClinicaDTO();
        historia.setPaciente(referenciaPaciente(pacienteId));
        historia.setUsuario(referenciaUsuario(usuarioId));
        return historia;
    }

    private PacienteDTO referenciaPaciente(String id) {
        PacienteDTO paciente = new PacienteDTO();
        paciente.setId(id);
        return paciente;
    }

    private UsuarioDTO referenciaUsuario(String id) {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(id);
        return usuario;
    }

    private void cargarCombos(Model model) {
        model.addAttribute("pacientes", pacientes.listar());
        model.addAttribute("usuarios", usuarios.listar());
    }
}