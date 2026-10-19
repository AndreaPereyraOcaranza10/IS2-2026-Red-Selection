package carrito.operaciones;

import java.text.NumberFormat;
import java.util.Locale;

public class FinalizarCompraOperacion extends OperacionCarritoTemplate {
    @Override
    protected void validar(ContextoOperacion contexto) {
        super.validar(contexto);
        if (contexto.carrito.detalles.isEmpty()) {
            throw new IllegalArgumentException("No se puede finalizar una compra sin articulos");
        }
    }

    @Override
    protected void antesDeProcesar(ContextoOperacion contexto) {
        contexto.totalAnterior = contexto.carrito.total;
    }

    @Override
    protected String procesar(ContextoOperacion contexto) {
        contexto.carrito.detalles.clear();
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
        formato.setMaximumFractionDigits(0);
        return "Compra finalizada por " + formato.format(contexto.totalAnterior);
    }
}
