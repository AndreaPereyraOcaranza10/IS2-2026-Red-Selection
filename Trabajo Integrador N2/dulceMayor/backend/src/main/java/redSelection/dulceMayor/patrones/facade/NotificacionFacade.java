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

    private static final String ASUNTO_POR_DEFECTO = "Tu pedido de Dulce Mayor es el próximo en reparto";
    private static final String CUERPO_POR_DEFECTO =
            "<p>Hola {{responsable}},</p>"
                    + "<p>El reparto de Dulce Mayor está en camino a <b>{{comercio}}</b> ({{direccion}}). "
                    + "Es tu turno: por favor, tené a mano el pago ({{pedido}}).</p>"
                    + "<p>¡Gracias por elegirnos!</p>";

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
