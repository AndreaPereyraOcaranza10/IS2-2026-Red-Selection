package redSelection.dulceMayor.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.PedidosPreventistaDiaDTO;
import redSelection.dulceMayor.dto.PedidosPreventistaMesDTO;
import redSelection.dulceMayor.dto.ProductoMasVendidoDTO;
import redSelection.dulceMayor.dto.RendimientoPreventistaDTO;
import redSelection.dulceMayor.dto.StockProductoEstadisticaDTO;
import redSelection.dulceMayor.service.EstadisticasService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

    private final EstadisticasService estadisticas;

    public EstadisticasController(EstadisticasService estadisticas) {
        this.estadisticas = estadisticas;
    }

    @GetMapping("/pedidos/preventistas/dia")
    public List<PedidosPreventistaDiaDTO> pedidosPorPreventistaDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return estadisticas.pedidosPorPreventistaDia(desde, hasta);
    }

    @GetMapping("/pedidos/preventistas/mes")
    public List<PedidosPreventistaMesDTO> pedidosPorPreventistaMes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return estadisticas.pedidosPorPreventistaMes(desde, hasta);
    }

    @GetMapping("/stock/productos")
    public List<StockProductoEstadisticaDTO> stockPorProducto() {
        return estadisticas.stockPorProducto();
    }

    @GetMapping("/rendimiento/preventistas")
    public List<RendimientoPreventistaDTO> rendimientoPreventistas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return estadisticas.rendimientoPreventistas(desde, hasta);
    }

    @GetMapping("/productos-mas-vendidos")
    public List<ProductoMasVendidoDTO> productosMasVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limite) {
        return estadisticas.productosMasVendidos(desde, hasta, limite);
    }
}
