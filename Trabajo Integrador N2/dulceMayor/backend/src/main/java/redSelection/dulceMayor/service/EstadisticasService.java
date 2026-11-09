package redSelection.dulceMayor.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.PedidosPreventistaDiaDTO;
import redSelection.dulceMayor.dto.PedidosPreventistaMesDTO;
import redSelection.dulceMayor.dto.ProductoMasVendidoDTO;
import redSelection.dulceMayor.dto.RendimientoPreventistaDTO;
import redSelection.dulceMayor.dto.StockProductoEstadisticaDTO;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Preventista;
import redSelection.dulceMayor.repository.FacturaClienteRepository;
import redSelection.dulceMayor.repository.PedidoRepository;
import redSelection.dulceMayor.repository.PreventistaRepository;
import redSelection.dulceMayor.repository.StockRepository;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consultas agregadas para los reportes y gráficos del panel del Jefe Administrativo (BK-11). */
@Service
@Transactional(readOnly = true)
public class EstadisticasService {

    private static final List<EstadoPedido> ESTADOS_FACTURADOS = List.of(
            EstadoPedido.FACTURADO, EstadoPedido.EN_REPARTO, EstadoPedido.ENTREGADO);

    private final PedidoRepository pedidoRepository;
    private final StockRepository stockRepository;
    private final FacturaClienteRepository facturaRepository;
    private final PreventistaRepository preventistaRepository;

    public EstadisticasService(PedidoRepository pedidoRepository, StockRepository stockRepository,
                               FacturaClienteRepository facturaRepository,
                               PreventistaRepository preventistaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.stockRepository = stockRepository;
        this.facturaRepository = facturaRepository;
        this.preventistaRepository = preventistaRepository;
    }

    public List<PedidosPreventistaDiaDTO> pedidosPorPreventistaDia(LocalDate desde, LocalDate hasta) {
        RangoFechas rango = rango(desde, hasta, Ventana.ULTIMOS_30_DIAS);
        return pedidoRepository.pedidosPorPreventistaDia(rango.inicio(), rango.fin(), EstadoPedido.ANULADO)
                .stream()
                .map(fila -> new PedidosPreventistaDiaDTO(fila.getPreventistaId(), fila.getNombrePreventista(),
                        "%04d-%02d-%02d".formatted(fila.getAnio(), fila.getMes(), fila.getDia()),
                        valor(fila.getCantidadPedidos()), dinero(fila.getMontoPedidos())))
                .toList();
    }

    public List<PedidosPreventistaMesDTO> pedidosPorPreventistaMes(LocalDate desde, LocalDate hasta) {
        RangoFechas rango = rango(desde, hasta, Ventana.ULTIMOS_12_MESES);
        return pedidoRepository.pedidosPorPreventistaMes(rango.inicio(), rango.fin(), EstadoPedido.ANULADO)
                .stream()
                .map(fila -> new PedidosPreventistaMesDTO(fila.getPreventistaId(), fila.getNombrePreventista(),
                        "%04d-%02d".formatted(fila.getAnio(), fila.getMes()),
                        valor(fila.getCantidadPedidos()), dinero(fila.getMontoPedidos())))
                .toList();
    }

    public List<StockProductoEstadisticaDTO> stockPorProducto() {
        return stockRepository.estadisticasStockPorProducto().stream()
                .map(fila -> {
                    long stock = valor(fila.getStockActual());
                    int minimo = fila.getStockMinimo() == null ? 0 : fila.getStockMinimo();
                    return new StockProductoEstadisticaDTO(fila.getProductoId(), fila.getCodigo(), fila.getNombre(),
                            fila.getMarca(), stock, minimo, stock <= minimo);
                })
                .toList();
    }

