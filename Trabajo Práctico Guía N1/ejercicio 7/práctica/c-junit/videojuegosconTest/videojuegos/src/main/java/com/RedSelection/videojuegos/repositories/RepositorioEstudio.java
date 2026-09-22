package com.RedSelection.videojuegos.repositories;

import com.RedSelection.videojuegos.entities.Estudio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioEstudio extends JpaRepository<Estudio, Long> {
}
