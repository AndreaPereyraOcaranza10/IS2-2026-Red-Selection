package com.ejercicio_c.biblioteca.entities;

import com.ejercicio_c.biblioteca.enums.Rol;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter

public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique=true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @OneToOne
    private Imagen imagen;

    @Column(nullable = false)
    private Boolean eliminado;

    public boolean isEliminado() {
        return eliminado;
    }
}
