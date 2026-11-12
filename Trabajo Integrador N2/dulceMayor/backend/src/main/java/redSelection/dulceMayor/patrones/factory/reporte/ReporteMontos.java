package redSelection.dulceMayor.patrones.factory.reporte;

import org.springframework.stereotype.Component;
import redSelection.dulceMayor.dto.PeriodoReporte;
import redSelection.dulceMayor.dto.ReporteDTO;
import redSelection.dulceMayor.dto.TablaReporteDTO;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.enums.TipoReporte;
import redSelection.dulceMayor.model.Pedido;
import redSelection.dulceMayor.repository.PedidoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Montos del período: por estado del pedido y por preventista. Los totales generales no cuentan los anulados. */
@Component
public class ReporteMontos implements ReporteGenerador {

    private static final List<String> COLUMNAS_ESTADO = List.of("Estado", "Pedidos", "Subtotal", "Descuento", "Total");
    private static final List<String> COLUMNAS_PREVENTISTA = List.of("Preventista", "Pedidos", "Total");

    private final PedidoRepository pedidoRepository;

    public ReporteMontos(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public TipoReporte tipo() { return TipoReporte.MONTOS; }

    @Override
    public ReporteDTO generar(PeriodoReporte periodo) {
        List<Pedido> pedidos = pedidoRepository
                .findByEliminadoFalseAndFechaHoraCargaBetweenOrderByFechaHoraCargaAsc(periodo.inicio(), periodo.fin());
        return new ReporteDTO(tipo(), "Reporte de montos", periodo.texto(), LocalDateTime.now(),
                List.of(porEstado(pedidos), porPreventista(pedidos)));
    }

    private TablaReporteDTO porEstado(List<Pedido> pedidos) {
        List<List<Object>> filas = new ArrayList<>();
        long cantidadTotal = 0;
        BigDecimal subtotalTotal = BigDecimal.ZERO;
        BigDecimal descuentoTotal = BigDecimal.ZERO;
        BigDecimal totalGeneral = BigDecimal.ZERO;
        for (EstadoPedido estado : EstadoPedido.values()) {
            List<Pedido> delEstado = pedidos.stream().filter(p -> p.getEstado() == estado).toList();
            if (delEstado.isEmpty()) {
                continue;
            }
            BigDecimal subtotal = sumar(delEstado, Pedido::getSubtotal);
            BigDecimal descuento = sumar(delEstado, Pedido::getDescuento);
            BigDecimal total = sumar(delEstado, Pedido::getTotal);
            filas.add(Celdas.fila(estado.name(), (long) delEstado.size(), subtotal, descuento, total));
            if (estado != EstadoPedido.ANULADO) {
                cantidadTotal += delEstado.size();
                subtotalTotal = subtotalTotal.add(subtotal);
                descuentoTotal = descuentoTotal.add(descuento);
                totalGeneral = totalGeneral.add(total);
            }
        }
        List<Object> totales = Celdas.fila("Total sin anulados", cantidadTotal, subtotalTotal, descuentoTotal, totalGeneral);
        return new TablaReporteDTO("Por estado del pedido", COLUMNAS_ESTADO, filas, totales);
    }

    private TablaReporteDTO porPreventista(List<Pedido> pedidos) {
        Map<String, List<Pedido>> agrupados = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Pedido p : pedidos) {
            if (p.getEstado() != EstadoPedido.ANULADO) {
                String nombre = Celdas.nombre(p.getPreventista().getNombre(), p.getPreventista().getApellido());
                agrupados.computeIfAbsent(nombre, k -> new ArrayList<>()).add(p);
            }
        }
        List<List<Object>> filas = new ArrayList<>();
        long cantidadTotal = 0;
        BigDecimal totalGeneral = BigDecimal.ZERO;
        for (Map.Entry<String, List<Pedido>> e : agrupados.entrySet()) {
            BigDecimal total = sumar(e.getValue(), Pedido::getTotal);
            filas.add(Celdas.fila(e.getKey(), (long) e.getValue().size(), total));
            cantidadTotal += e.getValue().size();
            totalGeneral = totalGeneral.add(total);
        }
        return new TablaReporteDTO("Por preventista (sin anulados)", COLUMNAS_PREVENTISTA, filas,
                Celdas.fila("Total", cantidadTotal, totalGeneral));
    }

    private static BigDecimal sumar(List<Pedido> pedidos, java.util.function.Function<Pedido, BigDecimal> campo) {
        return pedidos.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}