package com.tienda.zero.controller;

import com.tienda.zero.service.MercadoPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Endpoints propios de tiendaZero para el retorno y webhook de Mercado Pago. */
@Controller
@RequiredArgsConstructor
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;

    @GetMapping("/checkout/mercadopago/success")
    public String success(RedirectAttributes flash) {
        flash.addFlashAttribute("mensajeExito", "El pago fue recibido. Estamos verificando el estado de la orden.");
        return "redirect:/orders";
    }

    @GetMapping("/checkout/mercadopago/pending")
    public String pending(RedirectAttributes flash) {
        flash.addFlashAttribute("mensajeExito", "El pago quedó pendiente. La orden se actualizará cuando Mercado Pago confirme su estado.");
        return "redirect:/orders";
    }

    @GetMapping("/checkout/mercadopago/failure")
    public String failure(RedirectAttributes flash) {
        flash.addFlashAttribute("mensajeError", "El pago no fue aprobado. Podés revisar la orden e intentar nuevamente.");
        return "redirect:/orders";
    }

    @PostMapping("/mercadopago/webhook")
    public ResponseEntity<Void> webhook(
            @RequestParam(name = "data.id", required = false) String dataId,
            @RequestParam(name = "type", required = false) String type,
            @RequestHeader(name = "x-signature", required = false) String xSignature,
            @RequestHeader(name = "x-request-id", required = false) String xRequestId) {

        if (!"payment".equalsIgnoreCase(type) || dataId == null || !mercadoPagoService.validarWebhook(xSignature, xRequestId, dataId)) {
            return ResponseEntity.status(401).build();
        }

        try {
            mercadoPagoService.procesarPago(Long.valueOf(dataId));
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
