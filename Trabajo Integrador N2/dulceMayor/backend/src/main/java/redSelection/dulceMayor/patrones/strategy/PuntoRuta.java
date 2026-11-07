package redSelection.dulceMayor.patrones.strategy;

/** Punto geográfico con identificador. Es lo único que el optimizador necesita saber de una parada. */
public record PuntoRuta(String id, double latitud, double longitud) {

    private static final double RADIO_TIERRA_KM = 6371.0088;

    /** Distancia en línea recta sobre la esfera (Haversine), en kilómetros. */
    public double distanciaKm(PuntoRuta otro) {
        double dLat = Math.toRadians(otro.latitud - latitud);
        double dLon = Math.toRadians(otro.longitud - longitud);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitud)) * Math.cos(Math.toRadians(otro.latitud))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }
}
