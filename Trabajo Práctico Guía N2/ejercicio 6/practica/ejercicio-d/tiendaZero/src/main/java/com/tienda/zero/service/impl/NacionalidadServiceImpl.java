package com.tienda.zero.service.impl;

import com.tienda.zero.model.Nacionalidad;
import com.tienda.zero.repository.NacionalidadRepository;
import com.tienda.zero.service.NacionalidadService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NacionalidadServiceImpl implements NacionalidadService {

    private final NacionalidadRepository nacionalidadRepository;

    public NacionalidadServiceImpl(NacionalidadRepository nacionalidadRepository) {
        this.nacionalidadRepository = nacionalidadRepository;
    }

    @Override
    public Nacionalidad crearNacionalidad(String nombre) {
        validar(nombre);
        Nacionalidad nacionalidad = Nacionalidad.builder()
                .nombre(nombre)
                .eliminado(false)
                .build();
        return nacionalidadRepository.save(nacionalidad);
    }

    @Override
    public void validar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la nacionalidad es obligatorio");
        }
        nacionalidadRepository.findByNombreIgnoreCase(nombre).ifPresent(n -> {
            throw new IllegalArgumentException("Ya existe una nacionalidad con ese nombre");
        });
    }

    @Override
    public Nacionalidad buscarNacionalidad(String id) {
        return nacionalidadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nacionalidad no encontrada: " + id));
    }

    @Override
    public Nacionalidad buscarNacionalidadPorNombre(String nombre) {
        return nacionalidadRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Nacionalidad no encontrada: " + nombre));
    }

    @Override
    public Nacionalidad modificarNacionalidad(String id, String nombre) {
        Nacionalidad nacionalidad = buscarNacionalidad(id);
        nacionalidad.setNombre(nombre);
        return nacionalidadRepository.save(nacionalidad);
    }

    @Override
    public void eliminarNacionalidad(String id) {
        Nacionalidad nacionalidad = buscarNacionalidad(id);
        nacionalidad.setEliminado(true);
        nacionalidadRepository.save(nacionalidad);
    }

    @Override
    public List<Nacionalidad> listarNacionalidad() {
        return nacionalidadRepository.findAll();
    }

    @Override
    public List<Nacionalidad> listarNacionalidadActiva() {
        return nacionalidadRepository.findByEliminadoFalse();
    }
}
