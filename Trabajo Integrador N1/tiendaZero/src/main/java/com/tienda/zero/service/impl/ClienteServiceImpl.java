package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.model.Cliente;
import com.tienda.zero.model.Contacto;
import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Nacionalidad;
import com.tienda.zero.repository.ClienteRepository;
import com.tienda.zero.service.ClienteService;
import com.tienda.zero.service.NacionalidadService;
import com.tienda.zero.service.PersonaService;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

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
        //ahora validamos acá si ya existe cliente con ese doc
        if (clienteRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento).isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente con ese documento");
        }

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
        //ahora validamos acá si ya existe cliente con ese doc
        Optional<Cliente> existente = clienteRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento);
        if (existente.isPresent() && !existente.get().getId().equals(id)) {
            throw new IllegalArgumentException("Ya existe otro cliente con ese documento");
        }

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

    //modifico el eliminarCliente para poder borrar usuario al eliminarlo
    @Override
    public void eliminarCliente(String id) {
        buscarCliente(id);
        personaService.eliminarPersona(id);
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