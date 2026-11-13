package redSelection.dulceMayor.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.enums.TipoEmpresa;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BK-05: la factura se genera como PDF válido con la fuente Nunito embebida. Deja una copia en
 * target/facturas-ejemplo/ para poder mirarla (emitida con descuento y anulada).
 */
class FacturaPdfTest {

    @Test
    @DisplayName("Genera un PDF válido con Nunito embebida, con descuento y con el sello de anulada")
    void generaPdf() throws Exception {
        for (EstadoFactura estado : new EstadoFactura[]{EstadoFactura.EMITIDA, EstadoFactura.ANULADA}) {
            byte[] pdf = generar(estado);

            assertTrue(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1).startsWith("%PDF-"));
            assertTrue(new String(pdf, StandardCharsets.ISO_8859_1).contains("Nunito"), "la fuente debe ir embebida");

            Path carpeta = Path.of("target", "facturas-ejemplo");
            Files.createDirectories(carpeta);
            Files.write(carpeta.resolve("factura-" + estado.name().toLowerCase() + ".pdf"), pdf);
        }
    }

    private byte[] generar(EstadoFactura estado) {
        Direccion dirSede = Direccion.builder().calle("Av. España").numeracion("1020").localidadNombre("Ciudad")
                .provinciaNombre("Mendoza").build();
        DulceMayor sede = DulceMayor.builder().razonSocial("Dulce Mayor S.A.").cuit("30-70000000-1")
                .tipoEmpresa(TipoEmpresa.SEDE_CENTRAL).direccion(dirSede).build();
        Direccion dirCliente = Direccion.builder().calle("Las Heras").numeracion("220").localidadNombre("Ciudad")
                .provinciaNombre("Mendoza").build();
        ClienteMinorista cliente = ClienteMinorista.builder().razonSocial("Drugstore 24 hs")
                .nombreComercio("Drugstore 24 hs").cuit("20-30111003-3").direccion(dirCliente).build();
        Preventista preventista = Preventista.builder().nombre("Martín").apellido("Gómez").build();
        Pedido pedido = Pedido.builder().fechaHoraCarga(LocalDateTime.of(2026, 11, 9, 10, 15)).cliente(cliente)
                .preventista(preventista).promocion(Promocion.builder().codigo("ALFAJOR15").build()).build();

        FacturaCliente factura = FacturaCliente.builder().numeroFactura(1L).fechaFactura(LocalDateTime.of(2026, 11, 9, 10, 16))
                .estado(estado).pedido(pedido).subtotal(new BigDecimal("69700.00"))
                .descuento(new BigDecimal("5520.00")).total(new BigDecimal("64180.00")).detalles(new ArrayList<>()).build();
        factura.getDetalles().add(detalle(factura, "Alfajor triple chocolate", 2, "18400.00"));
        factura.getDetalles().add(detalle(factura, "Chicles menta (display)", 1, "32900.00"));

        FacturaClienteService facturas = mock(FacturaClienteService.class);
        when(facturas.obtenerParaDescarga(any(), any())).thenReturn(factura);
        DulceMayorService empresas = mock(DulceMayorService.class);
        when(empresas.sedeCentral()).thenReturn(Optional.of(sede));

        return new FacturaPdf(facturas, empresas).generar("fac-1", mock(UsuarioAutenticado.class));
    }

    private DetalleFactura detalle(FacturaCliente f, String producto, int cantidad, String precio) {
        BigDecimal unitario = new BigDecimal(precio);
        return DetalleFactura.builder().factura(f).producto(Producto.builder().nombre(producto).build())
                .cantidad(cantidad).precioUnitario(unitario)
                .subtotal(unitario.multiply(BigDecimal.valueOf(cantidad))).build();
    }
}
