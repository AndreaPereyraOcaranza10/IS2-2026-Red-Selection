package redSelection.dulceMayor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import redSelection.dulceMayor.model.FacturaCliente;
import redSelection.dulceMayor.patrones.facade.NotificacionFacade;

/**
 * BK-08: correo de confirmación al responsable del comercio cuando su pedido se factura, con la factura en PDF.
 *
 * Escucha el evento FacturaEmitidaEvento recién cuando la transacción se confirmó (si el pedido se revirtió, no
 * sale ningún correo) y lo procesa en segundo plano (@Async), para que quien cargó el pedido no espere al servidor
 * de correo. Un fallo solo se registra en el log: la factura ya está emitida y no se toca.
 */
@Service
public class CorreoFacturaService {

    private static final Logger log = LoggerFactory.getLogger(CorreoFacturaService.class);

    private final FacturaClienteService facturas;
    private final FacturaPdf pdf;
    private final NotificacionFacade notificaciones;

    public CorreoFacturaService(FacturaClienteService facturas, FacturaPdf pdf, NotificacionFacade notificaciones) {
        this.facturas = facturas;
        this.pdf = pdf;
        this.notificaciones = notificaciones;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void alEmitirFactura(FacturaEmitidaEvento evento) {
        enviar(evento.facturaId());
    }

    /** @return true si el correo salió. Nunca lanza: cualquier problema queda en el log. */
    @Transactional(readOnly = true)
    public boolean enviar(String facturaId) {
        try {
            FacturaCliente factura = facturas.obtenerEntidad(facturaId);
            String numero = FacturaPdf.numero(factura);
            boolean enviado = notificaciones.enviarFactura(factura, pdf.generar(factura), numero,
                    FacturaPdf.pesos(factura.getTotal()));
            if (enviado) {
                log.info("Factura N° {} enviada por correo a {}", numero,
                        factura.getPedido().getCliente().getNombreComercio());
            } else {
                log.warn("No se envió la factura N° {} de {}: el responsable no tiene correo cargado o falló el envío.",
                        numero, factura.getPedido().getCliente().getNombreComercio());
            }
            return enviado;
        } catch (RuntimeException e) {
            log.error("Falló el envío de la factura {} por correo", facturaId, e);
            return false;
        }
    }
}
