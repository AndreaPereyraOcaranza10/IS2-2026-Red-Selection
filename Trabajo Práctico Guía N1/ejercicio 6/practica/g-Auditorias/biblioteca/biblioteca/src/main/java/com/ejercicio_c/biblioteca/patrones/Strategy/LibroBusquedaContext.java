package com.ejercicio_c.biblioteca.patrones.Strategy;


import com.ejercicio_c.biblioteca.entities.Libro;
import com.ejercicio_c.biblioteca.exceptions.ErrorServiceException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LibroBusquedaContext {

    private LibroBusquedaStrategy estrategia;

    public void setEstrategia(LibroBusquedaStrategy estrategia) {
        this.estrategia = estrategia;
    }

    public List<Libro> ejecutarBusqueda(String valor) throws ErrorServiceException {
        if (estrategia == null) {
            throw new ErrorServiceException("No se ha definido una estrategia de búsqueda");
        }
        return estrategia.buscar(valor);
    }
}
