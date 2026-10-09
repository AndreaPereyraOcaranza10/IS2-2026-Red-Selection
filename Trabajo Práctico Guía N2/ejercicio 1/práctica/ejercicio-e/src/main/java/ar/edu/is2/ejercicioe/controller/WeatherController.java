package ar.edu.is2.ejercicioe.controller;

import ar.edu.is2.ejercicioe.model.WeatherResponse;
import ar.edu.is2.ejercicioe.service.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    private final WeatherService weatherService;
    public WeatherController(WeatherService weatherService) { this.weatherService = weatherService; }

    @GetMapping
    public WeatherResponse getWeather(@RequestParam String city) {
        try { return weatherService.forecast(city); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()); }
        catch (IllegalStateException e) { throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage()); }
    }
}
