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
import java.util.List;

/** Pedidos cargados en el período, con su estado y total. El total general no cuenta los anulados. */
@Component
public class ReportePedidos implements ReporteGenerador {

    private static final List<String> COLUMNAS = List.of("Fecha", "Pedido", "Comercio", "Preventista", "Estado", "Total");

    private final PedidoRepository pedidoRepository;

    public ReportePedidos(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public TipoReporte tipo() { return TipoReporte.PEDIDOS; }

    @Override
    public ReporteDTO generar(PeriodoReporte periodo) {
        List<Pedido> pedidos = pedidoRepository
                .findByEliminadoFalseAndFechaHoraCargaBetweenOrderByFechaHoraCargaAsc(periodo.inicio(), periodo.fin())
                .stream()
                .filter(p -> periodo.incluyeDepartamento(p.getCliente().getDireccion().getDepartamentoNombre()))
                .toList();
        List<List<Object>> filas = pedidos.stream().map(this::fila).toList();
        BigDecimal total = pedidos.stream()
                .filter(p -> p.getEstado() != EstadoPedido.ANULADO)
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Object> totales = Celdas.fila("Total sin anulados", "", "", "", pedidos.size() + " pedidos", total);
        return new ReporteDTO(tipo(), "Reporte de pedidos", periodo.texto(), LocalDateTime.now(),
                List.of(new TablaReporteDTO("Pedidos", COLUMNAS, filas, totales)));
    }

    private List<Object> fila(Pedido p) {
        return Celdas.fila(
                Celdas.fecha(p.getFechaHoraCarga()),
                Celdas.idCorto(p.getId()),
                p.getCliente().getNombreComercio(),
                Celdas.nombre(p.getPreventista().getNombre(), p.getPreventista().getApellido()),
                p.getEstado().name(),
                p.getTotal());
    }
}