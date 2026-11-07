package redSelection.dulceMayor.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import redSelection.dulceMayor.dto.ImagenContenidoDTO;
import redSelection.dulceMayor.dto.ImagenRespuestaDTO;
import redSelection.dulceMayor.enums.TipoImagen;
import redSelection.dulceMayor.service.ImagenService;

import java.net.URI;
import java.time.Duration;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    private final ImagenService imagenService;

    public ImagenController(ImagenService imagenService) { this.imagenService = imagenService; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenRespuestaDTO> subir(@RequestParam("archivo") MultipartFile archivo,
                                                    @RequestParam(defaultValue = "PRODUCTO") TipoImagen tipo) {
        ImagenRespuestaDTO creada = imagenService.subir(archivo, tipo);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> descargar(@PathVariable String id) {
        ImagenContenidoDTO img = imagenService.contenido(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(img.mime()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(img.contenido());
    }
}
