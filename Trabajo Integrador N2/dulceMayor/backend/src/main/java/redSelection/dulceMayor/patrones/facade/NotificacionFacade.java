package redSelection.dulceMayor.patrones.facade;

import org.springframework.stereotype.Component;
import redSelection.dulceMayor.enums.TipoCorreo;
import redSelection.dulceMayor.enums.TipoEmpresa;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.patrones.adapter.EnvioCorreo;
import redSelection.dulceMayor.patrones.builder.Notificacion;
import redSelection.dulceMayor.repository.ConfiguracionCorreoEmpresaRepository;
import redSelection.dulceMayor.repository.DulceMayorRepository;

import java.util.Optional;

/**
 * PATRÓN: Facade (estructural).
 * Una sola puerta para los avisos: esconde la búsqueda del correo principal del responsable, la plantilla
 * configurada por la empresa, el armado con el Builder y el envío por el Adapter.
 * Los services piden "avisá la próxima entrega" y no conocen nada de eso.
 * Debe llamarse dentro de una transacción (lee relaciones LAZY).
 */
@Component
public class NotificacionFacade {

    public static final String ASUNTO_POR_DEFECTO = "Tu pedido de Dulce Mayor es el próximo en reparto";
    public static final String CUERPO_POR_DEFECTO =
            "<p>Hola {{responsable}},</p>"
                    + "<p>El reparto de Dulce Mayor está en camino a <b>{{comercio}}</b> ({{direccion}}). "
                    + "Es tu turno: por favor, tené a mano el pago ({{pedido}}).</p>"
                    + "<p>¡Gracias por elegirnos!</p>";

    /** BK-08: confirmación del pedido al facturarse, con la factura en PDF adjunta. */
    public static final String ASUNTO_FACTURA = "Dulce Mayor · Tu pedido está confirmado (Factura N° {{numero}})";
    public static final String CUERPO_FACTURA =
            "<p>Hola {{responsable}},</p>"
                    + "<p>¡Tu pedido para <b>{{comercio}}</b> está confirmado! Ya lo facturamos y lo estamos preparando "
                    + "para el reparto.</p>"
                    + "<p><b>Factura N° {{numero}}</b> · Total: <b>{{total}}</b><br>"
                    + "Productos: {{productos}}</p>"
                    + "<p>Te adjuntamos la factura en PDF. El área de facturación se va a comunicar con vos para "
                    + "coordinar el pago.</p>"
                    + "<p>¡Gracias por elegirnos!<br>Dulce Mayor</p>";
    public static final String ASUNTO_NEWSLETTER = "Ofertas de Dulce Mayor · {{fecha}}";
    public static final String CUERPO_NEWSLETTER =
            "<html><body style='font-family:Arial,sans-serif;color:#261923'>"
                    + "<h2>Ofertas de Dulce Mayor</h2><p>Hola {{responsable}},</p>"
                    + "<p>Estas son las ofertas disponibles con stock al {{fecha}}:</p>"
                    + "<ul>{{productos}}</ul><p>Consultá el catálogo para realizar tu pedido.</p>"
                    + "<p>Dulce Mayor</p></body></html>";
    private static final String ASUNTO_ACCESO = "Tu acceso a Dulce Mayor";
    private static final String CUERPO_ACCESO =
            "<p>Hola {{nombre}},</p>"
                    + "<p>Ya podés ingresar a Dulce Mayor para ver tus pedidos y facturas.</p>"
                    + "<p>Usuario: <b>{{usuario}}</b><br>Clave temporal: <b>{{clave}}</b></p>"
                    + "<p>Por seguridad, al ingresar por primera vez vas a tener que elegir una clave nueva. "
                    + "Si no esperabas este correo, ignoralo.</p>";

    private final DulceMayorRepository dulceMayorRepository;
    private final ConfiguracionCorreoEmpresaRepository configuracionRepository;
    private final EnvioCorreo envioCorreo;

    public NotificacionFacade(DulceMayorRepository dulceMayorRepository,
                              ConfiguracionCorreoEmpresaRepository configuracionRepository,
                              EnvioCorreo envioCorreo) {
        this.dulceMayorRepository = dulceMayorRepository;
        this.configuracionRepository = configuracionRepository;
        this.envioCorreo = envioCorreo;
    }

    /** Avisa al comercio de la parada que es el próximo en la ruta de reparto. @return true si el correo salió. */
    public boolean avisarProximaEntrega(ParadaRuta parada) {
        ClienteMinorista cliente = parada.getCliente();
        Optional<String> correo = correoPrincipal(cliente.getResponsable());
        if (correo.isEmpty()) {
            return false;
        }
        Optional<ConfiguracionCorreoEmpresa> plantilla = sedeCentral()
                .flatMap(sede -> configuracionRepository
                        .findByDulceMayorIdAndTipoCorreo(sede.getId(), TipoCorreo.PROXIMA_ENTREGA));

        Direccion direccion = cliente.getDireccion();
        Notificacion notificacion = Notificacion.nueva()
                .para(correo.get())
                .asunto(plantilla.map(ConfiguracionCorreoEmpresa::getAsunto).orElse(ASUNTO_POR_DEFECTO))
                .plantilla(plantilla.map(ConfiguracionCorreoEmpresa::getCuerpoHtml).orElse(CUERPO_POR_DEFECTO))
                .valor("responsable", cliente.getResponsable().getNombre() + " " + cliente.getResponsable().getApellido())
                .valor("comercio", cliente.getNombreComercio())
                .valor("direccion", direccion.getCalle() + " " + direccion.getNumeracion() + ", " + direccion.getLocalidadNombre())
                .valor("pedido", parada.getPedido() != null ? "pedido " + parada.getPedido().getId() : "tu pedido")
                .construir();
        return envioCorreo.enviar(notificacion);
    }

