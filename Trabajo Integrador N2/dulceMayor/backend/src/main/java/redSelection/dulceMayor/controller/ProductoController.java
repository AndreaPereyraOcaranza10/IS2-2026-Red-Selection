
package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import redSelection.dulceMayor.dto.MovimientoStockDTO;
import redSelection.dulceMayor.dto.ProductoActualizarDTO;
import redSelection.dulceMayor.dto.ProductoCrearDTO;
import redSelection.dulceMayor.dto.ProductoRespuestaDTO;
import redSelection.dulceMayor.dto.ActualizarPrecioDTO;

import redSelection.dulceMayor.service.ProductoService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // =====================================================
    // CREAR PRODUCTO
    // =====================================================

    @PostMapping
    public ResponseEntity<ProductoRespuestaDTO> crear(
            @Valid @RequestBody ProductoCrearDTO dto) {

        ProductoRespuestaDTO creado = productoService.crear(dto);

        URI ubicacion = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();

        return ResponseEntity.created(ubicacion).body(creado);
    }

    // =====================================================
    // LISTAR PRODUCTOS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<ProductoRespuestaDTO>> listar() {

        return ResponseEntity.ok(productoService.listar());
    }

    // =====================================================
    // OBTENER PRODUCTO
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<ProductoRespuestaDTO> obtener(
            @PathVariable String id) {

        return ResponseEntity.ok(productoService.obtener(id));
    }

    // =====================================================
    // ACTUALIZAR PRODUCTO
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<ProductoRespuestaDTO> actualizar(
            @PathVariable String id,
            @Valid @RequestBody ProductoActualizarDTO dto) {

        return ResponseEntity.ok(
                productoService.actualizar(id, dto)
        );
    }

    // =====================================================
    // ELIMINAR PRODUCTO
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String id) {

        productoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // ACTUALIZAR PRECIO
    // =====================================================

    @PutMapping("/{id}/precio")
    public ResponseEntity<ProductoRespuestaDTO> actualizarPrecio(
            @PathVariable String id,
            @Valid @RequestBody ActualizarPrecioDTO dto) {

        return ResponseEntity.ok(
                productoService.actualizarPrecio(id, dto.precio())
        );
    }

    // =====================================================
    // REGISTRAR MOVIMIENTO DE STOCK
    // =====================================================

    @PostMapping("/{id}/stock")
    public ResponseEntity<ProductoRespuestaDTO> registrarMovimientoStock(
            @PathVariable String id,
            @Valid @RequestBody MovimientoStockDTO dto) {

        return ResponseEntity.ok(
                productoService.registrarMovimientoStock(id, dto)
        );
    }

    // =====================================================
    // LISTAR POR SUBCATEGORIA
    // =====================================================

    @GetMapping("/subcategoria/{subCategoriaId}")
    public ResponseEntity<List<ProductoRespuestaDTO>> listarPorSubCategoria(
            @PathVariable String subCategoriaId) {

        return ResponseEntity.ok(
                productoService.listarPorSubCategoria(subCategoriaId)
        );
    }
}
