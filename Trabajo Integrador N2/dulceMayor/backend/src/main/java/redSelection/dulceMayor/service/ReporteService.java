package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.ArchivoReporteDTO;
import redSelection.dulceMayor.dto.PeriodoReporte;
import redSelection.dulceMayor.dto.ReporteDTO;
import redSelection.dulceMayor.enums.FormatoReporte;
import redSelection.dulceMayor.enums.TipoReporte;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.patrones.factory.reporte.ReporteFactory;
import redSelection.dulceMayor.patrones.strategy.exportacion.ExportadorReporte;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Reportes (BK-10): pantalla, PDF y Excel a partir de la misma tabla de datos.
 *  - QUÉ reporte se arma lo decide ReporteFactory (Factory Method);
 *  - EN QUÉ formato sale lo decide el ExportadorReporte elegido (Strategy).
 * Este service solo orquesta: valida el período, pide el reporte y, si corresponde, lo exporta.
 * No extiende BaseService: no es un CRUD, es un caso de uso de consulta.
 */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final ReporteFactory factory;
    private final Map<FormatoReporte, ExportadorReporte> exportadores = new EnumMap<>(FormatoReporte.class);

    public ReporteService(ReporteFactory factory, List<ExportadorReporte> disponibles) {
        this.factory = factory;
        for (ExportadorReporte exportador : disponibles) {
            exportadores.put(exportador.formato(), exportador);
        }
    }

    // ---------- Reportes operativos (administración) ----------

    public ReporteDTO operativo(String tipo, LocalDate desde, LocalDate hasta) {
        return generar(tipoOperativo(tipo), desde, hasta);
    }

    public ArchivoReporteDTO operativoExportado(String tipo, String formato, LocalDate desde, LocalDate hasta) {
        return exportar(tipoOperativo(tipo), formato, desde, hasta);
    }

    // ---------- Reportes globales (solo Jefe Administrativo) ----------

    public ReporteDTO ventas(LocalDate desde, LocalDate hasta) {
        return generar(TipoReporte.VENTAS, desde, hasta);
    }

    public ArchivoReporteDTO ventasExportado(String formato, LocalDate desde, LocalDate hasta) {
        return exportar(TipoReporte.VENTAS, formato, desde, hasta);
    }

    // ---------- Núcleo ----------

    private ReporteDTO generar(TipoReporte tipo, LocalDate desde, LocalDate hasta) {
        return factory.crear(tipo).generar(PeriodoReporte.de(desde, hasta));
    }

    private ArchivoReporteDTO exportar(TipoReporte tipo, String textoFormato, LocalDate desde, LocalDate hasta) {
        FormatoReporte formato = FormatoReporte.desdeTexto(textoFormato)
                .orElseThrow(() -> new ReglaNegocioException("Formato no soportado. Usá pdf o excel."));
        PeriodoReporte periodo = PeriodoReporte.de(desde, hasta);   // valida antes de armar el reporte
        ReporteDTO reporte = factory.crear(tipo).generar(periodo);
        byte[] contenido = exportadores.get(formato).exportar(reporte);
        String nombre = "reporte-" + tipo.name().toLowerCase() + "-" + periodo.sufijoArchivo() + "." + formato.extension();
        return new ArchivoReporteDTO(nombre, formato.contentType(), contenido);
    }

    /** Un tipo que no existe o que es global no se puede pedir por la ruta operativa (la seguridad por ruta no lo cubriría). */
    private TipoReporte tipoOperativo(String texto) {
        TipoReporte tipo = TipoReporte.desdeTexto(texto)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el reporte '" + texto + "'."));
        if (tipo.esGlobal()) {
            throw new RecursoNoEncontradoException("No existe el reporte '" + texto + "'.");
        }
        return tipo;
    }
}
