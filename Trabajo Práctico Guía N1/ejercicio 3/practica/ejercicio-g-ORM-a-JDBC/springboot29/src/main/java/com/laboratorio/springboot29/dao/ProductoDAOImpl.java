package com.laboratorio.springboot29.dao;

import com.laboratorio.springboot29.dto.ProductoRequest;
import com.laboratorio.springboot29.dto.ProductoResponse;
import com.laboratorio.springboot29.model.Producto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductoDAOImpl implements ProductoDAO {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ProductoResponse findById(Integer codigo) {

        Producto producto;

        try {
            String sql = "SELECT * FROM productos WHERE codigo = ?";
            producto = this.jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Producto.class), codigo);

        } catch (Exception e) {

            return null;

        }


        if (producto != null) {
            return new ProductoResponse(producto);
        }

        return null;

    }

    @Override
    public List<ProductoResponse> findAll() {
        String sql = "SELECT * FROM productos";

        List<Producto> productos = this.jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Producto.class));

        return productos.stream()
                .map( (Producto p)-> new ProductoResponse(p))
                .toList();
    }

    @Override
    public void save(ProductoRequest request) {
        String sql = "INSERT INTO productos (nombre, precio) VALUES (?,?)";

        this.jdbcTemplate.update(sql, request.getNombre(), request.getPrecio());


    }

    @Override
    public boolean update(Integer codigo, ProductoRequest request) {
        String sql = "UPDATE productos SET nombre = ?, precio = ? WHERE codigo = ?";

        int result = this.jdbcTemplate.update(sql, request.getNombre(), request.getPrecio(), codigo);

        return (result == 1);
    }

    @Override
    public boolean delete(Integer codigo) {
        String sql = "DELETE FROM productos WHERE codigo = ?";

        int result = this.jdbcTemplate.update(sql, codigo);

        return (result == 1);
    }
}
