package com.dulcemayor.controller;

import com.dulcemayor.model.Imagen;
import com.dulcemayor.service.ImagenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImagenController extends ApiControllerTemplate {
    private final ImagenService imagenService;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> get(@PathVariable String id) {
        Imagen image = ejecutarEndpoint(() -> imagenService.buscarImagen(id));
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .contentType(MediaType.parseMediaType(image.getMime() == null ? "image/jpeg" : image.getMime()))
                .body(image.getContenido());
    }
}
