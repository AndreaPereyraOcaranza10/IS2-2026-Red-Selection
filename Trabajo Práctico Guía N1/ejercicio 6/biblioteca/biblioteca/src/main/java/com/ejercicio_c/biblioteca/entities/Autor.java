package com.ejercicio_c.biblioteca.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
public class Autor {

    @Id
    private String id;
    private String nombre;
    private boolean eliminado;

}
