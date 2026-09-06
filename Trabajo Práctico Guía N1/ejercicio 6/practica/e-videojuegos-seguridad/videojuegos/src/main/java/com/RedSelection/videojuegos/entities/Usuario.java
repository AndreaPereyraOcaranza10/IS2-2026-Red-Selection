package com.RedSelection.videojuegos.entities;

import com.RedSelection.videojuegos.enums.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotEmpty(message = "Debe indicar el nombre")
    private String nombre;

    @Column(unique = true, nullable = false)
    @NotEmpty(message = "Debe indicar el email")
    private String email;

    @Column(nullable = false)
    @NotEmpty(message = "Debe indicar la contraseña")
    private String password;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(nullable = false)
    private boolean eliminado;
}
