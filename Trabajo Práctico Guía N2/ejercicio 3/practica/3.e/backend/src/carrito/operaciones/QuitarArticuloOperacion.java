package carrito.operaciones;

import carrito.modelo.Detalle;

public class QuitarArticuloOperacion extends OperacionCarritoTemplate {
    @Override
    protected void validar(ContextoOperacion contexto) {
        super.validar(contexto);
        if (contexto.articulo == null) {
            throw new IllegalArgumentException("El articulo solicitado no existe");
        }
    }

    @Override
    protected String procesar(ContextoOperacion contexto) {
        Detalle detalle = contexto.carrito.detalles.stream()
                .filter(item -> item.articulo.id.equals(contexto.articulo.id) && !item.eliminado)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El articulo no esta en el carrito"));

        detalle.cantidad--;
        if (detalle.cantidad <= 0) {
            contexto.carrito.detalles.remove(detalle);
        }

        return contexto.articulo.nombre + " actualizado";
    }
}