    public List<RendimientoPreventistaDTO> rendimientoPreventistas(LocalDate desde, LocalDate hasta) {
        RangoFechas rango = rango(desde, hasta, Ventana.MES_ACTUAL);
        Map<String, RendimientoMutable> acumulados = new LinkedHashMap<>();

        for (Preventista preventista : preventistaRepository.findByEliminadoFalse()) {
            acumulados.put(preventista.getId(), new RendimientoMutable(preventista.getId(),
                    preventista.getNombre() + " " + preventista.getApellido()));
        }

        for (PedidoRepository.PedidosPorEstadoPreventista fila :
                pedidoRepository.pedidosPorEstadoPreventista(rango.inicio(), rango.fin())) {
            RendimientoMutable stats = acumulados.computeIfAbsent(fila.getPreventistaId(),
                    id -> new RendimientoMutable(id, fila.getNombrePreventista()));
            long cantidad = valor(fila.getCantidad());
            stats.cantidadPedidos += cantidad;
            if (ESTADOS_FACTURADOS.contains(fila.getEstado())) stats.pedidosFacturados += cantidad;
            if (fila.getEstado() == EstadoPedido.ANULADO) stats.pedidosAnulados += cantidad;
        }

        for (PedidoRepository.VentasPreventista fila : pedidoRepository.ventasPorPreventista(
                rango.inicio(), rango.fin(), ESTADOS_FACTURADOS)) {
            RendimientoMutable stats = acumulados.get(fila.getPreventistaId());
            if (stats != null) stats.montoFacturado = dinero(fila.getMontoFacturado());
        }

        return acumulados.values().stream()
                .map(RendimientoMutable::aDto)
                .sorted(Comparator.comparing(RendimientoPreventistaDTO::montoFacturado).reversed()
                        .thenComparing(RendimientoPreventistaDTO::nombrePreventista, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<ProductoMasVendidoDTO> productosMasVendidos(LocalDate desde, LocalDate hasta, int limite) {
        if (limite < 1 || limite > 50) {
            throw new ReglaNegocioException("El límite debe estar entre 1 y 50 productos.");
        }
        RangoFechas rango = rango(desde, hasta, Ventana.MES_ACTUAL);
        Pageable paginacion = PageRequest.of(0, limite);
        return facturaRepository.productosMasVendidos(rango.inicio(), rango.fin(), EstadoFactura.ANULADA, paginacion)
                .stream()
                .map(fila -> new ProductoMasVendidoDTO(fila.getProductoId(), fila.getCodigo(), fila.getNombre(),
                        valor(fila.getUnidadesVendidas())))
                .toList();
    }

    private RangoFechas rango(LocalDate desde, LocalDate hasta, Ventana ventana) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioPorDefecto = switch (ventana) {
            case ULTIMOS_30_DIAS -> hoy.minusDays(29);
            case ULTIMOS_12_MESES -> hoy.withDayOfMonth(1).minusMonths(11);
            case MES_ACTUAL -> hoy.withDayOfMonth(1);
        };
        LocalDate inicio = desde == null ? inicioPorDefecto : desde;
        LocalDate fin = hasta == null ? hoy : hasta;
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        return new RangoFechas(inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));
    }

    private long valor(Long valor) {
        return valor == null ? 0 : valor;
    }

    private BigDecimal dinero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private enum Ventana { ULTIMOS_30_DIAS, ULTIMOS_12_MESES, MES_ACTUAL }

    private record RangoFechas(LocalDateTime inicio, LocalDateTime fin) { }

    private static final class RendimientoMutable {
        private final String preventistaId;
        private final String nombrePreventista;
        private long cantidadPedidos;
        private long pedidosFacturados;
        private long pedidosAnulados;
        private BigDecimal montoFacturado = BigDecimal.ZERO;

        private RendimientoMutable(String preventistaId, String nombrePreventista) {
            this.preventistaId = preventistaId;
            this.nombrePreventista = nombrePreventista;
        }

        private RendimientoPreventistaDTO aDto() {
            long pendientes = Math.max(0, cantidadPedidos - pedidosFacturados - pedidosAnulados);
            return new RendimientoPreventistaDTO(preventistaId, nombrePreventista, cantidadPedidos,
                    pedidosFacturados, pendientes, pedidosAnulados, montoFacturado);
        }
    }
}
