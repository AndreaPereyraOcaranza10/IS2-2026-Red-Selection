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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Ventas históricas por departamento y preventista (el Excel del Jefe). Cuenta solo pedidos facturados
 * (facturados, en reparto o entregados): los cargados, pendientes de stock o anulados no son ventas.
 * Dos tablas: el detalle (una fila por departamento y preventista) y una matriz departamento x preventista.
 */
@Component
public class ReporteVentas implements ReporteGenerador {

    static final List<EstadoPedido> ESTADOS_VENDIDOS =
            List.of(EstadoPedido.FACTURADO, EstadoPedido.EN_REPARTO, EstadoPedido.ENTREGADO);

    private static final List<String> COLUMNAS_DETALLE = List.of("Departamento", "Preventista", "Pedidos", "Monto");

    private final PedidoRepository pedidoRepository;

    public ReporteVentas(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public TipoReporte tipo() { return TipoReporte.VENTAS; }

    @Override
    public ReporteDTO generar(PeriodoReporte periodo) {
        List<Pedido> vendidos = pedidoRepository
                .findByEliminadoFalseAndFechaHoraCargaBetweenOrderByFechaHoraCargaAsc(periodo.inicio(), periodo.fin())
                .stream()
                .filter(p -> ESTADOS_VENDIDOS.contains(p.getEstado()))
                .filter(p -> periodo.incluyeDepartamento(p.getCliente().getDireccion().getDepartamentoNombre()))
                .toList();

        // departamento -> preventista -> acumulado, ambos ordenados alfabéticamente sin importar mayúsculas
        Map<String, Map<String, Acumulado>> ventas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Set<String> preventistas = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Pedido p : vendidos) {
            String departamento = p.getCliente().getDireccion().getDepartamentoNombre();
            String preventista = Celdas.nombre(p.getPreventista().getNombre(), p.getPreventista().getApellido());
            preventistas.add(preventista);
            ventas.computeIfAbsent(departamento, k -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
                    .computeIfAbsent(preventista, k -> new Acumulado())
                    .sumar(p.getTotal());
        }
        return new ReporteDTO(tipo(), "Ventas por departamento y preventista", periodo.texto(), LocalDateTime.now(),
                List.of(detalle(ventas), matriz(ventas, preventistas)));
    }

    private TablaReporteDTO detalle(Map<String, Map<String, Acumulado>> ventas) {
        List<List<Object>> filas = new ArrayList<>();
        long pedidos = 0;
        BigDecimal monto = BigDecimal.ZERO;
        for (Map.Entry<String, Map<String, Acumulado>> dep : ventas.entrySet()) {
            for (Map.Entry<String, Acumulado> prev : dep.getValue().entrySet()) {
                filas.add(Celdas.fila(dep.getKey(), prev.getKey(), prev.getValue().pedidos, prev.getValue().monto));
                pedidos += prev.getValue().pedidos;
                monto = monto.add(prev.getValue().monto);
            }
        }
        return new TablaReporteDTO("Detalle", COLUMNAS_DETALLE, filas, Celdas.fila("Total", "", pedidos, monto));
    }

    private TablaReporteDTO matriz(Map<String, Map<String, Acumulado>> ventas, Set<String> preventistas) {
        List<String> columnas = new ArrayList<>();
        columnas.add("Departamento");
        columnas.addAll(preventistas);
        columnas.add("Total");

        List<List<Object>> filas = new ArrayList<>();
        BigDecimal[] porColumna = new BigDecimal[preventistas.size()];
        java.util.Arrays.fill(porColumna, BigDecimal.ZERO);
        BigDecimal totalGeneral = BigDecimal.ZERO;
        for (Map.Entry<String, Map<String, Acumulado>> dep : ventas.entrySet()) {
            List<Object> fila = new ArrayList<>();
            fila.add(dep.getKey());
            BigDecimal totalDepartamento = BigDecimal.ZERO;
            int i = 0;
            for (String preventista : preventistas) {
                Acumulado a = dep.getValue().get(preventista);
                BigDecimal monto = a == null ? BigDecimal.ZERO : a.monto;
                fila.add(monto);
                porColumna[i] = porColumna[i].add(monto);
                totalDepartamento = totalDepartamento.add(monto);
                i++;
            }
            fila.add(totalDepartamento);
            filas.add(fila);
            totalGeneral = totalGeneral.add(totalDepartamento);
        }
        List<Object> totales = new ArrayList<>();
        totales.add("Total");
        for (BigDecimal t : porColumna) {
            totales.add(t);
        }
        totales.add(totalGeneral);
        return new TablaReporteDTO("Matriz de montos", columnas, filas, totales);
    }

    private static final class Acumulado {
        private long pedidos;
        private BigDecimal monto = BigDecimal.ZERO;

        void sumar(BigDecimal total) {
            pedidos++;
            monto = monto.add(total);
        }
    }
}