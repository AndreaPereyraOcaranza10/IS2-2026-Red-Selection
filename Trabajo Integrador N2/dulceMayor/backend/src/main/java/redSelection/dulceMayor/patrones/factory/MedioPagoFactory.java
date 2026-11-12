package redSelection.dulceMayor.patrones.factory;

import org.springframework.stereotype.Component;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.model.Pedido;
import redSelection.dulceMayor.model.Pago;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * PATRÓN: Factory (creacional). Dado un TipoPago devuelve el MedioPago que sabe crear ese Pago.
 * Spring inyecta TODAS las implementaciones de MedioPago, así que al agregar una nueva no hay que editar esta clase.
 * Si falta un medio para algún valor del enum, la app falla al arrancar (no en producción, cuando alguien pague).
 */
@Component
public class MedioPagoFactory {

    private final Map<TipoPago, MedioPago> medios = new EnumMap<>(TipoPago.class);

    public MedioPagoFactory(List<MedioPago> implementaciones) {
        for (MedioPago medio : implementaciones) {
            if (medios.put(medio.tipo(), medio) != null) {
                throw new IllegalStateException("Hay más de un MedioPago para " + medio.tipo());
            }
        }
        for (TipoPago tipo : TipoPago.values()) {
            if (!medios.containsKey(tipo)) {
                throw new IllegalStateException("Falta implementar el MedioPago de " + tipo);
            }
        }
    }

    public MedioPago obtener(TipoPago tipo) {
        return medios.get(tipo);
    }

    /** Atajo: crea el Pago pendiente del pedido con el medio indicado. */
    public Pago crear(TipoPago tipo, Pedido pedido) {
        return obtener(tipo).crearPago(pedido);
    }
}