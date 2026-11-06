package com.dulcemayor.service.impl;

import com.dulcemayor.enums.TipoDocumento;
import com.dulcemayor.model.Cliente;
import com.dulcemayor.model.Contacto;
import com.dulcemayor.model.Direccion;
import com.dulcemayor.model.Nacionalidad;
import com.dulcemayor.repository.ClienteRepository;
import com.dulcemayor.service.ClienteService;
import com.dulcemayor.service.NacionalidadService;
import com.dulcemayor.service.PersonaService;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final PersonaService personaService;
    private final NacionalidadService nacionalidadService;

    public ClienteServiceImpl(ClienteRepository clienteRepository, PersonaService personaService,
                              NacionalidadService nacionalidadService) {
        this.clienteRepository = clienteRepository;
        this.personaService = personaService;
        this.nacionalidadService = nacionalidadService;
    }

    @Override
    public Cliente crearCliente(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                                String numeroDocumento, String direccionEstadia, String idNacionalidad) {
        validar(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento, direccionEstadia, idNacionalidad);

        Nacionalidad nacionalidad = nacionalidadService.buscarNacionalidad(idNacionalidad);

        Cliente cliente = Cliente.builder()
                .nombre(nombre)
                .apellido(apellido)
                .fechaNacimiento(fechaNacimiento)
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(numeroDocumento)
                .eliminado(false)
                .direccionEstadia(direccionEstadia)
                .nacionalidad(nacionalidad)
                .build();

        return clienteRepository.save(cliente);
    }

    @Override
    public void validar(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                        String numeroDocumento, String direccionEstadia, String idNacionalidad) {
        personaService.validar(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento);

        if (direccionEstadia == null || direccionEstadia.isBlank()) {
            throw new IllegalArgumentException("La dirección de estadía es obligatoria");
        }
        if (idNacionalidad == null || idNacionalidad.isBlank()) {
            throw new IllegalArgumentException("La nacionalidad es obligatoria");
        }
    }

    @Override
    public Cliente modificarCliente(String id, String nombre, String apellido, Date fechaNacimiento,
                                    TipoDocumento tipoDocumento, String numeroDocumento,
                                    String direccionEstadia, String idNacionalidad) {
        Cliente cliente = buscarCliente(id);

        if (cliente.isEliminado()) {
            throw new IllegalArgumentException("No se puede modificar un cliente eliminado");
        }

        personaService.validarParaModificar(id, nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento);

        if (direccionEstadia == null || direccionEstadia.isBlank()) {
            throw new IllegalArgumentException("La dirección de estadía es obligatoria");
        }
        if (idNacionalidad == null || idNacionalidad.isBlank()) {
            throw new IllegalArgumentException("La nacionalidad es obligatoria");
        }

        Nacionalidad nacionalidad = nacionalidadService.buscarNacionalidad(idNacionalidad);

        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setFechaNacimiento(fechaNacimiento);
        cliente.setTipoDocumento(tipoDocumento);
        cliente.setNumeroDocumento(numeroDocumento);
        cliente.setDireccionEstadia(direccionEstadia);
        cliente.setNacionalidad(nacionalidad);

        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente buscarCliente(String id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
    }

    @Override
    public void eliminarCliente(String id) {
        Cliente cliente = buscarCliente(id);
        cliente.setEliminado(true);
        clienteRepository.save(cliente);
    }

    @Override
    public List<Cliente> listarCliente() {
        return clienteRepository.findAll();
    }

    @Override
    public List<Cliente> listarClienteActivo() {
        return clienteRepository.findByEliminadoFalse();
    }
}