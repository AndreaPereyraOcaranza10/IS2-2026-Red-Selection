package com.example.mascotas.entidades;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class Voto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date respuesta;
    
    @ManyToOne
    private Mascota mascota1; 

    @ManyToOne
    private Mascota mascota2; 

    //Getters y Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(Date respuesta) {
        this.respuesta = respuesta;
    }

    public Mascota getMascota1() {
        return mascota1;
    }

    public void setMascota1(Mascota Mascota1) {
        this.mascota1 = Mascota1;
    }

    public Mascota getMascota2() {
        return mascota2;
    }

    public void setMascota2(Mascota Mascota2) {
        this.mascota2 = Mascota2;
    }
    
    
}