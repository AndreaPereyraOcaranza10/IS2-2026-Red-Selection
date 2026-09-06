package com.ejercicio_c.biblioteca.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@SuperBuilder
@Entity
public class Autor extends Auditable {

    @Id
    private String id;
    private String nombre;
    private boolean eliminado;

}