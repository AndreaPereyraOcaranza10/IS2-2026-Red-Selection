package com.tienda.zero.controller;

import com.tienda.zero.dto.DatosFacturaCliente;
import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.model.OrdenCompra;
import com.tienda.zero.service.FacturaClienteService;
import com.tienda.zero.service.MercadoPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class FacturaClienteController {
    private final FacturaClienteService facturas;
    private final MercadoPagoService mercadoPagoService;

    @GetMapping("/orders/facturas/{id}")
    public String detalleCliente(@PathVariable String id, Authentication auth, Model model) {
        model.addAttribute("factura", facturas.buscar(id, auth.getName(), false));
        model.addAttribute("administrador", false);
        return "tienda/factura";
    }

    @GetMapping("/admin/facturas/clientes/{id}")
    public String detalleAdmin(@PathVariable String id, Authentication auth, Model model) {
        model.addAttribute("factura", facturas.buscar(id, auth.getName(), true));
        model.addAttribute("administrador", true);
        return "tienda/factura";
    }

    @GetMapping("/admin/facturas/clientes/orden/{ordenId}/nueva")
    public String formulario(@PathVariable String ordenId, Authentication auth, Model model) {
        OrdenCompra orden = ordenAdministrativa(ordenId, auth.getName());
        var existente = facturas.delUsuario(auth.getName()).get(ordenId);
        if (existente != null) return "redirect:/orders/facturas/" + existente.getId();
        model.addAttribute("orden", orden);
        model.addAttribute("datos", facturas.datosSugeridos(orden));
        return "admin/facturas/formulario-cliente";
    }

    @PostMapping("/admin/facturas/clientes/orden/{ordenId}/nueva")
    public String emitir(@PathVariable String ordenId, @ModelAttribute("datos") DatosFacturaCliente datos,
                         Authentication auth, Model model, RedirectAttributes flash) {
        OrdenCompra orden = ordenAdministrativa(ordenId, auth.getName());
        String facturaId;
        try {
            facturaId = facturas.emitir(ordenId, datos).getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("orden", orden);
            model.addAttribute("mensajeError", e.getMessage());
            return "admin/facturas/formulario-cliente";
        }
        if (orden.getFormaPago() == TipoPago.BILLETERA_VIRTUAL
                && orden.getEstadoOrdenCompra() == EstadoOrdenCompra.PENDIENTE_PAGO) {
            try {
                return "redirect:" + mercadoPagoService.crearCheckout(orden);
            } catch (RuntimeException e) {
                flash.addFlashAttribute("mensajeError", "La factura fue guardada. No se pudo iniciar el pago; podes reintentarlo.");
            }
        }
        return "redirect:/orders/facturas/" + facturaId;
    }

    @PostMapping("/orders/{ordenId}/pay")
    public String pagar(@PathVariable String ordenId, Authentication auth, RedirectAttributes flash) {
        OrdenCompra orden = facturas.ordenDelUsuario(ordenId, auth.getName());
        if (orden.getFormaPago() != TipoPago.BILLETERA_VIRTUAL
                || orden.getEstadoOrdenCompra() != EstadoOrdenCompra.PENDIENTE_PAGO
                || facturas.delUsuario(auth.getName()).get(ordenId) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            return "redirect:" + mercadoPagoService.crearCheckout(orden);
        } catch (RuntimeException e) {
            flash.addFlashAttribute("mensajeError", "No se pudo iniciar el pago. Intenta nuevamente.");
            return "redirect:/orders";
        }
    }

    private OrdenCompra ordenAdministrativa(String id, String username) {
        OrdenCompra orden = facturas.ordenDelUsuario(id, username);
        if (!facturas.requiereFormulario(orden)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (orden.isEliminado() || orden.getEstadoOrdenCompra() == EstadoOrdenCompra.PENDIENTE_COMPLETAR
                || orden.getEstadoOrdenCompra() == EstadoOrdenCompra.ANULADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return orden;
    }
}
