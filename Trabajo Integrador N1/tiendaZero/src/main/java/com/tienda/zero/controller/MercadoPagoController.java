package com.tienda.zero.controller;

import com.tienda.zero.service.MercadoPagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Endpoints propios de tiendaZero para el retorno y webhook de Mercado Pago. */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;

    @GetMapping("/checkout/mercadopago/success")
    public String success(@RequestParam(name = "payment_id", required = false) Long paymentId,
                          @RequestParam(name = "collection_id", required = false) Long collectionId,
                          RedirectAttributes flash) {
        try {
            mercadoPagoService.procesarPago(paymentId != null ? paymentId : collectionId);
        } catch (RuntimeException e) {
            log.warn("No se pudo verificar el pago al volver del checkout", e);
            flash.addFlashAttribute("mensajeError", "No se pudo verificar el pago. Intenta actualizar Mis pedidos.");
            return "redirect:/orders";
        }
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
            @RequestBody(required = false) NotificacionPago notificacion,
            @RequestHeader(name = "x-signature", required = false) String xSignature,
            @RequestHeader(name = "x-request-id", required = false) String xRequestId) {

        String tipo = type != null ? type : notificacion != null ? notificacion.type() : null;
        if (!"payment".equalsIgnoreCase(tipo)) return ResponseEntity.ok().build();
        if (dataId == null && notificacion != null && notificacion.data() != null) {
            dataId = notificacion.data().id();
        }
        if (dataId == null || !mercadoPagoService.validarWebhook(xSignature, xRequestId, dataId)) {
            log.warn("Webhook de Mercado Pago rechazado por firma invalida o ID ausente");
            return ResponseEntity.status(401).build();
        }

        try {
            mercadoPagoService.procesarPago(Long.valueOf(dataId));
            return ResponseEntity.ok().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("No se pudo procesar el webhook del pago {}", dataId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public record NotificacionPago(String type, DatosPago data) {}
    public record DatosPago(String id) {}
}
