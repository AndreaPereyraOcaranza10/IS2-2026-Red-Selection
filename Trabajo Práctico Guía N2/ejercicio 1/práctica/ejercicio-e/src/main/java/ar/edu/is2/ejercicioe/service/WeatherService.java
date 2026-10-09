package ar.edu.is2.ejercicioe.service;

import java.net.URI;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import ar.edu.is2.ejercicioe.model.WeatherResponse;

@Service
public class WeatherService {
    private final RestTemplate restTemplate;
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";

    public WeatherService(RestTemplate restTemplate) { this.restTemplate = restTemplate; }

    @SuppressWarnings("unchecked") //para evitar advertencias de conversión de tipos al usar Map<String, Object>
    public WeatherResponse forecast(String city) {
        if (city == null || city.isBlank()) throw new IllegalArgumentException("Ingresá el nombre de una ciudad.");
        try {
            URI geoUri = UriComponentsBuilder.fromUriString(GEOCODING_URL)
                    .queryParam("name", city.trim()).queryParam("count", 1)
                    .queryParam("language", "es").queryParam("format", "json").build().encode().toUri();
            Map<String, Object> geo = restTemplate.getForObject(geoUri, Map.class);
            if (geo == null || !(geo.get("results") instanceof java.util.List<?> results) || results.isEmpty())
                throw new IllegalArgumentException("No encontramos esa ciudad. Probá con otro nombre.");
            Map<String, Object> place = (Map<String, Object>) results.get(0);
            Object latitude = place.get("latitude"), longitude = place.get("longitude");
            URI forecastUri = UriComponentsBuilder.fromUriString(FORECAST_URL)
                    .queryParam("latitude", latitude).queryParam("longitude", longitude)
                    .queryParam("current", "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m")
                    .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code")
                    .queryParam("timezone", "auto").queryParam("forecast_days", 5).build().encode().toUri();
            Map<String, Object> result = restTemplate.getForObject(forecastUri, Map.class);
            if (result == null) throw new IllegalStateException("La API no devolvió datos del pronóstico.");
            Map<String, Object> currentMap = (Map<String, Object>) result.get("current");
            Map<String, Object> dailyMap = (Map<String, Object>) result.get("daily");
            WeatherResponse.Current current = new WeatherResponse.Current(
                    (String) currentMap.get("time"), number(currentMap, "temperature_2m"),
                    number(currentMap, "apparent_temperature"), integer(currentMap, "relative_humidity_2m"),
                    integer(currentMap, "weather_code"), number(currentMap, "wind_speed_10m"));
            WeatherResponse.Daily daily = new WeatherResponse.Daily(
                    (java.util.List<String>) dailyMap.get("time"), (java.util.List<Number>) dailyMap.get("temperature_2m_max"),
                    (java.util.List<Number>) dailyMap.get("temperature_2m_min"), (java.util.List<Integer>) dailyMap.get("weather_code"),
                    (java.util.List<Number>) dailyMap.get("precipitation_probability_max"));
            return new WeatherResponse((String) place.get("name"), (String) place.getOrDefault("country", ""),
                    ((Number) latitude).doubleValue(), ((Number) longitude).doubleValue(), current, daily);
        } catch (RestClientException e) {
            throw new IllegalStateException("No pudimos conectarnos con el servicio meteorológico. Intentá de nuevo más tarde.", e);
        }
    }
    private double number(Map<String, Object> map, String key) { return ((Number) map.get(key)).doubleValue(); }
    private int integer(Map<String, Object> map, String key) { return ((Number) map.get(key)).intValue(); }
}
