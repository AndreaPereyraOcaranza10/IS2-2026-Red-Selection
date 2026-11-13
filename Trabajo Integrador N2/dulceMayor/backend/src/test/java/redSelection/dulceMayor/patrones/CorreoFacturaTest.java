package redSelection.dulceMayor.patrones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import redSelection.dulceMayor.enums.TipoContacto;
import redSelection.dulceMayor.enums.TipoCorreo;
import redSelection.dulceMayor.enums.TipoEmpresa;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.patrones.adapter.EnvioCorreo;
import redSelection.dulceMayor.patrones.builder.Notificacion;
import redSelection.dulceMayor.patrones.facade.NotificacionFacade;
import redSelection.dulceMayor.repository.ConfiguracionCorreoEmpresaRepository;
import redSelection.dulceMayor.service.ConfiguracionCorreoService;
import redSelection.dulceMayor.service.CorreoFacturaService;
import redSelection.dulceMayor.service.DulceMayorService;
import redSelection.dulceMayor.service.FacturaClienteService;
import redSelection.dulceMayor.service.FacturaPdf;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** BK-08: el correo de confirmación lleva los datos del pedido y la factura en PDF; un fallo no rompe nada. */
class CorreoFacturaTest {

    private static final byte[] PDF = "%PDF-1.4 factura".getBytes();

    private EnvioCorreo envio;
    private ConfiguracionCorreoEmpresaRepository plantillas;
    private NotificacionFacade facade;
    private DulceMayor sede;
    private ResponsableComercio responsable;
    private FacturaCliente factura;

    @BeforeEach
    void preparar() {
        envio = mock(EnvioCorreo.class);
        when(envio.enviar(any())).thenReturn(true);
        plantillas = mock(ConfiguracionCorreoEmpresaRepository.class);
        DulceMayorService empresas = mock(DulceMayorService.class);
        sede = DulceMayor.builder().tipoEmpresa(TipoEmpresa.SEDE_CENTRAL).build();
        sede.setId("sede");
        when(empresas.sedeCentral()).thenReturn(Optional.of(sede));
        facade = new NotificacionFacade(new ConfiguracionCorreoService(plantillas, empresas), envio);

        responsable = ResponsableComercio.builder().nombre("María").apellido("Gómez").contactos(new ArrayList<>()).build();
        responsable.getContactos().add(ContactoCorreoElectronico.builder().tipoContacto(TipoContacto.LABORAL)
                .principal(true).email("maria@drugstore.com").build());
        ClienteMinorista cliente = ClienteMinorista.builder().nombreComercio("Drugstore 24 hs").responsable(responsable).build();
        Pedido pedido = Pedido.builder().cliente(cliente).build();
        factura = FacturaCliente.builder().numeroFactura(12L).pedido(pedido).total(new BigDecimal("36800"))
                .detalles(new ArrayList<>()).build();
        factura.getDetalles().add(DetalleFactura.builder().factura(factura).cantidad(2)
                .producto(Producto.builder().nombre("Alfajor triple chocolate").build()).build());
    }

    private Notificacion enviada() {
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(envio).enviar(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Va al correo principal del responsable, con el detalle del pedido y la factura en PDF adjunta")
    void correoConFacturaAdjunta() {
        assertTrue(facade.enviarFactura(factura, PDF, "00000012", "$ 36.800,00"));

        Notificacion n = enviada();
        assertEquals("maria@drugstore.com", n.destinatario());
        assertEquals("Dulce Mayor · Tu pedido está confirmado (Factura N° 00000012)", n.asunto());
        assertTrue(n.cuerpoHtml().contains("María Gómez"));
        assertTrue(n.cuerpoHtml().contains("Drugstore 24 hs"));
        assertTrue(n.cuerpoHtml().contains("$ 36.800,00"));
        assertTrue(n.cuerpoHtml().contains("2 × Alfajor triple chocolate"));
        assertEquals("Factura-00000012.pdf", n.adjunto().nombre());
        assertEquals("application/pdf", n.adjunto().tipo());
        assertArrayEquals(PDF, n.adjunto().contenido());
    }

    @Test
    @DisplayName("Si administración configuró la plantilla de confirmación, se usa esa")
    void usaLaPlantillaConfigurada() {
        when(plantillas.findByDulceMayorIdAndTipoCorreo("sede", TipoCorreo.CONFIRMACION_PEDIDO))
                .thenReturn(Optional.of(ConfiguracionCorreoEmpresa.builder().asunto("Factura {{numero}}")
                        .cuerpoHtml("<p>Gracias {{responsable}}, total {{total}}</p>").build()));

        facade.enviarFactura(factura, PDF, "00000012", "$ 36.800,00");

        assertEquals("Factura 00000012", enviada().asunto());
    }

    @Test
    @DisplayName("Si el responsable no tiene correo cargado, no se envía nada (y no falla)")
    void sinCorreoNoEnvia() {
        responsable.getContactos().clear();

        assertFalse(facade.enviarFactura(factura, PDF, "00000012", "$ 36.800,00"));
        verifyNoInteractions(envio);
    }

    @Test
    @DisplayName("Un error al enviar (ej. el servidor de correo caído) se registra y nunca se propaga")
    void unErrorNoSePropaga() {
        FacturaClienteService facturas = mock(FacturaClienteService.class);
        when(facturas.obtenerEntidad("fac-1")).thenReturn(factura);
        FacturaPdf pdf = mock(FacturaPdf.class);
        when(pdf.generar(factura)).thenThrow(new IllegalStateException("se rompió el PDF"));
        CorreoFacturaService correo = new CorreoFacturaService(facturas, pdf, facade);

        assertFalse(assertDoesNotThrow(() -> correo.enviar("fac-1")));
        verifyNoInteractions(envio);
    }
}