    /**
     * BK-08: confirma al responsable del comercio que su pedido se facturó, con la factura en PDF adjunta.
     * Usa la plantilla CONFIRMACION_PEDIDO si administración la configuró. @return true si el correo salió;
     * false si el responsable no tiene correo cargado o el envío falló (nunca lanza: la factura ya está emitida).
     */
    public boolean enviarFactura(FacturaCliente factura, byte[] pdf, String numero, String total) {
        ClienteMinorista cliente = factura.getPedido().getCliente();
        ResponsableComercio responsable = cliente.getResponsable();
        Optional<String> correo = responsable == null ? Optional.empty() : correoPrincipal(responsable);
        if (correo.isEmpty()) {
            return false;
        }
        Optional<ConfiguracionCorreoEmpresa> plantilla = sedeCentral()
                .flatMap(sede -> configuracionRepository
                        .findByDulceMayorIdAndTipoCorreo(sede.getId(), TipoCorreo.CONFIRMACION_PEDIDO))
                .filter(c -> !c.isEliminado());
        String productos = factura.getDetalles().stream()
                .map(d -> d.getCantidad() + " × " + d.getProducto().getNombre())
                .collect(java.util.stream.Collectors.joining(", "));
        Notificacion notificacion = Notificacion.nueva()
                .para(correo.get())
                .asunto(plantilla.map(ConfiguracionCorreoEmpresa::getAsunto).orElse(ASUNTO_FACTURA))
                .plantilla(plantilla.map(ConfiguracionCorreoEmpresa::getCuerpoHtml).orElse(CUERPO_FACTURA))
                .valor("responsable", responsable.getNombre() + " " + responsable.getApellido())
                .valor("comercio", cliente.getNombreComercio())
                .valor("numero", numero)
                .valor("total", total)
                .valor("productos", productos)
                .adjunto("Factura-" + numero + ".pdf", "application/pdf", pdf)
                .construir();
        return envioCorreo.enviar(notificacion);
    }

    /** BK-12: envía la plantilla newsletter (personalizada o predeterminada) al responsable de un comercio. */
    public boolean enviarNewsletter(String correo, String responsable, String fecha, String productosHtml) {
        Optional<ConfiguracionCorreoEmpresa> plantilla = sedeCentral()
                .flatMap(sede -> configuracionRepository
                        .findByDulceMayorIdAndTipoCorreo(sede.getId(), TipoCorreo.NEWSLETTER))
                .filter(c -> !c.isEliminado());
        Notificacion notificacion = Notificacion.nueva()
                .para(correo)
                .asunto(plantilla.map(ConfiguracionCorreoEmpresa::getAsunto).orElse(ASUNTO_NEWSLETTER))
                .plantilla(plantilla.map(ConfiguracionCorreoEmpresa::getCuerpoHtml).orElse(CUERPO_NEWSLETTER))
                .valor("responsable", responsable)
                .valor("fecha", fecha)
                .valorHtmlSeguro("productos", productosHtml)
                .construir();
        return envioCorreo.enviar(notificacion);
    }

    /**
     * Manda la clave temporal de un cliente nuevo. ...
     */
    public boolean enviarClaveTemporal(String correo, String nombre, String usuario, String claveTemporal) {
        Notificacion notificacion = Notificacion.nueva()
                .para(correo)
                .asunto(ASUNTO_ACCESO)
                .plantilla(CUERPO_ACCESO)
                .valor("nombre", nombre)
                .valor("usuario", usuario)
                .valor("clave", claveTemporal)
                .construir();
        return envioCorreo.enviar(notificacion);
    }

    private Optional<String> correoPrincipal(Persona persona) {
        Optional<ContactoCorreoElectronico> correos = persona.getContactos().stream()
                .filter(c -> c instanceof ContactoCorreoElectronico && !c.isEliminado())
                .map(c -> (ContactoCorreoElectronico) c)
                .sorted((a, b) -> Boolean.compare(b.isPrincipal(), a.isPrincipal()))
                .findFirst();
        return correos.map(ContactoCorreoElectronico::getEmail);
    }

    private Optional<DulceMayor> sedeCentral() {
        return dulceMayorRepository.findByEliminadoFalse().stream()
                .filter(d -> d.getTipoEmpresa() == TipoEmpresa.SEDE_CENTRAL)
                .findFirst();
    }
}
