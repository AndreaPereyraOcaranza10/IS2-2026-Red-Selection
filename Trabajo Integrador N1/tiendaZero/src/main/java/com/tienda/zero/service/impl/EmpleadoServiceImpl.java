package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoEmpleado;
import com.tienda.zero.model.Empleado;
import com.tienda.zero.repository.EmpleadoRepository;
import com.tienda.zero.service.EmpleadoService;
import com.tienda.zero.service.PersonaService;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PersonaService personaService;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository, PersonaService personaService) {
        this.empleadoRepository = empleadoRepository;
        this.personaService = personaService;
    }

    @Override
    public Empleado crearEmpleado(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                                  String numeroDocumento, String telefono, String correoElectronico, TipoEmpleado tipoEmpleado) {
        validar(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento, telefono, correoElectronico, tipoEmpleado);

        Empleado empleado = Empleado.builder()
                .nombre(nombre)
                .apellido(apellido)
                .fechaNacimiento(fechaNacimiento)
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(numeroDocumento)
                .eliminado(false)
                .telefono(telefono)
                .correoElectronico(correoElectronico)
                .tipoEmpleado(tipoEmpleado)
                .build();

        return empleadoRepository.save(empleado);
    }

    @Override
    public void validar(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                        String numeroDocumento, String telefono, String correoElectronico, TipoEmpleado tipoEmpleado) {
        personaService.validar(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento);

        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio");
        }
        if (correoElectronico == null || correoElectronico.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        if (tipoEmpleado == null) {
            throw new IllegalArgumentException("El tipo de empleado es obligatorio");
        }
    }

    @Override
    public Empleado buscarEmpleado(String id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + id));
    }

    @Override
    public Empleado modificarEmpleado(String id, String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                                      String numeroDocumento, TipoEmpleado tipoEmpleado) {
        Empleado empleado = buscarEmpleado(id);

        empleado.setNombre(nombre);
        empleado.setApellido(apellido);
        empleado.setFechaNacimiento(fechaNacimiento);
        empleado.setTipoDocumento(tipoDocumento);
        empleado.setNumeroDocumento(numeroDocumento);
        empleado.setTipoEmpleado(tipoEmpleado);

        return empleadoRepository.save(empleado);
    }

    @Override
    public void eliminarEmpleado(String id) {
        Empleado empleado = buscarEmpleado(id);
        empleado.setEliminado(true);
        empleadoRepository.save(empleado);
    }

    @Override
    public List<Empleado> listarEmpleado() {
        return empleadoRepository.findAll();
    }

    @Override
    public List<Empleado> listarEmpleadoActivo() {
        return empleadoRepository.findByEliminadoFalse();
    }
}