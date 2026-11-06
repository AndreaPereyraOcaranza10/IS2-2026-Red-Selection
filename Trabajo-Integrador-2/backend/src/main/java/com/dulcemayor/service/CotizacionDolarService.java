package com.dulcemayor.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;

@Service
public class CotizacionDolarService {
    private static final Duration CACHE_TTL = Duration.ofMinutes(1);
    private final RestClient client;
    private volatile CachedQuote cached;

    public CotizacionDolarService() {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(3));
        this.client = RestClient.builder()
                .baseUrl("https://monedapi.ar/api/v2")
                .requestFactory(requestFactory)
                .build();
    }

    public DollarQuote obtenerCotizacion() {
        CachedQuote current = cached;
        if (current != null && Duration.between(current.fetchedAt(), Instant.now()).compareTo(CACHE_TTL) < 0) {
            return current.quote();
        }
        try {
            JsonNode source = client.get().uri("/usd/bna").retrieve().body(JsonNode.class);
            if (source == null || !source.hasNonNull("sell")) {
                throw new IllegalStateException("La fuente no devolvió una cotización de venta.");
            }
            double buy = source.path("buy").asDouble(0);
            double sell = source.path("sell").asDouble(0);
            if (sell <= 0) throw new IllegalStateException("La cotización de venta no es válida.");
            DollarQuote quote = new DollarQuote("USD", "Banco Nación", buy, sell,
                    source.path("updatedAt").asText(""), source.path("lastScrapedAt").asText(""),
                    false, "https://www.bna.com.ar/Personas");
            cached = new CachedQuote(quote, Instant.now());
            return quote;
        } catch (Exception exception) {
            if (current != null) {
                DollarQuote last = current.quote();
                return new DollarQuote(last.currency(), last.source(), last.buy(), last.sell(),
                        last.updatedAt(), last.lastScrapedAt(), true, last.sourceUrl());
            }
            throw new IllegalStateException("La cotización del Banco Nación no está disponible en este momento.", exception);
        }
    }

    private record CachedQuote(DollarQuote quote, Instant fetchedAt) {}

    public record DollarQuote(String currency, String source, double buy, double sell,
                              String updatedAt, String lastScrapedAt, boolean stale, String sourceUrl) {}
}
