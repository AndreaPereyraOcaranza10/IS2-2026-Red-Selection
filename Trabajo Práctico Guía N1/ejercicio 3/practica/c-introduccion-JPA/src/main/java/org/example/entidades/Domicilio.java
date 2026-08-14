package org.example.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Domicilio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String calle;
    private String ciudad;
    //Para que la relación sea bidireccional
    //@OneToOne(mappedBy = "domicilio")
    //private Persona persona;

    public Domicilio() {

    }

    public Domicilio(Long id, String calle, String ciudad) {
        this.id = id;
        this.calle = calle;
        this.ciudad = ciudad;
    }

    public Long getId() {
        return id;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
}
