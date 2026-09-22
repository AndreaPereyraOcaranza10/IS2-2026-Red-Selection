package com.tienda.zero.service.impl;

import com.tienda.zero.model.Pais;
import com.tienda.zero.model.Provincia;
import com.tienda.zero.repository.ProvinciaRepository;
import com.tienda.zero.service.PaisService;
import com.tienda.zero.service.ProvinciaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProvinciaServiceImpl implements ProvinciaService {

    private final ProvinciaRepository provinciaRepository;
    private final PaisService paisService;

    public ProvinciaServiceImpl(ProvinciaRepository provinciaRepository, PaisService paisService) {
        this.provinciaRepository = provinciaRepository;
        this.paisService = paisService;
    }

    @Override
    public void crearProvincia(String nombre, String idPais) {
        validar(nombre, idPais);

        Pais pais = paisService.buscarPais(idPais);

        Provincia provincia = Provincia.builder()
                .nombre(nombre)
                .pais(pais)
                .eliminado(false)
                .build();

        provinciaRepository.save(provincia);
    }

    @Override
    public void validar(String nombre, String idPais) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la provincia es obligatorio");
        }
        if (idPais == null || idPais.isBlank()) {
            throw new IllegalArgumentException("El país es obligatorio");
        }
    }

    @Override
    public Provincia buscarProvincia(String id) {
        return provinciaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la provincia con id: " + id));
    }

    @Override
    public Provincia buscarProvinciaPorNombre(String nombre) {
        return provinciaRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalArgumentException("No existe la provincia con nombre: " + nombre));
    }

    @Override
    public void modificarProvincia(String id, String nombre, String idPais) {
        validar(nombre, idPais);

        Provincia provincia = buscarProvincia(id);
        Pais pais = paisService.buscarPais(idPais);

        provincia.setNombre(nombre);
        provincia.setPais(pais);

        provinciaRepository.save(provincia);
    }

    @Override
    public void eliminarProvincia(String id) {
        Provincia provincia = buscarProvincia(id);
        provincia.setEliminado(true);
        provinciaRepository.save(provincia);
    }

    @Override
    public List<Provincia> listarProvincia(String idPais) {
        return provinciaRepository.findByPaisId(idPais);
    }

    @Override
    public List<Provincia> listarProvinciaActivo(String idPais) {
        return provinciaRepository.findByPaisIdAndEliminadoFalse(idPais);
    }
}