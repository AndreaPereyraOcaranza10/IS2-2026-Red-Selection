package com.tienda.zero.service;

import com.tienda.zero.dto.DatosFacturaCliente;
import com.tienda.zero.enums.EstadoFactura;
import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.model.*;
import com.tienda.zero.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.AddressException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacturaClienteService {
    private final FacturaClienteRepository facturas;
    private final NumeracionFacturaClienteRepository numeracion;
    private final OrdenCompraRepository ordenes;
    private final PersonaRepository personas;
    private final FormaDePagoRepository formasPago;
    private final ApplicationEventPublisher eventos;

    public boolean requiereFormulario(OrdenCompra orden) {
        if (orden.getPropietario() == null) return false;
        TipoUsuario rol = orden.getPropietario().getRol();
        return rol == TipoUsuario.ADMINISTRATIVO || rol == TipoUsuario.JEFE;
    }

    @Transactional(readOnly = true)
    public DatosFacturaCliente datosSugeridos(OrdenCompra orden) {
        DatosFacturaCliente datos = new DatosFacturaCliente();
        datos.setDomicilio(orden.getDireccionEntrega());
        if (orden.getPropietario() != null) {
            datos.setCorreo(orden.getPropietario().getNombreUsuario());
            personas.findByUsuarioId(orden.getPropietario().getId()).ifPresent(persona -> {
                datos.setNombre(persona.getNombre());
                datos.setApellido(persona.getApellido());
                datos.setDocumento(persona.getNumeroDocumento());
            });
        }
        return datos;
    }

    @Transactional
    public FacturaCliente emitirAutomatica(OrdenCompra orden) {
        return emitir(orden.getId(), datosSugeridos(orden));
    }

    @Transactional
    public FacturaCliente emitir(String ordenId, DatosFacturaCliente datos) {
        OrdenCompra orden = ordenes.buscarParaFacturar(ordenId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        if (orden.isEliminado() || orden.getEstadoOrdenCompra() == EstadoOrdenCompra.PENDIENTE_COMPLETAR
                || orden.getEstadoOrdenCompra() == EstadoOrdenCompra.ANULADA) {
            throw new IllegalArgumentException("La orden no puede facturarse");
        }
        var existente = facturas.findByOrdenCompraId(ordenId);
        if (existente.isPresent()) return existente.get();
        String nombre = requerido(datos.getNombre(), "nombre", 100);
        String apellido = requerido(datos.getApellido(), "apellido", 100);
        String domicilio = requerido(datos.getDomicilio(), "domicilio", 250);
        String documento = opcional(datos.getDocumento(), 50);
        String correo = requerido(requiereFormulario(orden) ? datos.getCorreo()
                : orden.getPropietario().getNombreUsuario(), "correo", 150);
        validarCorreo(correo);
        var detalles = orden.getDetalles().stream().filter(d -> !d.isEliminado()).toList();
        if (detalles.isEmpty()) throw new IllegalArgumentException("La orden no tiene productos");

        FormaDePago formaPago = formasPago.findByEliminadoFalse().stream()
                .filter(f -> f.getTipoPago() == orden.getFormaPago()).findFirst().orElseGet(() ->
                        formasPago.save(FormaDePago.builder().tipoPago(orden.getFormaPago()).build()));
        FacturaCliente factura = new FacturaCliente();
        // IDENTITY asigna numeros distintos incluso entre compras simultaneas.
        factura.setNumeroFactura(numeracion.saveAndFlush(new NumeracionFacturaCliente()).getId());
        factura.setFechaFactura(Date.valueOf(LocalDate.now()));
        factura.setOrdenCompra(orden);
        factura.setNombreCliente(nombre);
        factura.setApellidoCliente(apellido);
        factura.setDocumentoCliente(documento);
        factura.setDomicilioCliente(domicilio);
        factura.setCorreoCliente(correo);
        factura.setFormaDePago(formaPago);
        factura.setEstado(estadoFactura(orden));
        factura.setTotalPagado(orden.getTotal().doubleValue());
        for (DetalleCompra detalle : detalles) {
            factura.getDetalles().add(DetalleFactura.builder().producto(detalle.getProducto())
                    .nombreProducto(detalle.getProducto().getNombre()).codigoProducto(detalle.getProducto().getCodigo())
                    .cantidad(detalle.getCantidad()).subtotal(detalle.getSubtotal().doubleValue()).build());
        }
        // El stock ya fue reservado por la orden; emitir la factura no lo descuenta otra vez.
        FacturaCliente guardada = facturas.save(factura);
        programarCorreo(guardada);
        return guardada;
    }

    @Transactional
    public void actualizarEstado(OrdenCompra orden) {
        facturas.buscarParaActualizar(orden.getId()).ifPresent(f -> {
            EstadoFactura anterior = f.getEstado();
            f.setEstado(estadoFactura(orden));
            if (f.getEstado() == EstadoFactura.ANULADA) f.setCorreoPendiente(false);
            if (anterior != EstadoFactura.PAGADA && f.getEstado() == EstadoFactura.PAGADA) programarCorreo(f);
        });
    }

    private void programarCorreo(FacturaCliente factura) {
        boolean lista = factura.getEstado() == EstadoFactura.PAGADA
                || factura.getOrdenCompra().getFormaPago() != TipoPago.BILLETERA_VIRTUAL;
        if (!lista || factura.getEstado() == EstadoFactura.ANULADA
                || factura.isCorreoEnviado() || factura.isCorreoPendiente()) return;
        factura.setCorreoPendiente(true);
        eventos.publishEvent(new FacturaCorreoPendiente(factura.getId()));
    }

    private void validarCorreo(String correo) {
        try {
            InternetAddress direccion = new InternetAddress(correo, true);
            direccion.validate();
            if (!correo.contains("@") || !correo.equals(direccion.getAddress())) {
                throw new AddressException();
            }
        } catch (AddressException e) {
            throw new IllegalArgumentException("El correo de la factura no es valido");
        }
    }

    private EstadoFactura estadoFactura(OrdenCompra orden) {
        return switch (orden.getEstadoOrdenCompra()) {
            case ANULADA -> EstadoFactura.ANULADA;
            case ENTREGADO, PENDIENTE_ENTREGA, PENDIENTE_ENVIO -> EstadoFactura.PAGADA;
            default -> EstadoFactura.SIN_DEFINIR;
        };
    }

    @Transactional(readOnly = true)
    public OrdenCompra ordenDelUsuario(String ordenId, String username) {
        return ordenes.findByIdAndPropietarioNombreUsuario(ordenId, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public FacturaCliente buscar(String id, String username, boolean administrador) {
        FacturaCliente factura = facturas.findById(id).filter(f -> !f.isEliminado())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!administrador && (factura.getOrdenCompra().getPropietario() == null
                || !username.equals(factura.getOrdenCompra().getPropietario().getNombreUsuario()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return factura;
    }

    @Transactional(readOnly = true)
    public Map<String, FacturaCliente> delUsuario(String username) {
        return facturas.findByOrdenCompraPropietarioNombreUsuarioAndEliminadoFalseOrderByFechaFacturaDescNumeroFacturaDesc(username)
                .stream().collect(Collectors.toMap(f -> f.getOrdenCompra().getId(), f -> f, (primera, siguiente) -> primera));
    }

    public List<FacturaCliente> listar() {
        return facturas.findByEliminadoFalseOrderByFechaFacturaDescNumeroFacturaDesc();
    }

    private String requerido(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("El " + campo + " es obligatorio");
        return opcional(valor, maximo);
    }

    private String opcional(String valor, int maximo) {
        if (valor == null) return "";
        String limpio = valor.trim();
        if (limpio.length() > maximo) throw new IllegalArgumentException("Un dato de la factura supera el largo permitido");
        return limpio;
    }
}
