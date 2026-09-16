package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoImagen;
import com.tienda.zero.model.Imagen;
import com.tienda.zero.repository.ImagenRepository;
import com.tienda.zero.service.ImagenService;
import org.springframework.stereotype.Service;

@Service
public class ImagenServiceImpl implements ImagenService {

    private static final long TAMANIO_MAXIMO_BYTES = 5 * 1024 * 1024; // 5MB

    private final ImagenRepository imagenRepository;

    public ImagenServiceImpl(ImagenRepository imagenRepository) {
        this.imagenRepository = imagenRepository;
    }

    @Override
    public Imagen crearImagen(String nombre, String mime, byte[] contenido, TipoImagen tipoImagen) {
        validar(nombre, mime, contenido, tipoImagen);

        Imagen imagen = Imagen.builder()
                .nombre(nombre)
                .mime(mime)
                .contenido(contenido)
                .tipoImagen(tipoImagen)
                .build();

        return imagenRepository.save(imagen);
    }

    @Override
    public void validar(String nombre, String mime, byte[] contenido, TipoImagen tipoImagen) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la imagen es obligatorio");
        }
        if (mime == null || !mime.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen válida");
        }
        if (contenido == null || contenido.length == 0) {
            throw new IllegalArgumentException("El contenido de la imagen no puede estar vacío");
        }
        if (contenido.length > TAMANIO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("La imagen supera el tamaño máximo permitido (5MB)");
        }
        if (tipoImagen == null) {
            throw new IllegalArgumentException("Debe indicarse el tipo de imagen (PERSONA o PRODUCTO)");
        }
    }

    @Override
    public Imagen modificarImagen(String id, String nombre, String mime, byte[] contenido, TipoImagen tipoImagen) {
        validar(nombre, mime, contenido, tipoImagen);

        Imagen existente = imagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe una imagen con id: " + id));

        existente.setNombre(nombre);
        existente.setMime(mime);
        existente.setContenido(contenido);
        existente.setTipoImagen(tipoImagen);

        return imagenRepository.save(existente);
    }

    @Override
    public Imagen buscarImagen(String id) {
        return imagenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada: " + id));
    }
}