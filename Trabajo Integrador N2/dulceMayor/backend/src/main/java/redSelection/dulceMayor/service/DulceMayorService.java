package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.SucursalImagenSolicitudDTO;
import redSelection.dulceMayor.dto.SucursalRespuestaDTO;
import redSelection.dulceMayor.dto.SucursalSolicitudDTO;
import redSelection.dulceMayor.dto.UbicacionResueltaDTO;
import redSelection.dulceMayor.enums.TipoContacto;
import redSelection.dulceMayor.enums.TipoEmpresa;
import redSelection.dulceMayor.enums.TipoImagen;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.ContactoCorreoElectronico;
import redSelection.dulceMayor.model.Direccion;
import redSelection.dulceMayor.model.DulceMayor;
import redSelection.dulceMayor.model.Imagen;
import redSelection.dulceMayor.repository.DulceMayorRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Dueño de la empresa (casa central y sucursales): ningún otro service ni controller toca DulceMayorRepository,
 * piden lo que necesitan acá. Las sucursales se exponen siempre como DTO.
 */
@Service
public class DulceMayorService {

    private static final int MAX_FOTOS_POR_SUCURSAL = 5;

    private final DulceMayorRepository dulceMayorRepository;
    private final UbicacionService ubicaciones;
    private final ImagenService imagenes;

    public DulceMayorService(DulceMayorRepository dulceMayorRepository, UbicacionService ubicaciones,
                             ImagenService imagenes) {
        this.dulceMayorRepository = dulceMayorRepository;
        this.ubicaciones = ubicaciones;
        this.imagenes = imagenes;
    }

    /** La casa central: el depósito desde donde sale el reparto. */
    @Transactional(readOnly = true)
    public Optional<DulceMayor> sedeCentral() {
        return dulceMayorRepository.findByEliminadoFalse().stream()
                .filter(d -> d.getTipoEmpresa() == TipoEmpresa.SEDE_CENTRAL)
                .findFirst();
    }

    // ---------- Sucursales ----------

