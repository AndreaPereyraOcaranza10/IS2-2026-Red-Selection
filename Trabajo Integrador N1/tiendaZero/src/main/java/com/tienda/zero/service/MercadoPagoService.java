package com.tienda.zero.service;

import com.tienda.zero.model.OrdenCompra;

/** Integración de tiendaZero con Mercado Pago mediante el SDK oficial. */
public interface MercadoPagoService {

    /** Crea una Preference y devuelve la URL de Checkout Pro. */
    String crearCheckout(OrdenCompra orden);

    /** Procesa una notificación de pago consultando el pago mediante el SDK. */
    void procesarPago(Long paymentId);

    /** Valida la firma del webhook de Mercado Pago. */
    boolean validarWebhook(String xSignature, String xRequestId, String dataId);
}
