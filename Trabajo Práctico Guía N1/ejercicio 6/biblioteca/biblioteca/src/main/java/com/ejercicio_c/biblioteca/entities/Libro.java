
package com.ejercicio_c.biblioteca.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
public class Libro {
	
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
