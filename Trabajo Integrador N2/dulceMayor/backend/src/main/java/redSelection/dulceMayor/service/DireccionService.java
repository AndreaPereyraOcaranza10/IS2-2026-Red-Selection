package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.DireccionCrearDTO;
import redSelection.dulceMayor.dto.DireccionRespuestaDTO;
import redSelection.dulceMayor.dto.UbicacionResueltaDTO;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Direccion;
import redSelection.dulceMayor.repository.DireccionRepository;

/*
 Los nombres de provincia/departamento/localidad salen de Georef (vía UbicacionService), nunca del cliente.
 Las coordenadas son obligatorias: con ellas se ordena la ruta y se dibuja el mapa.
 */
@Service
public class DireccionService {

    private record Coordenadas(double latitud, double longitud) { }

    private final DireccionRepository direccionRepository;
    private final UbicacionService ubicacionService;

    public DireccionService(DireccionRepository direccionRepository, UbicacionService ubicacionService) {
        this.direccionRepository = direccionRepository;
        this.ubicacionService = ubicacionService;
    }

    @Transactional
    public Direccion crear(DireccionCrearDTO dto) {
        Direccion direccion = new Direccion();
        completar(direccion, dto);
        return direccionRepository.save(direccion);
    }

    @Transactional
    public Direccion actualizar(Direccion direccion, DireccionCrearDTO dto) {
        completar(direccion, dto);
        return direccionRepository.save(direccion);
    }

    public DireccionRespuestaDTO aRespuesta(Direccion d) {
        String texto = d.getCalle() + " " + d.getNumeracion() + ", " + d.getLocalidadNombre()
                + ", " + d.getDepartamentoNombre() + ", " + d.getProvinciaNombre();
        return new DireccionRespuestaDTO(d.getCalle(), d.getNumeracion(), d.getBarrio(), d.getReferencia(),
                d.getProvinciaId(), d.getProvinciaNombre(), d.getDepartamentoId(), d.getDepartamentoNombre(),
                d.getLocalidadId(), d.getLocalidadNombre(), d.getLatitud(), d.getLongitud(), texto);
    }

    private void completar(Direccion d, DireccionCrearDTO dto) {
        UbicacionResueltaDTO ubicacion = ubicacionService.resolver(
                dto.provinciaId(), dto.departamentoId(), dto.localidadId());
        Coordenadas coordenadas = coordenadasDe(dto, ubicacion);

        d.setCalle(dto.calle().trim());
        d.setNumeracion(dto.numeracion().trim());
        d.setBarrio(vacioANulo(dto.barrio()));
        d.setReferencia(vacioANulo(dto.referencia()));
        d.setProvinciaId(ubicacion.provincia().id());
        d.setProvinciaNombre(ubicacion.provincia().nombre());
        d.setDepartamentoId(ubicacion.departamento().id());
        d.setDepartamentoNombre(ubicacion.departamento().nombre());
        d.setLocalidadId(ubicacion.localidad().id());
        d.setLocalidadNombre(ubicacion.localidad().nombre());
        d.setLatitud(coordenadas.latitud());
        d.setLongitud(coordenadas.longitud());
    }

    /** Si el cliente mandó coordenadas (GPS en el comercio) mandan esas; si no, se geocodifica con Georef. */
    private Coordenadas coordenadasDe(DireccionCrearDTO dto, UbicacionResueltaDTO ubicacion) {
        boolean hayLatitud = dto.latitud() != null;
        boolean hayLongitud = dto.longitud() != null;
        if (hayLatitud != hayLongitud) {
            throw new ReglaNegocioException("La latitud y la longitud se envían juntas.");
        }
        if (hayLatitud) {
            return new Coordenadas(dto.latitud(), dto.longitud());
        }
        return ubicacionService.geocodificar(dto.calle().trim(), dto.numeracion().trim(), ubicacion)
                .filter(g -> g.latitud() != null && g.longitud() != null)
                .map(g -> new Coordenadas(g.latitud(), g.longitud()))
                .orElseThrow(() -> new ReglaNegocioException(
                        "No pudimos ubicar la dirección en el mapa. Revisá la calle y la numeración, "
                                + "o usá «Usar mi ubicación» estando en el comercio."));
    }

    private static String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
