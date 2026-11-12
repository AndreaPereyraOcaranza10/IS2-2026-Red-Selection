package redSelection.dulceMayor.service;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

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
            Map<String, Object> source = client.get().uri("/usd/bna").retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            double sell = number(source, "sell");
            if (source == null || !Double.isFinite(sell) || sell <= 0) {
                throw new IllegalStateException("La fuente no devolvió una cotización válida.");
            }
            DollarQuote quote = new DollarQuote("USD", "Banco Nación",
                    number(source, "buy"), sell,
                    text(source, "updatedAt"), text(source, "lastScrapedAt"),
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

    private static double number(Map<String, Object> source, String field) {
        if (source == null) return 0;
        Object value = source.get(field);
        return value instanceof Number number ? number.doubleValue() : 0;
    }

    private static String text(Map<String, Object> source, String field) {
        if (source == null) return "";
        Object value = source.get(field);
        return value == null ? "" : value.toString();
    }

    private record CachedQuote(DollarQuote quote, Instant fetchedAt) {}
    public record DollarQuote(String currency, String source, double buy, double sell,
                              String updatedAt, String lastScrapedAt, boolean stale, String sourceUrl) {}
}
