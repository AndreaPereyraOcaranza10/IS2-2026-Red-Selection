package com.dulcemayor.service;

import java.util.function.Supplier;

/** Template Method para los servicios: ejecuta validaciones alrededor de cada caso de uso. */
public abstract class BusinessServiceTemplate {
    protected final <T> T ejecutarCasoDeUso(Supplier<T> action) {
        validarAntes();
        T result = action.get();
        validarDespues(result);
        return result;
    }
    protected void validarAntes() { }
    protected <T> void validarDespues(T result) { }
}
