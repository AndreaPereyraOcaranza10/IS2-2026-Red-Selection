package redSelection.dulceMayor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Tarea programada: cada tanto anula los pedidos que llevan demasiado esperando el pago, para no tener stock
 * reservado "al pedo". La regla vive en PedidoService (esto solo la dispara). Configurable:
 * app.pedidos.horas-vencimiento-pago (48 por defecto) y app.pedidos.vencimiento-cada-minutos (30).
 */
@Component
public class VencimientoPedidosJob {

    private static final Logger log = LoggerFactory.getLogger(VencimientoPedidosJob.class);

    private final PedidoService pedidoService;

    public VencimientoPedidosJob(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @Scheduled(fixedDelayString = "${app.pedidos.vencimiento-cada-minutos:30}", initialDelay = 1,
            timeUnit = TimeUnit.MINUTES)
    public void anularPedidosSinPago() {
        int anulados = pedidoService.vencerPedidosSinPago();
        if (anulados > 0) {
            log.info("Pedidos vencidos por falta de pago: {} (stock liberado).", anulados);
        }
    }
}