
package com.ejercicio_c.biblioteca.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ejercicio_c.biblioteca.entities.Editorial;


public interface EditorialRepository extends JpaRepository<Editorial, String> {
    
    @Query ("SELECT e FROM Editorial e WHERE e.nombre = :nombre")
    public Editorial buscarEditorialPorNombre (@Param ("nombre") String nombre) ;
   
    @Query("SELECT e FROM Editorial e WHERE "
                    + "CONCAT(e.id, e.nombre)"
                    + "LIKE %?1% ")
    public List<Editorial> listarEditorialPorFiltro(@Param("filtro") String filtro);

}
