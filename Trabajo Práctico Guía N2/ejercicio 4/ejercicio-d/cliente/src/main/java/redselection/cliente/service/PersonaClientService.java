package redselection.cliente.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redselection.cliente.dto.AutorDTO;
import redselection.cliente.dto.DomicilioDTO;
import redselection.cliente.dto.LibroDTO;
import redselection.cliente.dto.PersonaDTO;
import redselection.cliente.patrones.builder.LibroBuilderImpl;
import redselection.cliente.patrones.builder.LibroDirector;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

@Service
public class PersonaClientService {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final LibroClientService libroService;

    public PersonaClientService(
            RestTemplate restTemplate,
            @Value("${api.base-url}") String apiBaseUrl,
            LibroClientService libroService) {

        this.restTemplate = restTemplate;
        this.baseUrl = apiBaseUrl + "/personas";
        this.libroService = libroService;
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
     * (persona, domicilio y libros). Por eso se trae la persona actual y solo se pisan los campos del formulario.
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


    public void asociarLibro(Long personaId, Long libroId) {
        PersonaDTO persona = obtener(personaId);
        if (persona.getLibros() == null) {
            persona.setLibros(new ArrayList<>());
        }

        LibroDTO libro = libroService.obtener(libroId);

        if (libro == null) {
            throw new IllegalArgumentException(
                    "No existe el libro con ID: " + libroId);
        }

        boolean yaAsociado = persona.getLibros().stream()
                .anyMatch(l -> libroId.equals(l.getId()));

        if (yaAsociado) {
            throw new IllegalArgumentException(
                    "El libro ya está asociado a esta persona.");
        }

        persona.getLibros().add(libro);

        restTemplate.put(baseUrl + "/{id}", persona, personaId);
    }


    /** Al sacarlo de la lista, el servidor lo elimina (Persona.libros tiene orphanRemoval). */
    public void quitarLibro(Long personaId, Long libroId) {
        PersonaDTO existente = obtener(personaId);
        existente.getLibros().removeIf(l -> libroId.equals(l.getId()));

        restTemplate.put(baseUrl + "/{id}", existente, personaId);
    }

    public void eliminar(Long id) {
        restTemplate.delete(baseUrl + "/{id}", id);
    }
}
