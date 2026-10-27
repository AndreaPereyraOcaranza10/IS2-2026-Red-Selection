package com.tienda.zero.service.impl;

import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Localidad;
import com.tienda.zero.repository.DireccionRepository;
import com.tienda.zero.service.DireccionService;
import com.tienda.zero.service.LocalidadService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DireccionServiceImpl implements DireccionService {

    private final DireccionRepository direccionRepository;
    private final LocalidadService localidadService;

    public DireccionServiceImpl(DireccionRepository direccionRepository, LocalidadService localidadService) {
        this.direccionRepository = direccionRepository;
        this.localidadService = localidadService;
    }

    @Override
    @Transactional
    public Direccion crearDireccion(String calle, String numeracion, String barrio, String manzanaPiso,
                                    String casaDepartamento, String referencia, String idLocalidad) {
        validar(calle, numeracion, barrio, manzanaPiso, casaDepartamento, referencia, idLocalidad);

        Localidad localidad = localidadService.buscarLocalidad(idLocalidad);

        Direccion direccion = Direccion.builder()
                .calle(calle)
                .numeracion(numeracion)
                .barrio(barrio)
                .manzanaPiso(manzanaPiso)
                .casaDepartamento(casaDepartamento)
                .referencia(referencia)
                .localidad(localidad)
                .eliminado(false)
                .build();

        return direccionRepository.save(direccion);
    }

    @Override
    public void validar(String calle, String numeracion, String barrio, String manzanaPiso,
                        String casaDepartamento, String referencia, String idLocalidad) {
        if (calle == null || calle.isBlank()) {
            throw new IllegalArgumentException("La calle es obligatoria");
        }
        if (numeracion == null || numeracion.isBlank()) {
            throw new IllegalArgumentException("La numeración es obligatoria");
        }
        if (idLocalidad == null || idLocalidad.isBlank()) {
            throw new IllegalArgumentException("La localidad es obligatoria");
        }
    }

    @Override
    public Direccion buscarDireccion(String id) {
        return direccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la dirección con id: " + id));
    }

    @Override
    public Direccion buscarDireccionPorCalleNumeracion(String calle, String numeracion) {
        return direccionRepository.findByCalleAndNumeracion(calle, numeracion)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe la dirección con calle " + calle + " y numeración " + numeracion));
    }

    @Override
    public void modificarDireccion(String id, String calle, String numeracion, String barrio, String manzanaPiso,
                                   String casaDepartamento, String referencia, String idLocalidad) {
        validar(calle, numeracion, barrio, manzanaPiso, casaDepartamento, referencia, idLocalidad);

        Direccion direccion = buscarDireccion(id);
        Localidad localidad = localidadService.buscarLocalidad(idLocalidad);

        direccion.setCalle(calle);
        direccion.setNumeracion(numeracion);
        direccion.setBarrio(barrio);
        direccion.setManzanaPiso(manzanaPiso);
        direccion.setCasaDepartamento(casaDepartamento);
        direccion.setReferencia(referencia);
        direccion.setLocalidad(localidad);

        direccionRepository.save(direccion);
    }

    @Override
    public void eliminarDireccion(String id) {
        Direccion direccion = buscarDireccion(id);
        direccion.setEliminado(true);
        direccionRepository.save(direccion);
    }
}