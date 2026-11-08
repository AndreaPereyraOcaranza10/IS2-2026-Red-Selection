package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import redSelection.dulceMayor.dto.DireccionNormalizadaDTO;
import redSelection.dulceMayor.dto.UbicacionInversaDTO;
import redSelection.dulceMayor.dto.UbicacionResueltaDTO;
import redSelection.dulceMayor.dto.UnidadTerritorialDTO;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.patrones.adapter.UbicacionProvider;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UbicacionService {

    private static final Pattern ID_VALIDO = Pattern.compile("\\d{1,11}");

    private final UbicacionProvider proveedor;

    public UbicacionService(UbicacionProvider proveedor) {
        this.proveedor = proveedor;
    }

    public List<UnidadTerritorialDTO> provincias() {
        return proveedor.provincias();
    }

    public List<UnidadTerritorialDTO> departamentos(String provinciaId) {
        validarId(provinciaId, "provincia");
        return proveedor.departamentos(provinciaId);
    }

    public List<UnidadTerritorialDTO> localidades(String departamentoId) {
        validarId(departamentoId, "departamento");
        return proveedor.localidades(departamentoId);
    }

    /*
      Verifica que el departamento pertenezca a la provincia y la localidad al departamento.
      Los nombres que se guardan son los oficiales de la API, no los que mande el cliente.
     */
    public UbicacionResueltaDTO resolver(String provinciaId, String departamentoId, String localidadId) {
        validarId(provinciaId, "provincia");
        validarId(departamentoId, "departamento");
        validarId(localidadId, "localidad");
        UnidadTerritorialDTO provincia = buscar(proveedor.provincias(), provinciaId, "La provincia");
        UnidadTerritorialDTO departamento = buscar(proveedor.departamentos(provinciaId), departamentoId, "El departamento");
        UnidadTerritorialDTO localidad = buscar(proveedor.localidades(departamentoId), localidadId, "La localidad");
        return new UbicacionResueltaDTO(provincia, departamento, localidad);
    }

    public Optional<DireccionNormalizadaDTO> geocodificar(String calle, String numeracion, UbicacionResueltaDTO ubicacion) {
        return proveedor.normalizarDireccion(calle, numeracion,
                ubicacion.provincia().id(), ubicacion.departamento().id());
    }

    /** Provincia y departamento de unas coordenadas dentro de Argentina; vacío si Georef no las ubica. */
    public Optional<UbicacionInversaDTO> inversa(double latitud, double longitud) {
        // La forma negada también rechaza NaN.
        if (!(latitud >= -56 && latitud <= -21 && longitud >= -74 && longitud <= -53)) {
            throw new ReglaNegocioException("Las coordenadas están fuera de Argentina");
        }
        return proveedor.ubicarCoordenadas(latitud, longitud);
    }

    private UnidadTerritorialDTO buscar(List<UnidadTerritorialDTO> opciones, String id, String descripcion) {
        return opciones.stream()
                .filter(o -> o.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(descripcion + " no es válida para la selección"));
    }

    private void validarId(String id, String campo) {
        if (id == null || !ID_VALIDO.matcher(id).matches()) {
            throw new ReglaNegocioException("El identificador de " + campo + " no es válido");
        }
    }
}
