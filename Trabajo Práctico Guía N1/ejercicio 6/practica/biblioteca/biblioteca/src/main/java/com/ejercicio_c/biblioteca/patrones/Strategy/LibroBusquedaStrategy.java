package com.ejercicio_c.biblioteca.patrones.Strategy;


import com.ejercicio_c.biblioteca.entities.Libro;
import com.ejercicio_c.biblioteca.exceptions.ErrorServiceException;

import java.util.List;

public interface LibroBusquedaStrategy {
    List<Libro> buscar(String valor) throws ErrorServiceException;
}