    @Transactional(readOnly = true)
    public List<SucursalRespuestaDTO> listarSucursales() {
        return dulceMayorRepository.findByEliminadoFalse().stream()
                .filter(d -> d.getTipoEmpresa() == TipoEmpresa.SUCURSAL)
                .sorted(Comparator.comparing(DulceMayor::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional
    public SucursalRespuestaDTO crearSucursal(SucursalSolicitudDTO solicitud) {
        UbicacionResueltaDTO ubicacion = ubicaciones.resolver(
                solicitud.provinciaId(), solicitud.departamentoId(), solicitud.localidadId());
        DulceMayor sucursal = DulceMayor.builder()
                .nombre(solicitud.razonSocial().trim()).razonSocial(solicitud.razonSocial().trim())
                .cuit(solicitud.cuit().trim()).tipoEmpresa(TipoEmpresa.SUCURSAL)
                .direccion(aDireccion(solicitud, ubicacion))
                .contactos(List.of(contactoDeSucursal(solicitud.email())))
                .build();
        return aRespuesta(dulceMayorRepository.save(sucursal));
    }

    @Transactional
    public SucursalRespuestaDTO actualizarSucursal(String id, SucursalSolicitudDTO solicitud) {
        DulceMayor sucursal = buscarSucursal(id);
        UbicacionResueltaDTO ubicacion = ubicaciones.resolver(
                solicitud.provinciaId(), solicitud.departamentoId(), solicitud.localidadId());
        Direccion direccion = sucursal.getDireccion();
        direccion.setCalle(solicitud.calle().trim());
        direccion.setNumeracion(solicitud.numeracion().trim());
        direccion.setBarrio(limpiar(solicitud.barrio()));
        direccion.setReferencia(limpiar(solicitud.referencia()));
        direccion.setManzanaPiso(limpiar(solicitud.manzanaPiso()));
        direccion.setCasaDepartamento(limpiar(solicitud.casaDepartamento()));
        direccion.setProvinciaId(ubicacion.provincia().id());
        direccion.setProvinciaNombre(ubicacion.provincia().nombre());
        direccion.setDepartamentoId(ubicacion.departamento().id());
        direccion.setDepartamentoNombre(ubicacion.departamento().nombre());
        direccion.setLocalidadId(ubicacion.localidad().id());
        direccion.setLocalidadNombre(ubicacion.localidad().nombre());
        sucursal.setNombre(solicitud.razonSocial().trim());
        sucursal.setRazonSocial(solicitud.razonSocial().trim());
        sucursal.setCuit(solicitud.cuit().trim());
        sucursal.getContactos().stream().filter(c -> c instanceof ContactoCorreoElectronico)
                .forEach(c -> c.setEliminado(true));
        sucursal.getContactos().add(contactoDeSucursal(solicitud.email()));
        return aRespuesta(dulceMayorRepository.save(sucursal));
    }

    @Transactional
    public SucursalRespuestaDTO agregarFoto(String id, SucursalImagenSolicitudDTO solicitud) {
        DulceMayor sucursal = buscarSucursal(id);
        Imagen imagen = imagenes.obtenerEntidad(solicitud.imagenId());
        if (imagen.getTipoImagen() != TipoImagen.FOTO_SUCURSAL) {
            throw new ReglaNegocioException("La imagen no es una foto de sucursal.");
        }
        boolean yaAsociada = sucursal.getImagenes().stream().anyMatch(i -> i.getId().equals(imagen.getId()));
        long fotos = sucursal.getImagenes().stream().filter(i -> i.getTipoImagen() == TipoImagen.FOTO_SUCURSAL).count();
        if (!yaAsociada && fotos >= MAX_FOTOS_POR_SUCURSAL) {
            throw new ReglaNegocioException("Cada sucursal puede tener hasta " + MAX_FOTOS_POR_SUCURSAL + " fotos.");
        }
        if (!yaAsociada) {
            sucursal.getImagenes().add(imagen);
        }
        return aRespuesta(sucursal);
    }

    @Transactional
    public void eliminarSucursal(String id) {
        buscarSucursal(id).setEliminado(true);
    }

    // ---------- Auxiliares ----------

    private DulceMayor buscarSucursal(String id) {
        return dulceMayorRepository.findByIdAndEliminadoFalse(id)
                .filter(d -> d.getTipoEmpresa() == TipoEmpresa.SUCURSAL)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal inexistente."));
    }

    private ContactoCorreoElectronico contactoDeSucursal(String email) {
        return ContactoCorreoElectronico.builder()
                .tipoContacto(TipoContacto.EMPRESA).observacion("Contacto de sucursal")
                .principal(true).email(email.trim()).build();
    }

    private Direccion aDireccion(SucursalSolicitudDTO s, UbicacionResueltaDTO ubicacion) {
        return Direccion.builder().calle(s.calle().trim()).numeracion(s.numeracion().trim())
                .barrio(limpiar(s.barrio())).referencia(limpiar(s.referencia()))
                .manzanaPiso(limpiar(s.manzanaPiso())).casaDepartamento(limpiar(s.casaDepartamento()))
                .provinciaId(ubicacion.provincia().id()).provinciaNombre(ubicacion.provincia().nombre())
                .departamentoId(ubicacion.departamento().id()).departamentoNombre(ubicacion.departamento().nombre())
                .localidadId(ubicacion.localidad().id()).localidadNombre(ubicacion.localidad().nombre())
                .build();
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private SucursalRespuestaDTO aRespuesta(DulceMayor sucursal) {
        Direccion d = sucursal.getDireccion();
        String email = sucursal.getContactos().stream()
                .filter(c -> !c.isEliminado() && c instanceof ContactoCorreoElectronico)
                .map(c -> ((ContactoCorreoElectronico) c).getEmail()).findFirst().orElse("");
        List<String> fotos = sucursal.getImagenes().stream()
                .filter(i -> !i.isEliminado() && i.getTipoImagen() == TipoImagen.FOTO_SUCURSAL)
                .map(i -> "/api/imagenes/" + i.getId()).toList();
        return new SucursalRespuestaDTO(sucursal.getId(), sucursal.getRazonSocial(), sucursal.getCuit(),
                d.getCalle(), d.getNumeracion(), d.getBarrio(), d.getManzanaPiso(), d.getCasaDepartamento(),
                d.getReferencia(), d.getLocalidadId(), d.getLocalidadNombre(), d.getDepartamentoId(),
                d.getDepartamentoNombre(), d.getProvinciaId(), d.getProvinciaNombre(), email, fotos);
    }
}