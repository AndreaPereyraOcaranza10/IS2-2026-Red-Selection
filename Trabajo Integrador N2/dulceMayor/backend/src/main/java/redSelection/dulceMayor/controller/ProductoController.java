
package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import redSelection.dulceMayor.dto.*;
import redSelection.dulceMayor.service.ProductoService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController extends BaseController<ProductoCrearDTO, ProductoRespuestaDTO> {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        super(productoService);
        this.productoService = productoService;
    }

    @Override protected String idDe(ProductoRespuestaDTO respuesta) { return respuesta.id(); }

    @PutMapping("/{id}/precio")
    public ResponseEntity<ProductoRespuestaDTO> actualizarPrecio(@PathVariable String id, @Valid @RequestBody ActualizarPrecioDTO dto) {
        return ResponseEntity.ok(productoService.actualizarPrecio(id, dto.precio()));
    }

    @PostMapping("/{id}/stock")
    public ResponseEntity<ProductoRespuestaDTO> registrarMovimientoStock(@PathVariable String id, @Valid @RequestBody MovimientoStockDTO dto) {
        return ResponseEntity.ok(productoService.registrarMovimientoStock(id, dto));
    }

    @GetMapping("/{id}/stock/historial")
    public ResponseEntity<List<MovimientoStockRespuestaDTO>> historialStock(@PathVariable String id) {
        return ResponseEntity.ok(productoService.historialStock(id));
    }

    @GetMapping("/subcategoria/{subCategoriaId}")
    public ResponseEntity<List<ProductoRespuestaDTO>> listarPorSubCategoria(@PathVariable String subCategoriaId) {
        return ResponseEntity.ok(productoService.listarPorSubCategoria(subCategoriaId));
    }
}