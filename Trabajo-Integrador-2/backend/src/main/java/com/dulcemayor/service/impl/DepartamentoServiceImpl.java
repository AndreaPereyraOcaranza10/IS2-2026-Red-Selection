package com.dulcemayor.service.impl;

import com.dulcemayor.model.Departamento;
import com.dulcemayor.model.Provincia;
import com.dulcemayor.repository.DepartamentoRepository;
import com.dulcemayor.service.DepartamentoService;
import com.dulcemayor.service.ProvinciaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final ProvinciaService provinciaService;

    public DepartamentoServiceImpl(DepartamentoRepository departamentoRepository, ProvinciaService provinciaService) {
        this.departamentoRepository = departamentoRepository;
        this.provinciaService = provinciaService;
    }

    @Override
    public void crearDepartamento(String nombre, String idProvincia) {
        validar(nombre, idProvincia);

        Provincia provincia = provinciaService.buscarProvincia(idProvincia);

        Departamento departamento = Departamento.builder()
                .nombre(nombre)
                .provincia(provincia)
                .eliminado(false)
                .build();

        departamentoRepository.save(departamento);
    }

    @Override
    public void validar(String nombre, String idProvincia) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del departamento es obligatorio");
        }
        if (idProvincia == null || idProvincia.isBlank()) {
            throw new IllegalArgumentException("La provincia es obligatoria");
        }
    }

    @Override
    public Departamento buscarDepartamento(String id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el departamento con id: " + id));
    }

    @Override
    public Departamento buscarDepartamentoPorNombre(String nombre) {
        return departamentoRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalArgumentException("No existe el departamento con nombre: " + nombre));
    }

    @Override
    public void modificarDepartamento(String id, String nombre, String idProvincia) {
        validar(nombre, idProvincia);

        Departamento departamento = buscarDepartamento(id);
        Provincia provincia = provinciaService.buscarProvincia(idProvincia);

        departamento.setNombre(nombre);
        departamento.setProvincia(provincia);

        departamentoRepository.save(departamento);
    }

    @Override
    public void eliminarDepartamento(String id) {
        Departamento departamento = buscarDepartamento(id);
        departamento.setEliminado(true);
        departamentoRepository.save(departamento);
    }

    @Override
    public List<Departamento> listarDepartamento(String idProvincia) {
        return departamentoRepository.findByProvinciaId(idProvincia);
    }

    @Override
    public List<Departamento> listarDepartamentoActivo(String idProvincia) {
        return departamentoRepository.findByProvinciaIdAndEliminadoFalse(idProvincia);
    }
}