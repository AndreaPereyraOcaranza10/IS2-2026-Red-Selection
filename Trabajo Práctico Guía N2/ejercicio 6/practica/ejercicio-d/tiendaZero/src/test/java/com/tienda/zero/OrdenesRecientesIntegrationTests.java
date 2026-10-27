package com.tienda.zero;

import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.model.OrdenCompra;
import com.tienda.zero.model.OrdenCompraProveedor;
import com.tienda.zero.repository.OrdenCompraRepository;
import com.tienda.zero.repository.OrdenCompraProveedorRepository;
import com.tienda.zero.repository.UsuarioRepository;
import com.tienda.zero.service.FlujoCompraService;
import com.tienda.zero.service.OrdenCompraProveedorService;
import com.tienda.zero.service.ProveedorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class OrdenesRecientesIntegrationTests {
    @Autowired private OrdenCompraRepository ordenes;
    @Autowired private OrdenCompraProveedorRepository proveedores;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private FlujoCompraService flujo;
    @Autowired private OrdenCompraProveedorService servicioProveedores;
    @Autowired private ProveedorService proveedorService;

    @Test
    void pedidosDelUsuarioYAdministracionOrdenanPorDiaYHoraConCompatibilidadHistorica() {
        var usuario = usuarios.findAll().getFirst();
        LocalDate dia = LocalDate.of(2099, 1, 1);
        OrdenCompra historica = guardarPedido(usuario, dia, null);
        OrdenCompra temprano = guardarPedido(usuario, dia, dia.atTime(9, 0));
        OrdenCompra tarde = guardarPedido(usuario, dia, dia.atTime(18, 0));
        OrdenCompra siguiente = guardarPedido(usuario, dia.plusDays(1), dia.plusDays(1).atTime(8, 0));
        List<String> esperados = List.of(siguiente.getId(), tarde.getId(), temprano.getId(), historica.getId());

        assertEquals(esperados, flujo.listarPedidosUsuario(usuario.getNombreUsuario()).stream()
                .map(OrdenCompra::getId).filter(esperados::contains).toList());
        assertEquals(esperados, flujo.listarPedidosAdministracion().stream()
                .map(OrdenCompra::getId).filter(esperados::contains).toList());
    }

    private OrdenCompra guardarPedido(com.tienda.zero.model.Usuario usuario, LocalDate dia, LocalDateTime hora) {
        return ordenes.saveAndFlush(OrdenCompra.builder().identificadorCompra(UUID.randomUUID().toString())
                .fecha(dia).fechaHoraCreacion(hora).propietario(usuario).direccionEntrega("Direccion test")
                .formaPago(TipoPago.BILLETERA_VIRTUAL).estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_PAGO).build());
    }

    @Test
    void ordenesDeProveedoresYLasPendientesMuestranPrimeroLaMasReciente() {
        var proveedor = proveedorService.listarProveedorActivo().getFirst();
        LocalDate dia = LocalDate.of(2099, 1, 1);
        OrdenCompraProveedor historica = proveedores.saveAndFlush(OrdenCompraProveedor.builder()
                .proveedor(proveedor).fechaCreacion(Date.valueOf(dia)).build());
        OrdenCompraProveedor temprano = proveedores.saveAndFlush(OrdenCompraProveedor.builder()
                .proveedor(proveedor).fechaCreacion(Date.valueOf(dia)).fechaHoraCreacion(dia.atTime(9, 0)).build());
        OrdenCompraProveedor tarde = proveedores.saveAndFlush(OrdenCompraProveedor.builder()
                .proveedor(proveedor).fechaCreacion(Date.valueOf(dia)).fechaHoraCreacion(dia.atTime(18, 0)).build());
        OrdenCompraProveedor siguiente = proveedores.saveAndFlush(OrdenCompraProveedor.builder()
                .proveedor(proveedor).fechaCreacion(Date.valueOf(dia.plusDays(1)))
                .fechaHoraCreacion(dia.plusDays(1).atTime(8, 0)).build());
        List<String> esperados = List.of(siguiente.getId(), tarde.getId(), temprano.getId(), historica.getId());

        assertEquals(esperados, servicioProveedores.listarOrdenes().stream()
                .map(OrdenCompraProveedor::getId).filter(esperados::contains).toList());
        assertEquals(esperados, servicioProveedores.listarOrdenesPendientes().stream()
                .map(OrdenCompraProveedor::getId).filter(esperados::contains).toList());
    }
}
