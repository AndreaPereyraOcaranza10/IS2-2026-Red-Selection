package redSelection.dulceMayor.patrones.strategy.promocion;

import org.springframework.stereotype.Component;
import redSelection.dulceMayor.enums.TipoPromocion;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Promocion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** VOLUMEN: el porcentaje se aplica sobre todo el pedido si suma al menos la cantidad mínima de unidades. */
@Component
public class PromocionPorVolumen implements ReglaPromocion {

    @Override
    public TipoPromocion tipo() {
        return TipoPromocion.VOLUMEN;
    }

    @Override
    public BigDecimal descuento(Promocion promocion, List<LineaPromocion> lineas) {
        int unidades = lineas.stream().mapToInt(LineaPromocion::cantidad).sum();
        int minimo = promocion.getCantidadMinima() == null ? 1 : promocion.getCantidadMinima();
        if (unidades < minimo) {
            throw new ReglaNegocioException("El código " + promocion.getCodigo() + " aplica a partir de "
                    + minimo + " unidades (el pedido tiene " + unidades + ").");
        }
        BigDecimal subtotal = lineas.stream().map(LineaPromocion::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return porcentaje(subtotal, promocion.getPorcentajeDescuento());
    }

    static BigDecimal porcentaje(BigDecimal monto, BigDecimal porcentaje) {
        return monto.multiply(porcentaje).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
