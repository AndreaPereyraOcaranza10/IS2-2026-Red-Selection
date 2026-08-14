package org.example.entidades;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class PersonaMany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToMany
    @JoinTable(
            name = "persona_curso", //tabla intermedia
            joinColumns = @JoinColumn(name = "persona_id"), //FK a persona
            inverseJoinColumns = @JoinColumn(name = "curso_id") //FK a curso
    )

    private Set<Cursos> cursos = new HashSet<>();

    public PersonaMany () {
    }

    public PersonaMany(String name) {
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

    public Set<Cursos> getCursos() {
        return cursos;
    }

    public void setCursos(Set<Cursos> cursos) {
        this.cursos = cursos;
    }

    /*

    Esto es en caso de que la relación sea unidireccional
    public void agregarCurso(Cursos c1) {
        if (c1 != null) {
            this.cursos.add(c1);
        }
    }
    */

    //caso relación bidireccional, agregamos el helper
    public void agregarCurso(Cursos c1) {
        if (c1 != null) {
            this.cursos.add(c1);
            c1.agregarInscriptos(this);
        }
    }

    @Override
    public String toString() {
        return "Persona { " +
                "id = "+ id +
                ", nombre = " + name + '\'' +
                ", cursos = " + cursos +
                "}";
    }

}
