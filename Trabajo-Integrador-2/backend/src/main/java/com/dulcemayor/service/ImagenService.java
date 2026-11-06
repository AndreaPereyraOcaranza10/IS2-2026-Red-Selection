package com.dulcemayor.service;

import com.dulcemayor.enums.TipoImagen;
import com.dulcemayor.model.Imagen;

public interface ImagenService {

    Imagen crearImagen(String nombre, String mime, byte[] contenido, TipoImagen tipoImagen);

    void validar(String nombre, String mime, byte[] contenido, TipoImagen tipoImagen);

    Imagen modificarImagen(String id, String nombre, String mime, byte[] contenido, TipoImagen tipoImagen);

    Imagen buscarImagen(String id);
}