package com.dulcemayor.repository;

import com.dulcemayor.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, String> {
    List<Pedido> findByClienteIgnoreCaseOrderByFechaCargaDesc(String cliente);
}
