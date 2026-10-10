package com.redselection.clima.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ClimaService {

    @Value("${weather.api.key}")
    private String apiKey;

    public ResponseEntity<String> obtenerClimaApi(String ciudad) {
        try {
            String url = "https://api.openweathermap.org/data/2.5/weather?q="
                    + ciudad
                    + "&appid="
                    + apiKey;

            RestTemplate restTemplate = new RestTemplate();
            String climaJson = restTemplate.getForObject(url, String.class);

            return ResponseEntity.ok(climaJson);

        } catch (HttpClientErrorException.Unauthorized e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error al autenticarse con la API de clima");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los datos del clima: " + e.getMessage());
        }
    }
}