package redselection.cliente.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import redselection.cliente.security.JwtBearerInterceptor;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // Cada llamada a la API lleva el JWT del usuario logueado
        restTemplate.getInterceptors().add(new JwtBearerInterceptor());
        return restTemplate;
    }
}
