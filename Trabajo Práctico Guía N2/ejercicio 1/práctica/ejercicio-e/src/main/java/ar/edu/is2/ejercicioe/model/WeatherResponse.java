package ar.edu.is2.ejercicioe.model;

import java.util.List;

public record WeatherResponse(String city, String country, double latitude, double longitude,
                              Current current, Daily daily) {
    public record Current(String time, double temperature, double apparentTemperature,
                          int relativeHumidity, int weatherCode, double windSpeed) {}
    public record Daily(List<String> time, List<Number> temperatureMax, List<Number> temperatureMin,
                        List<Integer> weatherCode, List<Number> precipitationProbabilityMax) {}
}
