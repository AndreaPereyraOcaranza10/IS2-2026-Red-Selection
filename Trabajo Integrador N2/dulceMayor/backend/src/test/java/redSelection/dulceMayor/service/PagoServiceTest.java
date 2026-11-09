package redSelection.dulceMayor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import redSelection.dulceMayor.dto.PagoCrearDTO;
import redSelection.dulceMayor.dto.PagoRespuestaDTO;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoEmpresa;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.patrones.factory.*;
import redSelection.dulceMayor.repository.DulceMayorRepository;
import redSelection.dulceMayor.repository.PagoRepository;
import redSelection.dulceMayor.security.UsuarioActual;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** BK-07 / BK-15: reglas de PagoService con repositorios simulados (sin base de datos). */
class PagoServiceTest {

    private PagoRepository pagos;
    private FacturaClienteService facturas;
    private UsuarioActual usuarioActual;
    private PagoService servicio;
    private FacturaCliente factura;
    private final List<Pago> enBd = new ArrayList<>();

    @BeforeEach
    void preparar() {
        pagos = mock(PagoRepository.class);
        facturas = mock(FacturaClienteService.class);
        usuarioActual = mock(UsuarioActual.class);
        DulceMayorRepository empresas = mock(DulceMayorRepository.class);
        when(empresas.findByEliminadoFalse()).thenReturn(List.of(DulceMayor.builder()
                .tipoEmpresa(TipoEmpresa.SEDE_CENTRAL).aliasTransferencia("ALIAS.TEST").build()));
        MedioPagoFactory factory = new MedioPagoFactory(List.of(new EfectivoMedioPago(),
                new TransferenciaMedioPago(empresas), new BilleteraVirtualMedioPago("http://app/pagos")));
        servicio = new PagoService(pagos, facturas, factory, usuarioActual);

        ResponsableComercio responsable = ResponsableComercio.builder().build();
        responsable.setId("resp-1");
        ClienteMinorista cliente = ClienteMinorista.builder().responsable(responsable).build();
        Pedido pedido = Pedido.builder().cliente(cliente).build();
        factura = FacturaCliente.builder().numeroFactura(7L).total(new BigDecimal("2000.00"))
                .estado(EstadoFactura.EMITIDA).pedido(pedido).build();
        factura.setId("f1");

        when(facturas.obtenerEntidad("f1")).thenReturn(factura);
        when(pagos.findByFacturaIdAndEliminadoFalse("f1")).thenAnswer(inv -> enBd);
        when(pagos.save(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            if (p.getId() == null) p.setId("p" + (enBd.size() + 1));
            enBd.add(p);
            return p;
        });
        when(pagos.buscarConBloqueo(any())).thenAnswer(inv ->
                enBd.stream().filter(p -> p.getId().equals(inv.getArgument(0))).findFirst());
        actuaComo(Rol.ADMINISTRATIVO, null);
    }

    private void actuaComo(Rol rol, String empleadoId) {
        when(usuarioActual.obtener()).thenReturn(new UsuarioAutenticado("u1", "test", rol, false, empleadoId, 0));
    }

    @Test @DisplayName("Iniciar transferencia: el monto sale de la factura, queda PENDIENTE y muestra el alias")
    void iniciaTransferencia() {
        PagoRespuestaDTO r = servicio.iniciar("f1", new PagoCrearDTO(TipoPago.TRANSFERENCIA));
        assertEquals(new BigDecimal("2000.00"), r.monto());
        assertEquals(EstadoPago.PENDIENTE, r.estado());
        assertTrue(r.instrucciones().contains("ALIAS.TEST"));
    }

    @Test @DisplayName("Iniciar efectivo: queda PENDIENTE (se cobra al entregar)")
    void iniciaEfectivo() {
        PagoRespuestaDTO r = servicio.iniciar("f1", new PagoCrearDTO(TipoPago.EFECTIVO));
        assertEquals(EstadoPago.PENDIENTE, r.estado());
        assertNull(r.codigoQr());
    }

    @Test @DisplayName("La billetera virtual todavía no está habilitada y no deja nada guardado")
    void billeteraNoDisponible() {
        assertThrows(ReglaNegocioException.class, () -> servicio.iniciar("f1", new PagoCrearDTO(TipoPago.BILLETERA_VIRTUAL)));
        assertTrue(enBd.isEmpty());
    }

