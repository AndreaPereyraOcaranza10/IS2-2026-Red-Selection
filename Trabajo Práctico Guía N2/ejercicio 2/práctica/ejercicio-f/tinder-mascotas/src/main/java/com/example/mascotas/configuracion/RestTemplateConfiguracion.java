package com.example.mascotas.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguracion {
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
