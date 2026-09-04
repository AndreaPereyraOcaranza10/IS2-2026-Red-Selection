package com.ejercicio_c.biblioteca.patrones.Strategy;


import com.ejercicio_c.biblioteca.entities.Libro;
import com.ejercicio_c.biblioteca.exceptions.ErrorServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ejercicio_c.biblioteca.services.LibroService;

import java.util.List;

@Component
public class BusquedaPorAnio implements LibroBusquedaStrategy {

    @Autowired
    private LibroService libroService;

    @Override
    public List<Libro> buscar(String valor) throws ErrorServiceException {
        Integer anio;
        try {
            anio = Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new ErrorServiceException("El año debe ser un número válido");
        }

        return libroService.listarLibroPorAnio(anio);
    }
}