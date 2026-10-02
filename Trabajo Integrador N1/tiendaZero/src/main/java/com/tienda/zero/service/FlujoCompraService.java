package com.tienda.zero.service;

import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.model.OrdenCompra;

import java.util.List;
import java.util.Map;

public interface FlujoCompraService {

    Map<String, Object> verCarrito(String username);

    Map<String, Object> agregarAlCarrito(String username, String productoId, int cantidad);

    Map<String, Object> actualizarCantidadCarrito(String username, String productoId, int cantidad);

    Map<String, Object> quitarDelCarrito(String username, String productoId);

    Map<String, Object> vaciarCarrito(String username);

    List<OrdenCompra> listarPedidosCliente(String username);
    List<OrdenCompra> listarPedidosUsuario(String username);

    List<OrdenCompra> listarPedidosAdministracion();

    List<EstadoOrdenCompra> estadosSiguientes(EstadoOrdenCompra estadoActual);

    List<EstadoOrdenCompra> estadosAdministracion();

    OrdenCompra crearOrdenCliente(String username, String direccion, TipoPago formaPago);
    void guardarDatosMercadoPago(String ordenId, String preferenceId);

    OrdenCompra anularOrdenCliente(String ordenId, String username);

    OrdenCompra cambiarSeguimiento(String ordenId, EstadoOrdenCompra nuevoEstado);

    OrdenCompra cambiarEstadoAdministracion(String ordenId, EstadoOrdenCompra nuevoEstado);
}
