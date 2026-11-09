package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.PromocionCrearDTO;
import redSelection.dulceMayor.dto.PromocionRespuestaDTO;
import redSelection.dulceMayor.dto.PromocionValidadaDTO;
import redSelection.dulceMayor.enums.TipoPromocion;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Producto;
import redSelection.dulceMayor.model.Promocion;
import redSelection.dulceMayor.patrones.strategy.promocion.ReglaPromocion;
import redSelection.dulceMayor.patrones.strategy.promocion.ReglaPromocion.LineaPromocion;
import redSelection.dulceMayor.repository.PromocionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Promociones con código (BK-06).
 * - ABM con Template Method (BaseService): alta, vigencia, porcentaje y baja lógica.
 * - Validación del código para el preventista y cálculo del descuento con Strategy (ReglaPromocion).
 * El descuento definitivo siempre lo calcula el backend al cargar el pedido; el frontend solo lo muestra.
 */
@Service
public class PromocionService extends BaseService<Promocion, PromocionCrearDTO, PromocionRespuestaDTO> {

    private final PromocionRepository promocionRepository;
    private final ProductoService productoService;
    private final Map<TipoPromocion, ReglaPromocion> reglas = new EnumMap<>(TipoPromocion.class);

    public PromocionService(PromocionRepository promocionRepository, ProductoService productoService,
                            List<ReglaPromocion> reglas) {
        super(promocionRepository);
        this.promocionRepository = promocionRepository;
        this.productoService = productoService;
        reglas.forEach(r -> this.reglas.put(r.tipo(), r));
        for (TipoPromocion tipo : TipoPromocion.values()) {
            if (!this.reglas.containsKey(tipo)) {
                throw new IllegalStateException("Falta la regla de promoción para " + tipo);
            }
        }
    }

    // =====================================================
    // ABM (Template Method)
    // =====================================================

    @Override protected String nombreRecurso() { return "Promoción"; }

    @Override
    protected Promocion aEntidad(PromocionCrearDTO dto) {
        Promocion promocion = Promocion.builder().build();
        aplicarCambios(promocion, dto);
        return promocion;
    }

    @Override
    protected void aplicarCambios(Promocion p, PromocionCrearDTO dto) {
        p.setCodigo(codigo(dto.codigo()));
        p.setTipoPromocion(dto.tipoPromocion());
        p.setPorcentajeDescuento(dto.porcentajeDescuento());
        p.setFechaDesde(dto.fechaDesde());
        p.setFechaHasta(dto.fechaHasta());
        p.setActiva(dto.activa() == null || dto.activa());
        // Cada tipo guarda solo el dato que usa
        boolean porVolumen = dto.tipoPromocion() == TipoPromocion.VOLUMEN;
        p.setCantidadMinima(porVolumen ? dto.cantidadMinima() : null);
        p.setProducto(porVolumen ? null : productoService.obtenerEntidad(dto.productoId()));
    }

    @Override
    protected PromocionRespuestaDTO aRespuesta(Promocion p) {
        Producto producto = p.getProducto();
        return new PromocionRespuestaDTO(p.getId(), p.getCodigo(), p.getTipoPromocion(), p.getPorcentajeDescuento(),
                p.getCantidadMinima(), producto == null ? null : producto.getId(),
                producto == null ? null : producto.getNombre(),
                p.getFechaDesde(), p.getFechaHasta(), p.isActiva(), motivoNoVigente(p, LocalDate.now()) == null);
    }

    @Override
    protected void validarCreacion(PromocionCrearDTO dto) {
        validarDatos(dto);
        // El código es único aunque la promoción anterior se haya dado de baja (la base lo exige así)
        promocionRepository.findByCodigoIgnoreCase(codigo(dto.codigo())).ifPresent(p -> {
            throw new ReglaNegocioException("Ya existe una promoción con el código " + p.getCodigo() + ".");
        });
    }

    @Override
    protected void validarActualizacion(Promocion actual, PromocionCrearDTO dto) {
        validarDatos(dto);
        promocionRepository.findByCodigoIgnoreCase(codigo(dto.codigo()))
                .filter(otra -> !otra.getId().equals(actual.getId()))
                .ifPresent(p -> {
                    throw new ReglaNegocioException("Ya existe otra promoción con el código " + p.getCodigo() + ".");
                });
    }

    private void validarDatos(PromocionCrearDTO dto) {
        if (dto.fechaHasta() != null && dto.fechaHasta().isBefore(dto.fechaDesde())) {
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la de inicio.");
        }
        if (dto.tipoPromocion() == TipoPromocion.VOLUMEN && dto.cantidadMinima() == null) {
            throw new ReglaNegocioException("Una promoción por volumen necesita la cantidad mínima de unidades.");
        }
        if (dto.tipoPromocion() == TipoPromocion.PRODUCTO_DESTACADO
                && (dto.productoId() == null || dto.productoId().isBlank())) {
            throw new ReglaNegocioException("Una promoción de producto destacado necesita el producto.");
        }
    }

    // =====================================================
    // USO EN PEDIDOS
    // =====================================================

    /** Para el botón "Aplicar" del preventista: nunca lanza error, responde si el código sirve y por qué. */
    @Transactional(readOnly = true)
    public PromocionValidadaDTO validar(String codigoIngresado) {
        if (codigoIngresado == null || codigoIngresado.isBlank()) {
            return PromocionValidadaDTO.invalida("Ingresá un código.");
        }
        Promocion p = promocionRepository.findByCodigoIgnoreCaseAndEliminadoFalse(codigo(codigoIngresado)).orElse(null);
        if (p == null) {
            return PromocionValidadaDTO.invalida("El código no existe.");
        }
        String motivo = motivoNoVigente(p, LocalDate.now());
        if (motivo != null) {
            return PromocionValidadaDTO.invalida(motivo);
        }
        Producto producto = p.getProducto();
        return new PromocionValidadaDTO(true, p.getPorcentajeDescuento(), p.getTipoPromocion().name(),
                p.getCantidadMinima(), producto == null ? null : producto.getId(),
                producto == null ? null : producto.getNombre(), null);
    }

    /** La promoción vigente de ese código, o ReglaNegocioException (422) con el motivo. */
    @Transactional(readOnly = true)
    public Promocion vigentePorCodigo(String codigoIngresado) {
        Promocion p = promocionRepository.findByCodigoIgnoreCaseAndEliminadoFalse(codigo(codigoIngresado))
                .orElseThrow(() -> new ReglaNegocioException("El código de promoción no existe."));
        String motivo = motivoNoVigente(p, LocalDate.now());
        if (motivo != null) {
            throw new ReglaNegocioException(motivo);
        }
        return p;
    }

    /** Descuento en pesos de la promoción sobre esas líneas, según su estrategia (Strategy). */
    public BigDecimal calcularDescuento(Promocion promocion, List<LineaPromocion> lineas) {
        return reglas.get(promocion.getTipoPromocion()).descuento(promocion, lineas);
    }

    /** null si está vigente hoy; si no, el motivo para mostrarle al usuario. */
    private static String motivoNoVigente(Promocion p, LocalDate hoy) {
        if (!p.isActiva()) return "El código " + p.getCodigo() + " está desactivado.";
        if (hoy.isBefore(p.getFechaDesde())) return "El código " + p.getCodigo() + " empieza a regir el " + p.getFechaDesde() + ".";
        if (p.getFechaHasta() != null && hoy.isAfter(p.getFechaHasta())) return "El código " + p.getCodigo() + " venció el " + p.getFechaHasta() + ".";
        return null;
    }

    private static String codigo(String valor) {
        return valor.trim().toUpperCase();
    }
}
