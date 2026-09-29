package com.tienda.zero.repository;

import com.tienda.zero.enums.EstadoFactura;
import com.tienda.zero.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, String> {

    List<Factura> findByEliminadoFalse();

    List<Factura> findByEstado(EstadoFactura estado);

    Optional<Factura> findByDetallesId(String idDetalleFactura);
}