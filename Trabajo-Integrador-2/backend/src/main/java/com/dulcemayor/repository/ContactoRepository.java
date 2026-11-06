package com.dulcemayor.repository;

import com.dulcemayor.model.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactoRepository extends JpaRepository<Contacto, String> {
}

