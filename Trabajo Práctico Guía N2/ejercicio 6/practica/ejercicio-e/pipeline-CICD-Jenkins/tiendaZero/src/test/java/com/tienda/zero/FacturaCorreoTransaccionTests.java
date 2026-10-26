package com.tienda.zero;

import com.tienda.zero.service.FacturaCorreoPendiente;
import com.tienda.zero.service.FacturaCorreoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.*;

@SpringBootTest
class FacturaCorreoTransaccionTests {
    @Autowired private PlatformTransactionManager transacciones;
    @Autowired private ApplicationEventPublisher eventos;
    @MockitoBean private FacturaCorreoService correos;

    @Test
    void soloEnviaDespuesDeQueLaCompraQuedaConfirmada() {
        new TransactionTemplate(transacciones).executeWithoutResult(status -> {
            eventos.publishEvent(new FacturaCorreoPendiente("confirmada"));
            verify(correos, never()).enviar("confirmada");
        });
        verify(correos).enviar("confirmada");
    }

    @Test
    void noEnviaSiLaCompraSeDeshace() {
        new TransactionTemplate(transacciones).executeWithoutResult(status -> {
            eventos.publishEvent(new FacturaCorreoPendiente("deshecha"));
            status.setRollbackOnly();
        });
        verify(correos, never()).enviar("deshecha");
    }
}
