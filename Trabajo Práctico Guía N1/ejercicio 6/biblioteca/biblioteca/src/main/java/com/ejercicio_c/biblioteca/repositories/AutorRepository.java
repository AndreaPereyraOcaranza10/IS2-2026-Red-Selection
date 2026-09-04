
package com.ejercicio_c.biblioteca.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ejercicio_c.biblioteca.entities.Autor;


public interface AutorRepository extends JpaRepository<Autor, String> {
    

    @Query ("SELECT a FROM Autor a WHERE a.nombre = :nombre")
    public Autor buscarAutorPorNombre (@Param ("nombre") String nombre) ;
    
    @Query("SELECT a FROM Autor a WHERE "
                    + "CONCAT(a.id, a.nombre)"
                    + "LIKE %?1% ")
    public List<Autor> listarAutorPorFiltro(@Param("filtro") String filtro);
    
    
}
