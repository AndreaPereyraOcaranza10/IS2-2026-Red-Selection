package com.dulcemayor.repository;

import com.dulcemayor.model.VigenciaPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VigenciaPrecioRepository extends JpaRepository<VigenciaPrecio, String> {

    List<VigenciaPrecio> findByEliminadoFalse();

    List<VigenciaPrecio> findByProductoIdAndEliminadoFalse(String idProducto);

    VigenciaPrecio findByProductoIdAndFechaHastaIsNullAndEliminadoFalse(String idProducto);
}
