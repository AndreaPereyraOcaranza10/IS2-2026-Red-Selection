package com.dulcemayor.controller;

import com.dulcemayor.dto.ProductResponseDTO;
import com.dulcemayor.dto.ProductWriteDTO;
import com.dulcemayor.model.Imagen;
import com.dulcemayor.enums.TipoImagen;
import com.dulcemayor.service.CatalogoMayoristaService;
import com.dulcemayor.service.ImagenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController extends ApiControllerTemplate {
    private final CatalogoMayoristaService catalogo;
    private final ImagenService imagenes;

    @PostMapping("/images")
    public ImageResponse uploadImage(@RequestParam("image") MultipartFile file) throws java.io.IOException {
        Imagen image = imagenes.crearImagen(file.getOriginalFilename(), file.getContentType(), file.getBytes(), TipoImagen.PRODUCTO);
        return ejecutarEndpoint(() -> new ImageResponse(image.getId(), "/api/images/" + image.getId()));
    }

    @GetMapping("/products")
    public List<ProductResponseDTO> inventory(@RequestParam(required = false) String search) {
        return ejecutarEndpoint(() -> catalogo.listar(search, null, true));
    }

    @PostMapping("/products")
    public ProductResponseDTO create(@Valid @RequestBody ProductWriteDTO request) { return ejecutarEndpoint(() -> catalogo.crear(request)); }

    @PutMapping("/products/{id}")
    public ProductResponseDTO update(@PathVariable String id, @Valid @RequestBody ProductWriteDTO request) {
        return ejecutarEndpoint(() -> catalogo.actualizar(id, request));
    }

    @PatchMapping("/products/{id}/stock")
    public ProductResponseDTO updateStock(@PathVariable String id, @RequestBody StockUpdate request) {
        return ejecutarEndpoint(() -> catalogo.actualizarStock(id, request.stock()));
    }

    @DeleteMapping("/products/{id}")
    public void delete(@PathVariable String id) { ejecutarEndpoint(() -> { catalogo.eliminar(id); return null; }); }

    public record StockUpdate(int stock) {}
    public record ImageResponse(String id, String url) {}
}
