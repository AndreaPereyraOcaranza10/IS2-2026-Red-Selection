package com.tienda.zero.repository;

import com.tienda.zero.enums.EstadoFactura;
import com.tienda.zero.model.FacturaProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaProveedorRepository extends JpaRepository<FacturaProveedor, String> {

    List<FacturaProveedor> findByEliminadoFalseOrderByFechaFacturaDesc();

    // Evita cargar dos veces la misma factura de un proveedor. Una factura anulada no cuenta,
    // así se puede volver a cargar bien una que se anuló por un error.
    boolean existsByProveedorIdAndNumeroFacturaAndEstadoNotAndEliminadoFalse(
            String idProveedor, long numeroFactura, EstadoFactura estado);

    // Una orden de compra solo puede tener una factura vigente (las anuladas no cuentan)
    boolean existsByOrdenCompraIdAndEstadoNotAndEliminadoFalse(String idOrdenCompra, EstadoFactura estado);
}