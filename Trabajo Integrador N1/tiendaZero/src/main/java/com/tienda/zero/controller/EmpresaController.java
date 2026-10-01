package com.tienda.zero.controller;

import com.tienda.zero.dto.EmpresaFormDTO;
import com.tienda.zero.enums.TipoContacto;
import com.tienda.zero.enums.TipoEmpresa;
import com.tienda.zero.enums.TipoTelefono;
import com.tienda.zero.model.ContactoCorreoElectronico;
import com.tienda.zero.model.ContactoTelefonico;
import com.tienda.zero.model.Empresa;
import com.tienda.zero.service.EmpresaService;
import com.tienda.zero.service.GestionEmpresaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.tienda.zero.service.UbicacionService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/empresas")
public class EmpresaController {

    private static final String VISTA_FORMULARIO = "admin/empresas/formulario";
    private static final String REDIRECCION_LISTA = "redirect:/admin/empresas";

    private final EmpresaService empresaService;
    private final GestionEmpresaService gestionEmpresaService;
    private final UbicacionService ubicacionService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("empresas", empresaService.listarEmpresaActiva());
        return "admin/empresas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        prepararFormulario(model, new EmpresaFormDTO(), null);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/nueva")
    public String crear(@ModelAttribute("empresaForm") EmpresaFormDTO formulario, Model model,
                        RedirectAttributes redirectAttributes) {
        try {
            gestionEmpresaService.crearEmpresa(formulario);
            redirectAttributes.addFlashAttribute("mensajeExito", "La empresa fue creada correctamente.");
            return REDIRECCION_LISTA;
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            prepararFormulario(model, formulario, null);
            return VISTA_FORMULARIO;
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Empresa empresa = empresaService.buscarEmpresa(id);
            if (empresa.isEliminado()) {
                throw new IllegalArgumentException("No se puede editar una empresa eliminada");
            }
            prepararFormulario(model, convertirFormulario(empresa), empresa);
            return VISTA_FORMULARIO;
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return REDIRECCION_LISTA;
        }
    }

    @PostMapping("/{id}/editar")
    public String modificar(@PathVariable String id,
                            @ModelAttribute("empresaForm") EmpresaFormDTO formulario,
                            Model model, RedirectAttributes redirectAttributes) {
        try {
            gestionEmpresaService.modificarEmpresa(id, formulario);
            redirectAttributes.addFlashAttribute("mensajeExito", "La empresa fue modificada correctamente.");
            return REDIRECCION_LISTA;
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            prepararFormulario(model, formulario, Empresa.builder().id(id).build());
            return VISTA_FORMULARIO;
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            gestionEmpresaService.eliminarEmpresa(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "La empresa fue eliminada.");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return REDIRECCION_LISTA;
    }

    private void prepararFormulario(Model model, EmpresaFormDTO formulario, Empresa empresa) {
        model.addAttribute("empresaForm", formulario);
        model.addAttribute("empresa", empresa);
        model.addAttribute("tiposEmpresa", TipoEmpresa.values());
        model.addAttribute("tiposContacto", TipoContacto.values());
        model.addAttribute("tiposTelefono", TipoTelefono.values());
        model.addAttribute("ubicacion", ubicacionService.obtenerUbicaciones());
    }

    private EmpresaFormDTO convertirFormulario(Empresa empresa) {
        EmpresaFormDTO formulario = new EmpresaFormDTO();
        formulario.setRazonSocial(empresa.getRazonSocial());
        formulario.setCuit(empresa.getCuit());
        formulario.setTipoEmpresa(empresa.getTipoEmpresa());
        formulario.setCalle(empresa.getDireccion().getCalle());
        formulario.setNumeracion(empresa.getDireccion().getNumeracion());
        formulario.setBarrio(empresa.getDireccion().getBarrio());
        formulario.setManzanaPiso(empresa.getDireccion().getManzanaPiso());
        formulario.setCasaDepartamento(empresa.getDireccion().getCasaDepartamento());
        formulario.setReferencia(empresa.getDireccion().getReferencia());
        formulario.setIdLocalidad(empresa.getDireccion().getLocalidad().getId());
        formulario.setIdDepartamento(empresa.getDireccion().getLocalidad().getDepartamento().getId());
        formulario.setIdProvincia(empresa.getDireccion().getLocalidad().getDepartamento().getProvincia().getId());
        formulario.setTipoContacto(empresa.getContacto().getTipoContacto());
        formulario.setObservacion(empresa.getContacto().getObservacion());
        if (empresa.getContacto() instanceof ContactoCorreoElectronico correo) {
            formulario.setMedioContacto("CORREO");
            formulario.setEmail(correo.getEmail());
        } else if (empresa.getContacto() instanceof ContactoTelefonico telefono) {
            formulario.setMedioContacto("TELEFONO");
            formulario.setTelefono(telefono.getTelefono());
            formulario.setTipoTelefono(telefono.getTipoTelefono());
        }
        return formulario;
    }
}
