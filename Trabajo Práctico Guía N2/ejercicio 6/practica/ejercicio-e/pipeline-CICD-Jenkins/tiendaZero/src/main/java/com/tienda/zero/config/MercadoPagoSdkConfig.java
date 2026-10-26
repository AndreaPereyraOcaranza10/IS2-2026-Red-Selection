package com.tienda.zero.config;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

/**
 * Inicializa el SDK oficial de Mercado Pago con el Access Token del vendedor.
 * No contiene llamadas HTTP manuales: el SDK se encarga de la comunicación.
 */
@Configuration
public class MercadoPagoSdkConfig {

    @Bean
    public PaymentClient paymentClient() {
        return new PaymentClient();
    }

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @PostConstruct
    public void init() {
        if (accessToken != null && !accessToken.isBlank()) {
            MercadoPagoConfig.setAccessToken(accessToken);
        }
    }
}
