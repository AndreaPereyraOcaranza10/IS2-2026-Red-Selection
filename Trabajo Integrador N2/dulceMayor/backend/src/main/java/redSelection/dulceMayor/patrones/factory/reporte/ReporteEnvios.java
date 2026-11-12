package redSelection.dulceMayor.patrones.factory.reporte;

import org.springframework.stereotype.Component;
import redSelection.dulceMayor.dto.PeriodoReporte;
import redSelection.dulceMayor.dto.ReporteDTO;
import redSelection.dulceMayor.dto.TablaReporteDTO;
import redSelection.dulceMayor.enums.TipoReporte;
import redSelection.dulceMayor.model.Entrega;
import redSelection.dulceMayor.repository.EntregaRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Envíos (despacho y entrega) de los pedidos cargados en el período. */
@Component
public class ReporteEnvios implements ReporteGenerador {

    private static final List<String> COLUMNAS = List.of(
            "Pedido", "Comercio", "Transportista", "Despacho", "Entrega", "Estado del pedido");

    private final EntregaRepository entregaRepository;

    public ReporteEnvios(EntregaRepository entregaRepository) {
        this.entregaRepository = entregaRepository;
    }

    @Override
    public TipoReporte tipo() { return TipoReporte.ENVIOS; }

    @Override
    public ReporteDTO generar(PeriodoReporte periodo) {
        List<Entrega> entregas = entregaRepository
                .findByEliminadoFalseAndPedidoFechaHoraCargaBetweenOrderByPedidoFechaHoraCargaAsc(
                        periodo.inicio(), periodo.fin());
        List<List<Object>> filas = entregas.stream().map(this::fila).toList();
        long entregados = entregas.stream().filter(e -> e.getFechaEntrega() != null).count();
        List<Object> totales = Celdas.fila(
                "Total: " + entregas.size() + " envíos", "", "", "", entregados + " entregados", "");
        return new ReporteDTO(tipo(), "Reporte de envíos", periodo.texto(), LocalDateTime.now(),
                List.of(new TablaReporteDTO("Envíos", COLUMNAS, filas, totales)));
    }

    private List<Object> fila(Entrega e) {
        return Celdas.fila(
                Celdas.idCorto(e.getPedido().getId()),
                e.getPedido().getCliente().getNombreComercio(),
                e.getTransportista() == null ? "Sin asignar"
                        : Celdas.nombre(e.getTransportista().getNombre(), e.getTransportista().getApellido()),
                Celdas.fecha(e.getFechaDespacho()),
                Celdas.fecha(e.getFechaEntrega()),
                e.getPedido().getEstado().name());
    }
}
