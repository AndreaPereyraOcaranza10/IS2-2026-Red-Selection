package com.tienda.zero.service.impl;

import com.tienda.zero.model.Departamento;
import com.tienda.zero.model.Localidad;
import com.tienda.zero.repository.LocalidadRepository;
import com.tienda.zero.service.DepartamentoService;
import com.tienda.zero.service.LocalidadService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalidadServiceImpl implements LocalidadService {

    private final LocalidadRepository localidadRepository;
    private final DepartamentoService departamentoService;

    public LocalidadServiceImpl(LocalidadRepository localidadRepository, DepartamentoService departamentoService) {
        this.localidadRepository = localidadRepository;
        this.departamentoService = departamentoService;
    }

    @Override
    public void crearLocalidad(String nombre, String codigoPostal, String idDepartamento) {
        validar(nombre, codigoPostal, idDepartamento);

        Departamento departamento = departamentoService.buscarDepartamento(idDepartamento);

        Localidad localidad = Localidad.builder()
                .nombre(nombre)
                .codigoPostal(codigoPostal)
                .departamento(departamento)
                .eliminado(false)
                .build();

        localidadRepository.save(localidad);
    }

    @Override
    public void validar(String nombre, String codigoPostal, String idDepartamento) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la localidad es obligatorio");
        }
        if (codigoPostal == null || codigoPostal.isBlank()) {
            throw new IllegalArgumentException("El código postal es obligatorio");
        }
        if (idDepartamento == null || idDepartamento.isBlank()) {
            throw new IllegalArgumentException("El departamento es obligatorio");
        }
    }

    @Override
    public Localidad buscarLocalidad(String id) {
        return localidadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la localidad con id: " + id));
    }

    @Override
    public Localidad buscarLocalidadPorNombre(String nombre) {
        return localidadRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalArgumentException("No existe la localidad con nombre: " + nombre));
    }

    @Override
    public Localidad buscarLocalidadPorCodigoPostal(String codigoPostal) {
        return localidadRepository.findByCodigoPostal(codigoPostal)
                .orElseThrow(() -> new IllegalArgumentException("No existe la localidad con código postal: " + codigoPostal));
    }

    @Override
    public void modificarLocalidad(String id, String nombre, String codigoPostal, String idDepartamento) {
        validar(nombre, codigoPostal, idDepartamento);

        Localidad localidad = buscarLocalidad(id);
        Departamento departamento = departamentoService.buscarDepartamento(idDepartamento);

        localidad.setNombre(nombre);
        localidad.setCodigoPostal(codigoPostal);
        localidad.setDepartamento(departamento);

        localidadRepository.save(localidad);
    }

    @Override
    public void eliminarLocalidad(String id) {
        Localidad localidad = buscarLocalidad(id);
        localidad.setEliminado(true);
        localidadRepository.save(localidad);
    }

    @Override
    public List<Localidad> listarLocalidad(String idDepartamento) {
        return localidadRepository.findByDepartamentoId(idDepartamento);
    }

    @Override
    public List<Localidad> listarLocalidadActivo(String idDepartamento) {
        return localidadRepository.findByDepartamentoIdAndEliminadoFalse(idDepartamento);
    }
}