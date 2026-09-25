package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoEmpleado;
import com.tienda.zero.model.Empleado;
import com.tienda.zero.model.Empresa;
import com.tienda.zero.repository.EmpleadoRepository;
import com.tienda.zero.service.EmpleadoService;
import com.tienda.zero.service.EmpresaService;
import com.tienda.zero.service.PersonaService;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PersonaService personaService;
    private final EmpresaService empresaService;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository, PersonaService personaService,
                               EmpresaService empresaService) {
        this.empleadoRepository = empleadoRepository;
        this.personaService = personaService;
        this.empresaService = empresaService;
    }

    @Override
    public Empleado crearEmpleado(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                                  String numeroDocumento, TipoEmpleado tipoEmpleado, String idEmpresa) {
        validar(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento, tipoEmpleado, idEmpresa);

        Empresa empresa = empresaService.buscarEmpresa(idEmpresa);

        Empleado empleado = Empleado.builder()
                .nombre(nombre)
                .apellido(apellido)
                .fechaNacimiento(fechaNacimiento)
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(numeroDocumento)
                .eliminado(false)
                .tipoEmpleado(tipoEmpleado)
                .empresa(empresa)
                .build();

        return empleadoRepository.save(empleado);
    }

    @Override
    public void validar(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                        String numeroDocumento, TipoEmpleado tipoEmpleado, String idEmpresa) {
        personaService.validar(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento);

        if (tipoEmpleado == null) {
            throw new IllegalArgumentException("El tipo de empleado es obligatorio");
        }
        if (idEmpresa == null || idEmpresa.isBlank()) {
            throw new IllegalArgumentException("La empresa es obligatoria");
        }
    }

    @Override
    public Empleado buscarEmpleado(String id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + id));
    }

    @Override
    public Empleado modificarEmpleado(String id, String nombre, String apellido, Date fechaNacimiento,
                                      TipoDocumento tipoDocumento, String numeroDocumento,
                                      TipoEmpleado tipoEmpleado, String idEmpresa) {
        Empleado empleado = buscarEmpleado(id);

        if (empleado.isEliminado()) {
            throw new IllegalArgumentException("No se puede modificar un empleado eliminado");
        }

        personaService.validarParaModificar(id, nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento);

        if (tipoEmpleado == null) {
            throw new IllegalArgumentException("El tipo de empleado es obligatorio");
        }
        if (idEmpresa == null || idEmpresa.isBlank()) {
            throw new IllegalArgumentException("La empresa es obligatoria");
        }

        Empresa empresa = empresaService.buscarEmpresa(idEmpresa);

        empleado.setNombre(nombre);
        empleado.setApellido(apellido);
        empleado.setFechaNacimiento(fechaNacimiento);
        empleado.setTipoDocumento(tipoDocumento);
        empleado.setNumeroDocumento(numeroDocumento);
        empleado.setTipoEmpleado(tipoEmpleado);
        empleado.setEmpresa(empresa);

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