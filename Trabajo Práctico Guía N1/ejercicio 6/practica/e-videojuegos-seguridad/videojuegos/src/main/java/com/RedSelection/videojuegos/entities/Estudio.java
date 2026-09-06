package com.RedSelection.videojuegos.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "estudios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Estudio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nombre;
    private boolean activo = true;

    @OneToMany(mappedBy = "estudio")
    private List<Videojuego> videojuegos;
}
