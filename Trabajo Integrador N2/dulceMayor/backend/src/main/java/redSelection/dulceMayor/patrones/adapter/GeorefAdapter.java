package redSelection.dulceMayor.patrones.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import redSelection.dulceMayor.dto.DireccionNormalizadaDTO;
import redSelection.dulceMayor.dto.UbicacionInversaDTO;
import redSelection.dulceMayor.dto.UnidadTerritorialDTO;
import redSelection.dulceMayor.exception.ServicioExternoException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * PATRÓN: Adapter (estructural) - adaptador.
 * Adapta la API pública Georef (HTTP + JSON propio, ids del INDEC) a la interfaz UbicacionProvider.
 * Si Georef cambia, o se quiere otro proveedor, solo se toca esta clase.
 * Incluye un caché en memoria: evita llamadas repetidas y, si la API se cae,
 * sigue sirviendo el último dato conocido.
 */
@Component
public class GeorefAdapter implements UbicacionProvider {

    private static final Duration VIGENCIA_CACHE = Duration.ofHours(24);
    private static final int MAXIMO_RESULTADOS = 1000; // Georef devuelve pocos por defecto

    private final RestClient cliente;
    private final Map<String, Entrada> cache = new ConcurrentHashMap<>();

    public GeorefAdapter(@Value("${app.georef.url:https://apis.datos.gob.ar/georef/api}") String urlBase) {
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.cliente = RestClient.builder().baseUrl(urlBase).requestFactory(factory).build();
    }

    @Override
    public List<UnidadTerritorialDTO> provincias() {
        return cacheado("provincias", () -> {
            RespuestaProvincias r = cliente.get()
                    .uri(u -> u.path("/provincias")
                            .queryParam("campos", "id,nombre")
                            .queryParam("orden", "nombre")
                            .queryParam("max", MAXIMO_RESULTADOS)
                            .build())
                    .retrieve().body(RespuestaProvincias.class);
            return aUnidades(r == null ? null : r.provincias());
        });
    }

    @Override
    public List<UnidadTerritorialDTO> departamentos(String provinciaId) {
        return cacheado("departamentos:" + provinciaId, () -> {
            RespuestaDepartamentos r = cliente.get()
                    .uri(u -> u.path("/departamentos")
                            .queryParam("provincia", provinciaId)
                            .queryParam("campos", "id,nombre")
                            .queryParam("orden", "nombre")
                            .queryParam("max", MAXIMO_RESULTADOS)
                            .build())
                    .retrieve().body(RespuestaDepartamentos.class);
            return aUnidades(r == null ? null : r.departamentos());
        });
    }

    @Override
    public List<UnidadTerritorialDTO> localidades(String departamentoId) {
        return cacheado("localidades:" + departamentoId, () -> {
            RespuestaLocalidades r = cliente.get()
                    .uri(u -> u.path("/localidades")
                            .queryParam("departamento", departamentoId)
                            .queryParam("campos", "id,nombre")
                            .queryParam("orden", "nombre")
                            .queryParam("max", MAXIMO_RESULTADOS)
                            .build())
                    .retrieve().body(RespuestaLocalidades.class);
            return aUnidades(r == null ? null : r.localidades());
        });
    }

    @Override
    public Optional<DireccionNormalizadaDTO> normalizarDireccion(String calle, String numeracion,
                                                                 String provinciaId, String departamentoId) {
        String texto = (calle + " " + numeracion).trim();
        try {
            RespuestaDirecciones r = cliente.get()
                    .uri(u -> u.path("/direcciones")
                            .queryParam("direccion", "{texto}")
                            .queryParam("provincia", provinciaId)
                            .queryParam("departamento", departamentoId)
                            .queryParam("max", 1)
                            .build(texto))
                    .retrieve().body(RespuestaDirecciones.class);
            if (r == null || r.direcciones() == null || r.direcciones().isEmpty()) {
                return Optional.empty();
            }
            DireccionGeoref d = r.direcciones().get(0);
            Double lat = d.ubicacion() == null ? null : d.ubicacion().lat();
            Double lon = d.ubicacion() == null ? null : d.ubicacion().lon();
            return Optional.of(new DireccionNormalizadaDTO(d.nomenclatura(), lat, lon));
        } catch (RestClientException ex) {
            // La geocodificación es "mejor esfuerzo": si falla, el alta sigue (plan B: ubicación manual).
            return Optional.empty();
        }
    }

    @Override
    public Optional<UbicacionInversaDTO> ubicarCoordenadas(double latitud, double longitud) {
        try {
            RespuestaInversa r = cliente.get()
                    .uri(u -> u.path("/ubicacion")
                            .queryParam("lat", latitud)
                            .queryParam("lon", longitud)
                            .build())
                    .retrieve().body(RespuestaInversa.class);
            if (r == null || r.ubicacion() == null) {
                return Optional.empty();
            }
            Item provincia = r.ubicacion().provincia();
            Item departamento = r.ubicacion().departamento();
            if (provincia == null || provincia.id() == null || departamento == null || departamento.id() == null) {
                return Optional.empty();
            }
            return Optional.of(new UbicacionInversaDTO(
                    new UnidadTerritorialDTO(provincia.id(), provincia.nombre()),
                    new UnidadTerritorialDTO(departamento.id(), departamento.nombre())));
        } catch (RestClientException ex) {
            // Mejor esfuerzo, igual que la geocodificación: si falla, el formulario se completa a mano.
            return Optional.empty();
        }
    }

    // ---------- Caché con vigencia (si la API falla, se usa el dato vencido) ----------

    private record Entrada(Object valor, Instant vence) { }

    @SuppressWarnings("unchecked")
    private <T> T cacheado(String clave, Supplier<T> consulta) {
        Entrada actual = cache.get(clave);
        if (actual != null && actual.vence().isAfter(Instant.now())) {
            return (T) actual.valor();
        }
        try {
            T nuevo = consulta.get();
            cache.put(clave, new Entrada(nuevo, Instant.now().plus(VIGENCIA_CACHE)));
            return nuevo;
        } catch (RestClientException ex) {
            if (actual != null) {
                return (T) actual.valor();
            }
            throw new ServicioExternoException(
                    "El servicio de ubicaciones no está disponible. Intentá de nuevo en unos minutos.", ex);
        }
    }

    private List<UnidadTerritorialDTO> aUnidades(List<Item> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream().map(i -> new UnidadTerritorialDTO(i.id(), i.nombre())).toList();
    }

    // ---------- Formato JSON de Georef (solo los campos que usamos) ----------

    private record Item(String id, String nombre) { }
    private record RespuestaProvincias(List<Item> provincias) { }
    private record RespuestaDepartamentos(List<Item> departamentos) { }
    private record RespuestaLocalidades(List<Item> localidades) { }
    private record Ubicacion(Double lat, Double lon) { }
    private record DireccionGeoref(String nomenclatura, Ubicacion ubicacion) { }
    private record RespuestaDirecciones(List<DireccionGeoref> direcciones) { }
    private record UbicadoEn(Item provincia, Item departamento) { }
    private record RespuestaInversa(UbicadoEn ubicacion) { }
}
