package redSelection.dulceMayor.patrones.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pasarela de demo (sin internet ni credenciales). Activa con {@code app.pagos.pasarela=simulada} (valor por defecto).
 * En producción debe estar apagada: permite "pagar" sin pagar. Con {@code app.pagos.pasarela=mercadopago} entra el adapter real.
 */
@Component
@ConditionalOnProperty(name = "app.pagos.pasarela", havingValue = "simulada", matchIfMissing = true)
public class PasarelaSimulada implements PasarelaPago {

    private static final Logger log = LoggerFactory.getLogger(PasarelaSimulada.class);

    private final Map<String, EstadoOrden> ordenes = new ConcurrentHashMap<>();

    public PasarelaSimulada() {
        log.warn("Pasarela de pago SIMULADA activa (modo demo). En producción usá app.pagos.pasarela=mercadopago.");
    }

    @Override
    public OrdenPasarela crearOrden(String pagoId, BigDecimal monto, String descripcion) {
        String referencia = "SIM-" + UUID.randomUUID();
        ordenes.put(referencia, EstadoOrden.PENDIENTE);
        return new OrdenPasarela(referencia, "DULCEMAYOR-SIMULADO|" + referencia + "|" + monto.toPlainString());
    }

    @Override
    public EstadoOrden consultar(String referencia) {
        return ordenes.getOrDefault(referencia, EstadoOrden.PENDIENTE);
    }

    @Override
    public void cancelar(String referencia) {
        ordenes.computeIfPresent(referencia, (r, estado) -> estado == EstadoOrden.ACREDITADA ? estado : EstadoOrden.CANCELADA);
    }

    @Override
    public boolean esSimulada() { return true; }

    @Override
    public void simularAcreditacion(String referencia) {
        ordenes.compute(referencia, (r, estado) -> estado == EstadoOrden.CANCELADA ? estado : EstadoOrden.ACREDITADA);
    }
}