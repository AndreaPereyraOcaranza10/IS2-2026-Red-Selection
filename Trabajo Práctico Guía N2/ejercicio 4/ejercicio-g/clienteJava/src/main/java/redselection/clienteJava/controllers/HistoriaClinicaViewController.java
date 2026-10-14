package redselection.clienteJava.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import redselection.clienteJava.dto.*;
import redselection.clienteJava.service.*;
import redselection.clienteJava.util.MensajesError;

import java.time.LocalDate;
import java.util.ArrayList;
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
                HistoriaClinicaDTO nueva = new HistoriaClinicaDTO();
                nueva.setPaciente(refPaciente(pacienteId));
                nueva.setUsuario(refUsuario(usuarioId));
                HistoriaClinicaDTO creada = historias.crear(nueva);
                ra.addFlashAttribute("exito", "Historia clínica creada. Ahora podés agregar consultas.");
                return "redirect:/historias/" + creada.getId();
            }
            HistoriaClinicaDTO existente = historias.obtener(id);
            HistoriaClinicaDTO enviar = aEnviar(existente, existente.getDetalles());
            enviar.setPaciente(refPaciente(pacienteId));
            enviar.setUsuario(refUsuario(usuarioId));
            historias.actualizar(id, enviar);
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

            List<DetalleHistoriaClinicaDTO> detalles = new ArrayList<>(existente.getDetalles());
            DetalleHistoriaClinicaDTO nuevo = new DetalleHistoriaClinicaDTO();
            nuevo.setFechaHistoria(fechaHistoria);
            nuevo.setDetalleHistoria(detalleHistoria);
            nuevo.setMedico(refMedico(medicoId));
            detalles.add(nuevo);

            historias.actualizar(id, aEnviar(existente, detalles));
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
            List<DetalleHistoriaClinicaDTO> restantes = existente.getDetalles().stream()
                    .filter(d -> !detalleId.equals(d.getId()))
                    .toList();
            historias.actualizar(id, aEnviar(existente, restantes));
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

    // ---------- Ayudas privadas ----------

    private void cargarCombos(Model model) {
        model.addAttribute("pacientes", pacientes.listar());
        model.addAttribute("usuarios", usuarios.listar());
    }

    /** Arma la historia "liviana" que se manda al servidor: solo ids de las referencias. */
    private HistoriaClinicaDTO aEnviar(HistoriaClinicaDTO original, List<DetalleHistoriaClinicaDTO> detalles) {
        HistoriaClinicaDTO h = new HistoriaClinicaDTO();
        h.setId(original.getId());
        h.setPaciente(refPaciente(original.getPaciente().getId()));
        h.setUsuario(refUsuario(original.getUsuario().getId()));

        List<DetalleHistoriaClinicaDTO> copia = new ArrayList<>();
        for (DetalleHistoriaClinicaDTO d : detalles) {
            DetalleHistoriaClinicaDTO c = new DetalleHistoriaClinicaDTO();
            c.setFechaHistoria(d.getFechaHistoria());
            c.setDetalleHistoria(d.getDetalleHistoria());
            c.setMedico(refMedico(d.getMedico().getId()));
            copia.add(c);
        }
        h.setDetalles(copia);
        return h;
    }

    private PacienteDTO refPaciente(String id) {
        PacienteDTO p = new PacienteDTO();
        p.setId(id);
        return p;
    }

    private UsuarioDTO refUsuario(String id) {
        UsuarioDTO u = new UsuarioDTO();
        u.setId(id);
        return u;
    }

    private MedicoDTO refMedico(String id) {
        MedicoDTO m = new MedicoDTO();
        m.setId(id);
        return m;
    }
}