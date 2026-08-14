package org.example.entidades;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "persona")
public class PersonaRelaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;


    /*
    La idea en este tipo de relación es que una persona pueda tener varios domicilios
    pero los domicilios no saben que persona los referencia (unidireccional)
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    JoinColumn(name = "personaId") --> clave foránea queda en la tabla de domicilios con el nombre personaId
    private List<Domicilio> domicilios = new ArrayList();

    */

    @OneToOne(cascade = CascadeType.ALL)
    private Domicilio domicilio;

    public PersonaRelaciones() {

    }

    public PersonaRelaciones(Long id, String name, Domicilio domicilio) {
        this.id = id;
        this.name = name;
        this.domicilio = domicilio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Domicilio getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(Domicilio domicilio) {
        this.domicilio = domicilio;

        /*
        detalle para las relaciones bidireccionales, para mantener esa bidireccionalidad
        if (domicilio != null) {
          domicilio setPersona(this)
        }*/

    }

    //public void addDomicilio(Domicilio domicilio) {
    //    this.domicilio.add(domicilio);
    //}

    //public List<Domicilio> getDomicilios() {
    //    return domicilio;
    //}
}
