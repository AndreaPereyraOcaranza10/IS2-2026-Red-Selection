package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.TipoPromocion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromocionRespuestaDTO(String id, String codigo, TipoPromocion tipoPromocion,
                                    BigDecimal porcentajeDescuento, Integer cantidadMinima,
                                    String productoId, String productoNombre,
                                    LocalDate fechaDesde, LocalDate fechaHasta, boolean activa,
                                    boolean vigente) { }
