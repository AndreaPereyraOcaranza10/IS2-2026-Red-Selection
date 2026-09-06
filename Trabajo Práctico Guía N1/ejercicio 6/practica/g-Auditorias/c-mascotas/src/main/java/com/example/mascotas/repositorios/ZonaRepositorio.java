package com.example.mascotas.repositorios;

import com.example.mascotas.entidades.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ZonaRepositorio extends JpaRepository<Zona, String>{
    /*
    * @Query("SELECT c FROM Zona c WHERE c.nombre = :nombre AND c.eliminado = FALSE")
    public Zona buscarZonaPorNombre(@Param("nombre")String id);


    * */

    @Query("SELECT c FROM Zona c ")
    public Collection<Zona> listarZonaActiva();
}
