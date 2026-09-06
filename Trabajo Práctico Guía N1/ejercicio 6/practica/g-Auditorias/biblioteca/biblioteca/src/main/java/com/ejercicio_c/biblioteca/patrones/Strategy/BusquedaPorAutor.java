package com.ejercicio_c.biblioteca.patrones.Strategy;



import com.ejercicio_c.biblioteca.entities.Libro;
import com.ejercicio_c.biblioteca.exceptions.ErrorServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ejercicio_c.biblioteca.services.LibroService;

import java.util.List;

@Component
public class BusquedaPorAutor implements LibroBusquedaStrategy {

    @Autowired
    private LibroService libroService;

    @Override
    public List<Libro> buscar(String idAutor) throws ErrorServiceException {
        return libroService.listarLibroPorAutor(idAutor);
    }
}