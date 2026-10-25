
package com.example.mascotas.repositorios;

import com.example.mascotas.entidades.Voto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VotoRespositorio  extends JpaRepository<Voto, String> {
    @Query("SELECT c FROM Voto c WHERE c.mascota1.id = :id ORDER BY c.fecha DESC")
    public List<Voto> buscarVotosPropios(@Param("id") String id); 
    
    @Query("SELECT c FROM Voto c WHERE c.mascota2.id = :id ORDER BY c.fecha DESC")
    public List<Voto> buscarVotosRecibidos(@Param("id") String id);

    @Query("SELECT m.usuario.nombre, m.usuario.apellido, m.nombre, COUNT(v) " +
            "FROM Voto v JOIN v.mascota2 m " +
            "GROUP BY m.usuario.nombre, m.usuario.apellido, m.nombre " +
            "ORDER BY COUNT(v) DESC")
    public List<Object[]> contarVotosPorMascota();
}
