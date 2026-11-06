package com.dulcemayor.service;

import com.dulcemayor.model.Direccion;

public interface DireccionService {

    Direccion crearDireccion(String calle, String numeracion, String barrio, String manzanaPiso,
                             String casaDepartamento, String referencia, String idLocalidad);

    void validar(String calle, String numeracion, String barrio, String manzanaPiso,
                 String casaDepartamento, String referencia, String idLocalidad);

    Direccion buscarDireccion(String id);

    Direccion buscarDireccionPorCalleNumeracion(String calle, String numeracion);

    void modificarDireccion(String id, String calle, String numeracion, String barrio, String manzanaPiso,
                            String casaDepartamento, String referencia, String idLocalidad);

    void eliminarDireccion(String id);
}