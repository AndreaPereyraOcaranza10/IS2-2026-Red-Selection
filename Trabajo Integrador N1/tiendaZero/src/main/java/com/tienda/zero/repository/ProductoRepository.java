package com.tienda.zero.repository;

import com.tienda.zero.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {
    Optional<Producto> findByNombreIgnoreCase(String nombre);

    // Los nombres pueden repetirse (el mismo producto en distintos talles), por eso esta
    // versión devuelve el primero en lugar de fallar con más de un resultado.
    Optional<Producto> findFirstByNombreIgnoreCase(String nombre);

    Optional<Producto> findByCodigoIgnoreCase(String codigo);

    List<Producto> findByEliminadoFalse();

    List<Producto> findByEnOfertaTrueAndEliminadoFalse();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") String id);
}
