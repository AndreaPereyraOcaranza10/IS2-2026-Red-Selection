package com.ejercicio_c.biblioteca.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ejercicio_c.biblioteca.entities.Libro;

public interface LibroRepository extends JpaRepository<Libro, String>{

	@Query("SELECT l From Libro l WHERE l.titulo = :titulo AND l.eliminado = FALSE")
    public Libro buscarLibroPorTitulo(@Param("titulo") String titulo);

    @Query("SELECT l FROM Libro l WHERE l.autor.id = :idAutor AND l.eliminado = FALSE")
    List<Libro> listarLibroPorAutor(@Param("idAutor") String idAutor);

    @Query("SELECT l FROM Libro l WHERE l.editorial.nombre = :nombreEditorial AND l.eliminado = FALSE")
    List<Libro> listarLibroPorEditorial(@Param("nombreEditorial") String nombreEditorial);

    @Query("SELECT l FROM Libro l WHERE l.isbn= :isbn AND l.eliminado = FALSE")
    public Libro buscarLibroPorIsbn(@Param("isbn") Long isbn);
    
    @Query("SELECT l "
    	 + "  FROM Libro l "
    	 + " WHERE l.autor.id= :idAutor"
    	 + "   AND l.editorial.id = :idEditorial"
    	 + "   AND l.titulo= :titulo"
    	 + "   AND l.eliminado = FALSE")
    public Libro buscarLibroPorTituloAutorEditorial(@Param("titulo") String titulo, @Param("idAutor") String idAutor, @Param("idEditorial") String idEditorial);
    
    @Query("SELECT l "
    	 + "  FROM Libro l "
    	 + " WHERE CONCAT(l.id, l.isbn, l.titulo, l.autor.nombre, l.editorial.nombre)"
         + "  LIKE %?1% " )
    public List<Libro> listarLibroPorFiltro(@Param("filtro") String filtro);

    @Query("SELECT l FROM Libro l WHERE l.anio = :anio AND l.eliminado = FALSE")
    List<Libro> listarLibroPorAnio(@Param("anio") Integer anio);
}
