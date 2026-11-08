package redSelection.dulceMayor.service;

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
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(3));
        client = RestClient.builder().baseUrl("https://monedapi.ar/api/v2").requestFactory(factory).build();
    }

    public DollarQuote obtenerCotizacion() {
        CachedQuote current = cached;
        if (current != null && Duration.between(current.fetchedAt(), Instant.now()).compareTo(CACHE_TTL) < 0) {
            return current.quote();
        }
        try {
            JsonNode source = client.get().uri("/usd/bna").retrieve().body(JsonNode.class);
            if (source == null || !source.hasNonNull("sell") || source.path("sell").asDouble() <= 0) {
                throw new IllegalStateException("La fuente no devolvió una cotización válida.");
            }
            DollarQuote quote = new DollarQuote("USD", "Banco Nación",
                    source.path("buy").asDouble(0), source.path("sell").asDouble(),
                    source.path("updatedAt").asText(""), source.path("lastScrapedAt").asText(""),
                    false, "https://www.bna.com.ar/Personas");
            cached = new CachedQuote(quote, Instant.now());
            return quote;
        } catch (Exception exception) {
            if (current != null) {
                DollarQuote old = current.quote();
                return new DollarQuote(old.currency(), old.source(), old.buy(), old.sell(), old.updatedAt(),
                        old.lastScrapedAt(), true, old.sourceUrl());
            }
            throw new IllegalStateException("La cotización del Banco Nación no está disponible.", exception);
        }
    }

    private record CachedQuote(DollarQuote quote, Instant fetchedAt) {}
    public record DollarQuote(String currency, String source, double buy, double sell,
                              String updatedAt, String lastScrapedAt, boolean stale, String sourceUrl) {}
}