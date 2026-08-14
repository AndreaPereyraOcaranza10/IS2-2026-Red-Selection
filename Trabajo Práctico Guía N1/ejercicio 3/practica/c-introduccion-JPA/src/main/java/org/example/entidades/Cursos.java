package org.example.entidades;

import jakarta.persistence.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Cursos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    // ---- Relación BIDIRECCIONAL
    @ManyToMany(mappedBy = "cursos")
    private Set<PersonaMany> inscriptos = new HashSet<>();


    public Cursos() {
    }

    public Cursos(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void agregarInscriptos(PersonaMany persona) {
        if (persona != null) {
            inscriptos.add(persona);
        }
    }

    public void removerInscriptos(PersonaMany persona) {
        if (persona != null) {
            inscriptos.remove(persona);
        }
    }

    public Set<PersonaMany> getInscriptos() {
        return Collections.unmodifiableSet(this.inscriptos);
    }
}
