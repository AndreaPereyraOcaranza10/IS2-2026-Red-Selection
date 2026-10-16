package redselection.cliente.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import redselection.cliente.dto.DomicilioDTO;
import redselection.cliente.dto.PersonaDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class PersonaClientService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PersonaClientService(
            RestTemplate restTemplate,
            @Value("${api.base-url}") String apiBaseUrl) {

        this.restTemplate = restTemplate;
        this.baseUrl = apiBaseUrl + "/personas";
    }

    public List<PersonaDTO> listar() {
        PersonaDTO[] respuesta = restTemplate.getForObject(baseUrl, PersonaDTO[].class);
        return respuesta == null ? List.of() : Arrays.asList(respuesta);
    }

    public List<PersonaDTO> buscar(String filtro) {
        PersonaDTO[] respuesta = restTemplate.getForObject(baseUrl + "/search?filtro={filtro}", PersonaDTO[].class, filtro);
        return respuesta == null ? List.of() : Arrays.asList(respuesta);
    }

    public PersonaDTO obtener(Long id) {
        return restTemplate.getForObject(baseUrl + "/{id}", PersonaDTO.class, id);
    }

    public PersonaDTO crear(PersonaDTO persona) {
        return restTemplate.postForObject(baseUrl, persona, PersonaDTO.class);
    }

    /**
     * El servidor hace save() con lo que recibe, así que hay que mandarle la entidad completa con sus ids
     * (persona y domicilio). Por eso se trae la persona actual y solo se pisan los campos del formulario.
     * Los libros no se tocan desde acá: el servidor conserva los que ya tiene asignados.
     */
    public void actualizar(Long id, PersonaDTO formulario) {
        PersonaDTO existente = obtener(id);
        existente.asegurarDomicilio();

        existente.setNombre(formulario.getNombre());
        existente.setApellido(formulario.getApellido());
        existente.setDni(formulario.getDni());

        DomicilioDTO domicilio = existente.getDomicilio();
        domicilio.setCalle(formulario.getDomicilio().getCalle());
        domicilio.setNumero(formulario.getDomicilio().getNumero());
        domicilio.setLatitud(formulario.getDomicilio().getLatitud());
        domicilio.setLongitud(formulario.getDomicilio().getLongitud());
        domicilio.setLocalidad(formulario.getDomicilio().getLocalidad());
        existente.setEmail(formulario.getEmail());
        existente.setFechaNacimiento(formulario.getFechaNacimiento());

        restTemplate.put(baseUrl + "/{id}", existente, id);
    }


    /**
     * Asigna a la persona un libro que ya existe en el catálogo. No crea ningún libro:
     * el servidor solo vincula persona y libro (y guarda la fecha de devolución, si se indicó).
     */
    public void asociarLibro(Long personaId, Long libroId, LocalDate fechaDevolucion) {
        UriComponentsBuilder uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/{personaId}/libros/{libroId}");
        if (fechaDevolucion != null) {
            uri.queryParam("fechaDevolucion", fechaDevolucion);
        }

        restTemplate.postForObject(uri.buildAndExpand(personaId, libroId).toUri(), null, Void.class);
    }

    /** Solo desvincula el libro de la persona: el libro sigue existiendo en el catálogo. */
    public void quitarLibro(Long personaId, Long libroId) {
        restTemplate.delete(baseUrl + "/{personaId}/libros/{libroId}", personaId, libroId);
    }

    public void eliminar(Long id) {
        restTemplate.delete(baseUrl + "/{id}", id);
    }
}