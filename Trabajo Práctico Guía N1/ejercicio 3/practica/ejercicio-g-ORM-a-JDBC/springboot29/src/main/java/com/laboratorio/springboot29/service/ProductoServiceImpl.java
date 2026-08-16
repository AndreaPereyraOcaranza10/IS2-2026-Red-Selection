package com.laboratorio.springboot29.service;

import com.laboratorio.springboot29.dao.ProductoDAO;
import com.laboratorio.springboot29.dto.ProductoRequest;
import com.laboratorio.springboot29.dto.ProductoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {
    private final ProductoDAO productoDAO;

    @Override
    public Optional<ProductoResponse> findById(Integer codigo) {
        ProductoResponse response = this.productoDAO.findById(codigo);
        return response == null ? Optional.empty() :  Optional.of(response);
    }

    @Override
    public List<ProductoResponse> findAll() {
        return this.productoDAO.findAll();
    }

    @Override
    @Transactional
    public void save(ProductoRequest request) {
        this.productoDAO.save(request);
    }

    @Override
    @Transactional
    public boolean update(Integer codigo, ProductoRequest request) {
        return this.productoDAO.update(codigo, request);
    }

    @Override
    @Transactional
    public boolean delete(Integer codigo) {
        return this.productoDAO.delete(codigo);
    }
}