    @Test @DisplayName("No se paga una factura anulada")
    void facturaAnulada() {
        factura.setEstado(EstadoFactura.ANULADA);
        assertThrows(ReglaNegocioException.class, () -> servicio.iniciar("f1", new PagoCrearDTO(TipoPago.EFECTIVO)));
    }

    @Test @DisplayName("Cambiar de medio anula el pago pendiente anterior")
    void cambioDeMedio() {
        servicio.iniciar("f1", new PagoCrearDTO(TipoPago.EFECTIVO));
        servicio.iniciar("f1", new PagoCrearDTO(TipoPago.TRANSFERENCIA));
        assertEquals(EstadoPago.ANULADO, enBd.get(0).getEstado());
        assertEquals(EstadoPago.PENDIENTE, enBd.get(1).getEstado());
    }

    @Test @DisplayName("Confirmar deja el pago CONFIRMADO con fecha, la factura PAGADA, y no admite otro pago")
    void confirmar() {
        PagoRespuestaDTO creado = servicio.iniciar("f1", new PagoCrearDTO(TipoPago.TRANSFERENCIA));
        PagoRespuestaDTO r = servicio.confirmar(creado.id());
        assertEquals(EstadoPago.CONFIRMADO, r.estado());
        assertNotNull(r.fechaPago());
        assertEquals(EstadoFactura.PAGADA, factura.getEstado());
        assertThrows(ReglaNegocioException.class, () -> servicio.iniciar("f1", new PagoCrearDTO(TipoPago.EFECTIVO)));
    }

    @Test @DisplayName("Idempotencia: confirmar dos veces no cambia nada (misma fecha, un solo pago)")
    void confirmarEsIdempotente() {
        PagoRespuestaDTO creado = servicio.iniciar("f1", new PagoCrearDTO(TipoPago.EFECTIVO));
        PagoRespuestaDTO primera = servicio.confirmar(creado.id());
        PagoRespuestaDTO segunda = servicio.confirmar(creado.id());
        assertEquals(primera.fechaPago(), segunda.fechaPago());
        assertEquals(1, enBd.size());
    }

    @Test @DisplayName("confirmarFactura crea y confirma el pago; repetirla devuelve el mismo, sin duplicar")
    void confirmarFacturaEsIdempotente() {
        PagoRespuestaDTO primera = servicio.confirmarFactura("f1", TipoPago.EFECTIVO);
        PagoRespuestaDTO segunda = servicio.confirmarFactura("f1", TipoPago.EFECTIVO);
        assertEquals(EstadoPago.CONFIRMADO, primera.estado());
        assertEquals(primera.id(), segunda.id());
        assertEquals(1, enBd.size());
        assertEquals(EstadoFactura.PAGADA, factura.getEstado());
    }

    @Test @DisplayName("confirmarFactura con otro medio sobre una factura ya paga es un error")
    void confirmarFacturaOtroMedio() {
        servicio.confirmarFactura("f1", TipoPago.EFECTIVO);
        assertThrows(ReglaNegocioException.class, () -> servicio.confirmarFactura("f1", TipoPago.TRANSFERENCIA));
    }

    @Test @DisplayName("Un pago anulado no se puede confirmar")
    void anuladoNoSeConfirma() {
        PagoRespuestaDTO creado = servicio.iniciar("f1", new PagoCrearDTO(TipoPago.EFECTIVO));
        servicio.anular(creado.id());
        assertThrows(ReglaNegocioException.class, () -> servicio.confirmar(creado.id()));
    }

    @Test @DisplayName("El cliente no puede confirmar su propio pago")
    void clienteNoConfirma() {
        PagoRespuestaDTO creado = servicio.iniciar("f1", new PagoCrearDTO(TipoPago.TRANSFERENCIA));
        actuaComo(Rol.CLIENTE, "resp-1");
        assertThrows(AccessDeniedException.class, () -> servicio.confirmar(creado.id()));
    }

    @Test @DisplayName("El cliente ve las facturas de su comercio pero no las de otro")
    void clienteSoloVeLoSuyo() {
        actuaComo(Rol.CLIENTE, "resp-1");
        assertDoesNotThrow(() -> servicio.listarPorFactura("f1"));
        actuaComo(Rol.CLIENTE, "otro-resp");
        assertThrows(AccessDeniedException.class, () -> servicio.listarPorFactura("f1"));
    }
}