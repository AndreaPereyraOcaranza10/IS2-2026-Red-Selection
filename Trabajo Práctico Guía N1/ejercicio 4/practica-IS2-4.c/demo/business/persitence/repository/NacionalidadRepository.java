package com.example.demo.business.persitence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.business.domain.entity.Nacionalidad;
import com.example.demo.business.domain.entity.Pais;

/*¿Qué es un repositorio?
En el contexto de Spring Data JPA,
un repositorio es un componente que proporciona una abstracción sobre el acceso a la base de datos.
Simplifica las operaciones de CRUD (Crear, Leer, Actualizar y Borrar) y permite definir consultas personalizadas.
Los repositorios son interfaces que extienden de JpaRepository u otras interfaces de Spring Data,
lo que les permite heredar una amplia gama de métodos para interactuar con la base de datos
sin necesidad de implementar esos métodos manualmente.*/

public interface NacionalidadRepository extends JpaRepository<Nacionalidad, String> {

	@Query("SELECT n FROM Nacionalidad n WHERE n.nombre = :nombre AND n.eliminado = FALSE")
	public Nacionalidad buscarNacionalidadPorNombre(@Param("nombre")String nombre);
	
	@Query("SELECT n FROM Nacionalidad n WHERE n.eliminado = FALSE")
	public List<Nacionalidad> listarNacionalidadActivo();
	
}
