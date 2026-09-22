package com.tienda.zero.service.impl;

import com.tienda.zero.model.Pais;
import com.tienda.zero.repository.PaisRepository;
import com.tienda.zero.service.PaisService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaisServiceImpl implements PaisService {

    private final PaisRepository paisRepository;

    public PaisServiceImpl(PaisRepository paisRepository) {
        this.paisRepository = paisRepository;
    }

    @Override
    public void crearPais(String nombre) {
        validar(nombre);

        Pais pais = Pais.builder()
                .nombre(nombre)
                .eliminado(false)
                .build();

        paisRepository.save(pais);
    }

    @Override
    public void validar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del país es obligatorio");
        }
    }

    @Override
    public Pais buscarPais(String id) {
        return paisRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el país con id: " + id));
    }

    @Override
    public Pais buscarPaisPorNombre(String nombre) {
        return paisRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalArgumentException("No existe el país con nombre: " + nombre));
    }

    @Override
    public void modificarPais(String id, String nombre) {
        validar(nombre);

        Pais pais = buscarPais(id);
        pais.setNombre(nombre);

        paisRepository.save(pais);
    }

    @Override
    public void eliminarPais(String id) {
        Pais pais = buscarPais(id);
        pais.setEliminado(true);
        paisRepository.save(pais);
    }

    @Override
    public List<Pais> listarPais() {
        return paisRepository.findAll();
    }

    @Override
    public List<Pais> listarPaisActivo() {
        return paisRepository.findByEliminadoFalse();
    }
}