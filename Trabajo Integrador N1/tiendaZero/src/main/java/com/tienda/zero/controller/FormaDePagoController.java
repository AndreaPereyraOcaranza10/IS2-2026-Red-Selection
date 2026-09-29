package com.tienda.zero.controller;

import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.model.FormaDePago;
import com.tienda.zero.service.FormaDePagoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ABM de formas de pago en la administración.
 * Alta y modificación comparten la plantilla formulario.html.
 */
@Controller
@RequestMapping("/admin/formas-de-pago")
public class FormaDePagoController {

    private static final String VISTA_LISTA = "admin/formas-de-pago/lista";
    private static final String VISTA_FORMULARIO = "admin/formas-de-pago/formulario";
    private static final String REDIRECCION_LISTA = "redirect:/admin/formas-de-pago";

    private final FormaDePagoService formaDePagoService;

    public FormaDePagoController(FormaDePagoService formaDePagoService) {
        this.formaDePagoService = formaDePagoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("formasDePago", formaDePagoService.listarFormaDePago());
        return VISTA_LISTA;
    }

    @GetMapping("/nueva")
    public String mostrarAlta(Model model) {
        prepararFormulario(model, "Nueva forma de pago", "/admin/formas-de-pago/nueva", null, null);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/nueva")
    public String crear(@RequestParam(required = false) TipoPago tipoPago,
                        @RequestParam(required = false) String observacion,
                        Model model, RedirectAttributes redirectAttributes) {
        try {
            formaDePagoService.crearFormaDePago(tipoPago, observacion);
            redirectAttributes.addFlashAttribute("exito", "Forma de pago creada correctamente");
            return REDIRECCION_LISTA;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            prepararFormulario(model, "Nueva forma de pago", "/admin/formas-de-pago/nueva", tipoPago, observacion);
            return VISTA_FORMULARIO;
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarModificacion(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            FormaDePago formaDePago = formaDePagoService.buscarFormaDePago(id);
            prepararFormulario(model, "Modificar forma de pago", "/admin/formas-de-pago/" + id + "/editar",
                    formaDePago.getTipoPago(), formaDePago.getObservacion());
            return VISTA_FORMULARIO;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return REDIRECCION_LISTA;
        }
    }

    @PostMapping("/{id}/editar")
    public String modificar(@PathVariable String id,
                            @RequestParam(required = false) TipoPago tipoPago,
                            @RequestParam(required = false) String observacion,
                            Model model, RedirectAttributes redirectAttributes) {
        try {
            formaDePagoService.modificarFormaDePago(id, tipoPago, observacion);
            redirectAttributes.addFlashAttribute("exito", "Forma de pago modificada correctamente");
            return REDIRECCION_LISTA;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            prepararFormulario(model, "Modificar forma de pago", "/admin/formas-de-pago/" + id + "/editar",
                    tipoPago, observacion);
            return VISTA_FORMULARIO;
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            formaDePagoService.eliminarFormaDePago(id);
            redirectAttributes.addFlashAttribute("exito", "Forma de pago eliminada correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECCION_LISTA;
    }

    /** Carga en el modelo todo lo que necesita formulario.html, tanto para alta como para modificación. */
    private void prepararFormulario(Model model, String titulo, String accion,
                                    TipoPago tipoSeleccionado, String observacion) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
        model.addAttribute("tipos", TipoPago.values());
        model.addAttribute("tipoSeleccionado", tipoSeleccionado);
        model.addAttribute("observacion", observacion);
    }
}