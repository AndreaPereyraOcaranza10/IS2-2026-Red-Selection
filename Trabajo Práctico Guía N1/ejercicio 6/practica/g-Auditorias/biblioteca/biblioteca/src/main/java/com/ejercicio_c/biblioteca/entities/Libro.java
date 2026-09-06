package com.ejercicio_c.biblioteca.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@SuperBuilder
@Entity
public class Libro extends Auditable {
	
    @Id
    private String id;
    private Long isbn;
    private String titulo;
    private Integer ejemplares;
    private Integer anio;

    @ManyToOne
    private Autor autor;
    @ManyToOne
    private Editorial editorial;
    @OneToOne
    private Imagen imagen;
    private boolean eliminado;

}