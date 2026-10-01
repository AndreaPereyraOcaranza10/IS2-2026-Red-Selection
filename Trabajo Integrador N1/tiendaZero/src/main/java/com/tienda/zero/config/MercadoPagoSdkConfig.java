package com.tienda.zero.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Inicializa el SDK oficial de Mercado Pago con el Access Token del vendedor.
 * No contiene llamadas HTTP manuales: el SDK se encarga de la comunicación.
 */
@Configuration
public class MercadoPagoSdkConfig {

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @PostConstruct
    public void init() {
        if (accessToken != null && !accessToken.isBlank()) {
            MercadoPagoConfig.setAccessToken(accessToken);
        }
    }
}
