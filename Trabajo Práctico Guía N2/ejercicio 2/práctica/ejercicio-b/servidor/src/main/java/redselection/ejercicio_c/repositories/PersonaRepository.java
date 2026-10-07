package redselection.ejercicio_c.repositories;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import redselection.ejercicio_c.entities.Persona;

import java.util.List;

@Repository
public interface PersonaRepository extends BaseRepository<Persona, Long> {

    //Metodo de query para buscar personas por nombre o apellido
    List<Persona> findByNombreContainingOrApellidoContaining(String nombre, String apellido);
    //boolean existsDni(int dni);

    //Paginada
    //Page<Persona> findByfindByNombreContainingOrApellidoContaining(String nombre, String apellido, Pageable pageable);


    //Misma query de arriba pero con JPQL
    @Query(value = "SELECT p FROM Persona p WHERE p.nombre LIKE %:filtro% OR p.apellido LIKE %:filtro%")
    List<Persona> search(@Param("filtro")String filtro);

    //Paginada:
    @Query(
            value = "SELECT p FROM Persona p WHERE p.nombre LIKE %:filtro% OR p.apellido LIKE %:filtro%",
            countQuery = "SELECT COUNT(p) FROM Persona p WHERE p.nombre LIKE %:filtro% OR p.apellido LIKE %:filtro%"
    )
    Page<Persona> searchPaginado(@Param("filtro")String filtro, Pageable pageable);

    //Misma query pero usando SQL nativo
    @Query(
            value = "SELECT * FROM persona p WHERE p.nombre LIKE CONCAT('%', :filtro, '%') OR p.apellido LIKE CONCAT('%', :filtro, '%')",
            nativeQuery = true
    )
    List<Persona> searchNativo(@Param("filtro")String filtro);

    //Paginada
    /*@Query(
            value = "SELECT * FROM persona p WHERE p.nombre LIKE CONCAT('%', :filtro, '%') OR p.apellido LIKE CONCAT('%', :filtro, '%')",
            countQuery = "SELECT count(*) FROM persona",
            nativeQuery = true

    )
    List<Persona> searchNativo(@Param("filtro")String filtro, Pageable pageable);
    */
}