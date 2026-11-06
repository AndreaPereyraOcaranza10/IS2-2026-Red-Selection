package com.dulcemayor.repository;

import com.dulcemayor.model.ContactoTelefonico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactoTelefonicoRepository extends JpaRepository<ContactoTelefonico, String> {

    List<ContactoTelefonico> findByEliminadoFalse();
}
