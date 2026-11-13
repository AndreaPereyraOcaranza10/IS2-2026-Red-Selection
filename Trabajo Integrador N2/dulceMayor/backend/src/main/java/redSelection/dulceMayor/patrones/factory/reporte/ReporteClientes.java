package redSelection.dulceMayor.patrones.factory.reporte;

import org.springframework.stereotype.Component;
import redSelection.dulceMayor.dto.PeriodoReporte;
import redSelection.dulceMayor.dto.ReporteDTO;
import redSelection.dulceMayor.dto.TablaReporteDTO;
import redSelection.dulceMayor.enums.TipoReporte;
import redSelection.dulceMayor.model.ClienteMinorista;
import redSelection.dulceMayor.service.PuntoVentaService;

import java.time.LocalDateTime;
import java.util.List;

/** Listado de clientes minoristas activos. No depende del período: es la cartera actual. */
@Component
public class ReporteClientes implements ReporteGenerador {

    private static final List<String> COLUMNAS = List.of(
            "Comercio", "Razón social", "CUIT", "Departamento", "Localidad", "Responsable", "Preventista asignado");

    private final PuntoVentaService puntoVentaService;

    public ReporteClientes(PuntoVentaService puntoVentaService) {
        this.puntoVentaService = puntoVentaService;
    }

    @Override
    public TipoReporte tipo() { return TipoReporte.CLIENTES; }

    @Override
    public ReporteDTO generar(PeriodoReporte periodo) {
        List<List<Object>> filas = puntoVentaService.activosOrdenadosPorNombre().stream()
                .filter(c -> periodo.incluyeDepartamento(c.getDireccion().getDepartamentoNombre()))
                .map(this::fila).toList();
        List<Object> totales = Celdas.fila("Total: " + filas.size() + " clientes", "", "", "", "", "", "");
        return new ReporteDTO(tipo(), "Reporte de clientes", periodo.conDepartamento("Cartera actual"), LocalDateTime.now(),
                List.of(new TablaReporteDTO("Clientes", COLUMNAS, filas, totales)));
    }

    private List<Object> fila(ClienteMinorista c) {
        return Celdas.fila(
                c.getNombreComercio(),
                c.getRazonSocial(),
                c.getCuit(),
                c.getDireccion().getDepartamentoNombre(),
                c.getDireccion().getLocalidadNombre(),
                Celdas.nombre(c.getResponsable().getNombre(), c.getResponsable().getApellido()),
                c.getPreventistaAsignado() == null ? "Sin asignar"
                        : Celdas.nombre(c.getPreventistaAsignado().getNombre(), c.getPreventistaAsignado().getApellido()));
    }
}