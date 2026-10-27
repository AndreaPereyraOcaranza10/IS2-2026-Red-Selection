package com.tienda.zero.repository;

import com.tienda.zero.model.FormaDePago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface FormaDePagoRepository extends JpaRepository<FormaDePago, String> {

    List<FormaDePago> findByEliminadoFalse();

}
