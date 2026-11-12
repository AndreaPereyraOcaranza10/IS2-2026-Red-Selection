package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.ConfiguracionCorreoDTO;
import redSelection.dulceMayor.dto.ConfiguracionCorreoRespuestaDTO;
import redSelection.dulceMayor.enums.TipoCorreo;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.ConfiguracionCorreoEmpresa;
import redSelection.dulceMayor.model.DulceMayor;
import redSelection.dulceMayor.patrones.facade.NotificacionFacade;
import redSelection.dulceMayor.repository.ConfiguracionCorreoEmpresaRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Plantillas de los correos de la empresa (asunto + cuerpo HTML), editables por administración.
 * No extiende BaseService a propósito: no se opera por id sino por TIPO de correo (hay una plantilla por tipo y
 * por empresa), y el "alta" y la "modificación" son la misma operación (guardar la plantilla del tipo).
 * Los correos sin plantilla guardada usan el texto por defecto del código (NotificacionFacade).
 */
@Service
public class ConfiguracionCorreoService {

    private static final Pattern MARCADOR = Pattern.compile("\\{\\{\\s*([a-zA-Z]+)\\s*}}");
    /** Defensa en profundidad: el HTML lo escribe administración, pero se manda a clientes y se previsualiza en pantalla. */
    private static final Pattern HTML_PELIGROSO = Pattern.compile(
            "<\\s*(script|iframe|object|embed|form)\\b|\\son[a-z]+\\s*=|javascript\\s*:", Pattern.CASE_INSENSITIVE);

    private final ConfiguracionCorreoEmpresaRepository configuracionRepository;
    private final DulceMayorService dulceMayorService;

    public ConfiguracionCorreoService(ConfiguracionCorreoEmpresaRepository configuracionRepository,
                                      DulceMayorService dulceMayorService) {
        this.configuracionRepository = configuracionRepository;
        this.dulceMayorService = dulceMayorService;
    }

    @Transactional(readOnly = true)
    public List<ConfiguracionCorreoRespuestaDTO> listar() {
        DulceMayor sede = sedeCentral();
        return Arrays.stream(TipoCorreo.values()).map(tipo -> aRespuesta(sede, tipo)).toList();
    }

    @Transactional(readOnly = true)
    public ConfiguracionCorreoRespuestaDTO obtener(TipoCorreo tipo) {
        return aRespuesta(sedeCentral(), tipo);
    }

    /** Plantilla personalizada vigente de la casa central para ese tipo de correo (vacío = se usa el texto por defecto). */
    @Transactional(readOnly = true)
    public Optional<ConfiguracionCorreoEmpresa> plantillaPersonalizada(TipoCorreo tipo) {
        return dulceMayorService.sedeCentral()
                .flatMap(sede -> configuracionRepository.findByDulceMayorIdAndTipoCorreo(sede.getId(), tipo))
                .filter(c -> !c.isEliminado());
    }

    @Transactional
    public ConfiguracionCorreoRespuestaDTO guardar(TipoCorreo tipo, ConfiguracionCorreoDTO dto) {
        validarContenido(tipo, dto);
        DulceMayor sede = sedeCentral();
        ConfiguracionCorreoEmpresa configuracion = configuracionRepository
                .findByDulceMayorIdAndTipoCorreo(sede.getId(), tipo)
                .orElseGet(() -> ConfiguracionCorreoEmpresa.builder().dulceMayor(sede).tipoCorreo(tipo).build());
        configuracion.setEliminado(false);
        configuracion.setAsunto(dto.asunto().trim());
        configuracion.setCuerpoHtml(dto.cuerpoHtml().trim());
        configuracionRepository.save(configuracion);
        return aRespuesta(sede, tipo);
    }

    /** Vuelve al texto por defecto (borra la plantilla personalizada). */
    @Transactional
    public ConfiguracionCorreoRespuestaDTO restaurar(TipoCorreo tipo) {
        DulceMayor sede = sedeCentral();
        configuracionRepository.findByDulceMayorIdAndTipoCorreo(sede.getId(), tipo)
                .ifPresent(configuracionRepository::delete);
        return aRespuesta(sede, tipo);
    }

    // ---------- Reglas ----------

    /** Solo se aceptan los {{marcadores}} que ese correo sabe completar: un marcador desconocido saldría tal cual al cliente. */
    private void validarContenido(TipoCorreo tipo, ConfiguracionCorreoDTO dto) {
        if (dto.asunto().isBlank() || dto.cuerpoHtml().isBlank()) {
            throw new ReglaNegocioException("El asunto y el cuerpo no pueden estar vacíos.");
        }
        if (HTML_PELIGROSO.matcher(dto.cuerpoHtml()).find() || HTML_PELIGROSO.matcher(dto.asunto()).find()) {
            throw new ReglaNegocioException("El texto no puede incluir scripts, formularios ni código ejecutable.");
        }
        Set<String> permitidos = new HashSet<>(marcadores(tipo));
        for (String texto : List.of(dto.asunto(), dto.cuerpoHtml())) {
            Matcher m = MARCADOR.matcher(texto);
            while (m.find()) {
                if (!permitidos.contains(m.group(1))) {
                    throw new ReglaNegocioException("El marcador {{" + m.group(1) + "}} no existe para este correo. "
                            + (permitidos.isEmpty() ? "Este correo no admite marcadores."
                            : "Los disponibles son: " + String.join(", ", marcadores(tipo)) + "."));
                }
            }
        }
    }

    /** Marcadores que cada correo completa hoy. Si se suma un tipo que se envía, se declaran acá. */
    static List<String> marcadores(TipoCorreo tipo) {
        return switch (tipo) {
            case PROXIMA_ENTREGA -> List.of("responsable", "comercio", "direccion", "pedido");
            case CONFIRMACION_PEDIDO -> List.of("responsable", "comercio", "numero", "total", "productos");
            case NEWSLETTER -> List.of("responsable", "fecha", "productos");
        };
    }

    // ---------- Auxiliares ----------

    private ConfiguracionCorreoRespuestaDTO aRespuesta(DulceMayor sede, TipoCorreo tipo) {
        Optional<ConfiguracionCorreoEmpresa> guardada = configuracionRepository
                .findByDulceMayorIdAndTipoCorreo(sede.getId(), tipo).filter(c -> !c.isEliminado());
        if (guardada.isPresent()) {
            return new ConfiguracionCorreoRespuestaDTO(tipo, guardada.get().getAsunto(), guardada.get().getCuerpoHtml(),
                    true, marcadores(tipo));
        }
        String asunto = switch (tipo) {
            case PROXIMA_ENTREGA -> NotificacionFacade.ASUNTO_POR_DEFECTO;
            case CONFIRMACION_PEDIDO -> NotificacionFacade.ASUNTO_FACTURA;
            case NEWSLETTER -> NotificacionFacade.ASUNTO_NEWSLETTER;
        };
        String cuerpo = switch (tipo) {
            case PROXIMA_ENTREGA -> NotificacionFacade.CUERPO_POR_DEFECTO;
            case CONFIRMACION_PEDIDO -> NotificacionFacade.CUERPO_FACTURA;
            case NEWSLETTER -> NotificacionFacade.CUERPO_NEWSLETTER;
        };
        return new ConfiguracionCorreoRespuestaDTO(tipo, asunto, cuerpo, false, marcadores(tipo));
    }

    private DulceMayor sedeCentral() {
        return dulceMayorService.sedeCentral()
                .orElseThrow(() -> new ReglaNegocioException("No hay una casa central cargada."));
    }
}