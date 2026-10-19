package carrito.operaciones;

import carrito.modelo.Detalle;
import java.util.Optional;

public class AgregarArticuloOperacion extends OperacionCarritoTemplate {
    @Override
    protected void validar(ContextoOperacion contexto) {
        super.validar(contexto);
        if (contexto.articulo == null || contexto.articulo.eliminado) {
            throw new IllegalArgumentException("El articulo solicitado no existe");
        }
    }

    @Override
    protected String procesar(ContextoOperacion contexto) {
        Optional<Detalle> detalle = contexto.carrito.detalles.stream()
                .filter(item -> item.articulo.id.equals(contexto.articulo.id) && !item.eliminado)
                .findFirst();

        if (detalle.isPresent()) {
            detalle.get().cantidad++;
        } else {
            contexto.carrito.detalles.add(new Detalle("det-" + System.nanoTime(), contexto.articulo));
        }

        return contexto.articulo.nombre + " agregado al carrito";
    }
}
