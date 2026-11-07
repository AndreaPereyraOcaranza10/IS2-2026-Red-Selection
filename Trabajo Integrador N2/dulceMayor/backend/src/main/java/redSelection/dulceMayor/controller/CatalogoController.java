package redSelection.dulceMayor.controller;

import org.springframework.web.bind.annotation.*;

import redSelection.dulceMayor.dto.CatalogoProductoDTO;
import redSelection.dulceMayor.service.CatalogoService;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    // =====================================================
    // LISTAR PRODUCTOS
    // =====================================================

    @GetMapping("/productos")
    public List<CatalogoProductoDTO> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "false") boolean incluirSinStock) {

        return catalogoService.listar(
                busqueda,
                categoria,
                incluirSinStock
        );
    }

    // =====================================================
    // BUSCAR PRODUCTO POR ID
    // =====================================================

    @GetMapping("/productos/{id}")
    public CatalogoProductoDTO buscar(@PathVariable String id) {

        return catalogoService.buscar(id);
    }

    // =====================================================
    // LISTAR CATEGORIAS
    // =====================================================

    @GetMapping("/categorias")
    public List<String> categorias() {

        return catalogoService.categorias();
    }
}
