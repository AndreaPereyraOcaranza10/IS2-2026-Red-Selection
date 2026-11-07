package redSelection.dulceMayor.patrones.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import redSelection.dulceMayor.dto.TrayectoDTO;
import redSelection.dulceMayor.exception.ServicioExternoException;
import redSelection.dulceMayor.patrones.strategy.PlanOptimizado;
import redSelection.dulceMayor.patrones.strategy.PuntoRuta;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * PATRÓN: Adapter (estructural) - adaptador.
 * Adapta la API HTTP de OSRM (OpenStreetMap Routing Machine, formato GeoJSON con [lng, lat]) a la interfaz
 * RuteoProvider, que habla en PuntoRuta y [lat, lng]. Si se cambia de proveedor (OpenRouteService, GraphHopper)
 * solo se reemplaza esta clase.
 * La URL se configura con app.osrm.url (variable APP_OSRM_URL). El servidor público es solo para desarrollo:
 * en producción conviene uno propio.
 */
@Component
public class OsrmAdapter implements RuteoProvider {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OsrmAdapter.class);

    private final RestClient cliente;
    private final String urlBase;

    public OsrmAdapter(@Value("${app.osrm.url:https://router.project-osrm.org}") String urlBase) {
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(10));
        // Pedimos la respuesta sin comprimir: OSRM contesta con "deflate" y el cliente de Java falla al descomprimirla
        // (ZipException: incorrect header check). El JSON es chico, así que no se pierde nada.
        this.cliente = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("Accept-Encoding", "identity")
                .build();
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
    }

    @Override
    public TrayectoDTO trayecto(PuntoRuta desde, PuntoRuta hasta) {
        RespuestaRuta r = consultar(
                urlBase + "/route/v1/driving/" + coordenadas(List.of(desde, hasta)) + "?overview=full&geometries=geojson",
                RespuestaRuta.class);
        if (r == null || !"Ok".equals(r.code()) || r.routes() == null || r.routes().isEmpty()) {
            throw new ServicioExternoException("No encontramos una ruta hasta ese punto.", null);
        }
        Camino camino = r.routes().get(0);
        return new TrayectoDTO(camino.distance() / 1000.0, minutos(camino.duration()), aLatLng(camino.geometry()));
    }

    @Override
    public PlanOptimizado recorridoOptimo(List<PuntoRuta> puntos) {
        RespuestaTrip r = consultar(
                urlBase + "/trip/v1/driving/" + coordenadas(puntos)
                        + "?source=first&roundtrip=false&overview=full&geometries=geojson",
                RespuestaTrip.class);
        if (r == null || !"Ok".equals(r.code()) || r.trips() == null || r.trips().isEmpty()
                || r.waypoints() == null || r.waypoints().size() != puntos.size()) {
            throw new ServicioExternoException("El servicio de rutas no pudo ordenar las paradas.", null);
        }
        Camino camino = r.trips().get(0);
        // waypoints[i] corresponde al punto i de la consulta; waypoint_index es su posición en el recorrido.
        List<String> orden = IntStream.range(0, puntos.size())
                .boxed()
                .sorted(Comparator.comparingInt(i -> r.waypoints().get(i).waypoint_index()))
                .map(i -> puntos.get(i).id())
                .toList();
        return new PlanOptimizado(orden, camino.distance() / 1000.0, minutos(camino.duration()),
                aLatLng(camino.geometry()), PlanOptimizado.FUENTE_OSRM);
    }

    private <T> T consultar(String url, Class<T> tipo) {
        try {
            return cliente.get().uri(URI.create(url)).retrieve().body(tipo);
        } catch (RestClientException ex) {
            LOG.warn("OSRM no respondió ({})", url, ex);
            throw new ServicioExternoException(
                    "El servicio de rutas no está disponible. Intentá de nuevo en unos minutos.", ex);
        }
    }

    /** OSRM espera "lng,lat;lng,lat;...". */
    private String coordenadas(List<PuntoRuta> puntos) {
        return puntos.stream()
                .map(p -> p.longitud() + "," + p.latitud())
                .reduce((a, b) -> a + ";" + b)
                .orElseThrow();
    }

    private int minutos(double segundos) {
        return Math.max(1, (int) Math.round(segundos / 60.0));
    }

    /** GeoJSON [lng, lat] -> [lat, lng]. */
    private List<double[]> aLatLng(Geometria geometria) {
        if (geometria == null || geometria.coordinates() == null) {
            return List.of();
        }
        return geometria.coordinates().stream()
                .map(c -> new double[]{c.get(1), c.get(0)})
                .toList();
    }

    // ---------- Formato JSON de OSRM (solo los campos que usamos) ----------

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Geometria(List<List<Double>> coordinates) { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Camino(double distance, double duration, Geometria geometry) { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaRuta(String code, List<Camino> routes) { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Waypoint(int waypoint_index) { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaTrip(String code, List<Camino> trips, List<Waypoint> waypoints) { }
}