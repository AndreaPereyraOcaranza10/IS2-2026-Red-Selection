package com.tienda.zero.service.impl;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.model.DetalleCompra;
import com.tienda.zero.model.OrdenCompra;
import com.tienda.zero.repository.OrdenCompraRepository;
import com.tienda.zero.service.FlujoCompraService;
import com.tienda.zero.service.MercadoPagoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MercadoPagoServiceImpl implements MercadoPagoService {

    private final OrdenCompraRepository ordenes;
    private final FlujoCompraService flujoCompraService;

    @Value("${mercadopago.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${mercadopago.webhook-url:}")
    private String webhookUrl;

    @Value("${mercadopago.webhook-secret:}")
    private String webhookSecret;

    @Override
    public String crearCheckout(OrdenCompra orden) {
        if (orden == null || orden.getId() == null) {
            throw new IllegalArgumentException("La orden no es válida para Mercado Pago");
        }
        if (orden.getFormaPago() != TipoPago.BILLETERA_VIRTUAL) {
            throw new IllegalArgumentException("La orden no utiliza Mercado Pago");
        }
        if (orden.getDetalles() == null || orden.getDetalles().stream().noneMatch(d -> !d.isEliminado())) {
            throw new IllegalArgumentException("La orden no tiene productos");
        }

        List<PreferenceItemRequest> items = new ArrayList<>();
        for (DetalleCompra detalle : orden.getDetalles()) {
            if (detalle.isEliminado()) continue;
            BigDecimal unitPrice = detalle.getSubtotal()
                    .divide(BigDecimal.valueOf(detalle.getCantidad()), 2, java.math.RoundingMode.HALF_UP);

            items.add(PreferenceItemRequest.builder()
                    .id(detalle.getProducto().getId())
                    .title(detalle.getProducto().getNombre())
                    .description("Producto de Tienda Zero")
                    .quantity(detalle.getCantidad())
                    .unitPrice(unitPrice)
                    .currencyId("ARS")
                    .build());
        }

        String success = baseUrl + "/checkout/mercadopago/success";
        String pending = baseUrl + "/checkout/mercadopago/pending";
        String failure = baseUrl + "/checkout/mercadopago/failure";

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(success)
                .pending(pending)
                .failure(failure)
                .build();


        PreferenceRequest.PreferenceRequestBuilder builder = PreferenceRequest.builder()
                .items(items)
                .externalReference(orden.getIdentificadorCompra())
                .backUrls(backUrls)
                .binaryMode(false)
                .statementDescriptor("TIENDA ZERO");

        if (baseUrl.startsWith("https://")) {
            builder.autoReturn("approved");
        }

        // notification_url solo si hay una URL pública configurada
        if (webhookUrl != null && !webhookUrl.isBlank()) {
            builder.notificationUrl(webhookUrl);
        }

        PreferenceRequest preferenceRequest = builder.build();

        try {
            Preference preference = new PreferenceClient().create(preferenceRequest);
            if (preference == null || preference.getId() == null || preference.getInitPoint() == null) {
                throw new IllegalStateException("Mercado Pago no devolvió una preferencia válida");
            }
            flujoCompraService.guardarDatosMercadoPago(orden.getId(), preference.getId());
            return preference.getInitPoint();
        } catch (MPApiException e) {
            throw new IllegalStateException(
                    "No se pudo crear el checkout de Mercado Pago. Revisá la consola para ver el detalle.",
                    e
            );

        } catch (MPException e) {
            System.err.println("========== MERCADO PAGO SDK ERROR ==========");
            e.printStackTrace();

            throw new IllegalStateException(
                    "No se pudo comunicar con Mercado Pago. Intentá nuevamente.",
                    e
            );
        }

    }


    @Override
    @Transactional
    public void procesarPago(Long paymentId) {
        if (paymentId == null) return;

        try {
            Payment payment = new PaymentClient().get(paymentId);
            if (payment == null || payment.getExternalReference() == null) return;

            OrdenCompra orden = ordenes.findByIdentificadorCompra(payment.getExternalReference()).orElse(null);
            if (orden == null) return;

            orden.setMpPaymentId(payment.getId());
            orden.setMpPaymentStatus(payment.getStatus());

            BigDecimal montoEsperado = orden.getTotal();
            BigDecimal montoPagado = payment.getTransactionAmount();
            boolean montoCorrecto = montoPagado != null && montoEsperado != null
                    && montoPagado.compareTo(montoEsperado) == 0;

            if ("approved".equalsIgnoreCase(payment.getStatus()) && montoCorrecto) {
                if (orden.getEstadoOrdenCompra() == EstadoOrdenCompra.PENDIENTE_PAGO) {
                    orden.setEstadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_ENVIO);
                }
            } else if ("rejected".equalsIgnoreCase(payment.getStatus())
                    || "cancelled".equalsIgnoreCase(payment.getStatus())) {
                // No liberamos stock automáticamente en un pago rechazado porque
                // el cliente podría reintentar el pago desde el mismo checkout.
                // La orden sigue PENDIENTE_PAGO y puede ser anulada desde la tienda.
            }

            ordenes.save(orden);
        } catch (MPApiException | MPException e) {
            throw new IllegalStateException("No se pudo consultar el pago en Mercado Pago", e);
        }
    }

    @Override
    public boolean validarWebhook(String xSignature, String xRequestId, String dataId) {
        if (webhookSecret == null || webhookSecret.isBlank()) return false;
        if (xSignature == null || xRequestId == null || dataId == null) return false;

        String ts = null;
        String v1 = null;
        for (String part : xSignature.split(",")) {
            String[] pair = part.split("=", 2);
            if (pair.length != 2) continue;
            if ("ts".equals(pair[0])) ts = pair[1];
            if ("v1".equals(pair[0])) v1 = pair[1];
        }
        if (ts == null || v1 == null) return false;

        String manifest = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return MessageDigest.isEqual(hex.toString().getBytes(StandardCharsets.UTF_8), v1.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}
