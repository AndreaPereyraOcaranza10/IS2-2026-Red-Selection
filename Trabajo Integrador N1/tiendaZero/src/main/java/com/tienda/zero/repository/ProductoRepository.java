package com.tienda.zero.repository;

import com.tienda.zero.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {
    Optional<Producto> findByNombreIgnoreCase(String nombre);
    Optional<Producto> findByCodigoIgnoreCase(String codigo);
    List<Producto> findByEliminadoFalse();
}