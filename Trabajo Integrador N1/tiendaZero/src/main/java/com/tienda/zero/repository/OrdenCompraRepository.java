package com.tienda.zero.repository;

import com.tienda.zero.model.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, String> {
    List<OrdenCompra> findByEntregadaFalse();
}