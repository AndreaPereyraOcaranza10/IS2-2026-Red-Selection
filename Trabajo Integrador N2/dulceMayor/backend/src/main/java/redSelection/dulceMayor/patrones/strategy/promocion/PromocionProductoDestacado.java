package redSelection.dulceMayor.patrones.strategy.promocion;

import org.springframework.stereotype.Component;
import redSelection.dulceMayor.enums.TipoPromocion;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Promocion;

import java.math.BigDecimal;
import java.util.List;

/** PRODUCTO_DESTACADO: el porcentaje se aplica solo sobre las líneas de ese producto. */
@Component
public class PromocionProductoDestacado implements ReglaPromocion {

    @Override
    public TipoPromocion tipo() {
        return TipoPromocion.PRODUCTO_DESTACADO;
    }

    @Override
    public BigDecimal descuento(Promocion promocion, List<LineaPromocion> lineas) {
        String productoId = promocion.getProducto().getId();
        BigDecimal subtotalDelProducto = lineas.stream()
                .filter(l -> l.productoId().equals(productoId))
                .map(LineaPromocion::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (subtotalDelProducto.signum() == 0) {
            throw new ReglaNegocioException("El código " + promocion.getCodigo() + " es para "
                    + promocion.getProducto().getNombre() + ", que no está en el pedido.");
        }
        return PromocionPorVolumen.porcentaje(subtotalDelProducto, promocion.getPorcentajeDescuento());
    }
}
