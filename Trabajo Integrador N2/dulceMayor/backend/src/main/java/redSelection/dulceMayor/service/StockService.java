package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.MovimientoStockRespuestaDTO;
import redSelection.dulceMayor.enums.TipoMovimientoStock;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.DetalleFactura;
import redSelection.dulceMayor.model.Producto;
import redSelection.dulceMayor.model.Stock;
import redSelection.dulceMayor.repository.StockPorProducto;
import redSelection.dulceMayor.repository.StockRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) { this.stockRepository = stockRepository; }

    @Transactional(readOnly = true)
    public long stockActual(String productoId) { return stockRepository.stockActual(productoId); }

    /** Una sola consulta para todos los productos: evita N+1. */
    @Transactional(readOnly = true)
    public Map<String, Long> stockPorProducto() {
        Map<String, Long> mapa = new HashMap<>();
        for (StockPorProducto s : stockRepository.stockPorProducto()) mapa.put(s.getProductoId(), s.getCantidad());
        return mapa;
    }

    /** MANDATORY: exige transacción abierta; quien llama debe haber tomado antes el lock del producto. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Stock registrar(Producto producto, TipoMovimientoStock tipo, int movimiento, DetalleFactura detalle) {
        if (movimiento == 0) throw new ReglaNegocioException("El movimiento de stock no puede ser cero.");
        long disponible = stockRepository.stockActual(producto.getId());
        long nuevo = disponible + movimiento;
        if (nuevo < 0) {
            throw new ReglaNegocioException("Stock insuficiente para '" + producto.getNombre()
                    + "' (disponible: " + disponible + ", requerido: " + (-movimiento) + ").");
        }
        return stockRepository.save(Stock.builder()
                .producto(producto).tipoMovimiento(tipo).movimiento(movimiento)
                .cantidadActual(Math.toIntExact(nuevo)).fecha(LocalDateTime.now())
                .detalleFactura(detalle).build());
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockRespuestaDTO> historial(String productoId) {
        return stockRepository.findByProductoIdOrderByFechaDesc(productoId).stream()
                .map(s -> new MovimientoStockRespuestaDTO(s.getId(), s.getFecha(),
                        s.getTipoMovimiento(), s.getMovimiento(), s.getCantidadActual()))
                .toList();
    }

    /** Para EstadisticasService: stock actual y mínimo de cada producto. */
    @Transactional(readOnly = true)
    public List<StockRepository.StockProductoEstadistica> estadisticasPorProducto() {
        return stockRepository.estadisticasStockPorProducto();
    }

}