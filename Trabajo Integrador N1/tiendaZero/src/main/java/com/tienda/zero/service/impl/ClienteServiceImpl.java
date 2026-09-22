package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.model.Cliente;
import com.tienda.zero.model.Nacionalidad;
import com.tienda.zero.repository.ClienteRepository;
import com.tienda.zero.service.ClienteService;
import com.tienda.zero.service.NacionalidadService;
import com.tienda.zero.service.PersonaService;
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
                                String numeroDocumento, String telefono, String correoElectronico,
                                String direccionEstadia, String idNacionalidad) {
        validar(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento, telefono, correoElectronico,
                direccionEstadia, idNacionalidad);

        Nacionalidad nacionalidad = nacionalidadService.buscarNacionalidad(idNacionalidad);

        Cliente cliente = Cliente.builder()
                .nombre(nombre)
                .apellido(apellido)
                .fechaNacimiento(fechaNacimiento)
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(numeroDocumento)
                .eliminado(false)
                .direccionEstadia(direccionEstadia)
                .telefono(telefono)
                .correoElectronico(correoElectronico)
                .nacionalidad(nacionalidad)
                .build();

        return clienteRepository.save(cliente);
    }

    @Override
    public void validar(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                        String numeroDocumento, String telefono, String correoElectronico,
                        String direccionEstadia, String idNacionalidad) {
        personaService.validar(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento);

        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio");
        }
        if (correoElectronico == null || correoElectronico.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        if (direccionEstadia == null || direccionEstadia.isBlank()) {
            throw new IllegalArgumentException("La dirección de estadía es obligatoria");
        }
        if (idNacionalidad == null || idNacionalidad.isBlank()) {
            throw new IllegalArgumentException("La nacionalidad es obligatoria");
        }
    }

    @Override
    public Cliente buscarCliente(String id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
    }

    @Override
    public Cliente modificarCliente(String id, String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                                    String numeroDocumento, String telefono, String correoElectronico,
                                    String direccionEstadia, String idNacionalidad) {
        Cliente cliente = buscarCliente(id);
        Nacionalidad nacionalidad = nacionalidadService.buscarNacionalidad(idNacionalidad);

        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setFechaNacimiento(fechaNacimiento);
        cliente.setTipoDocumento(tipoDocumento);
        cliente.setNumeroDocumento(numeroDocumento);
        cliente.setTelefono(telefono);
        cliente.setCorreoElectronico(correoElectronico);
        cliente.setDireccionEstadia(direccionEstadia);
        cliente.setNacionalidad(nacionalidad);

        return clienteRepository.save(cliente);
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