package com.tienda.zero.repository;

import com.tienda.zero.model.ContactoCorreoElectronico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactoCorreoElectronicoRepository extends JpaRepository<ContactoCorreoElectronico, String> {

    List<ContactoCorreoElectronico> findByEliminadoFalse();
}
