package com.dulcemayor.controller;

import java.util.function.Supplier;

/** Template Method compartido por los controladores REST: valida, ejecuta y normaliza cada respuesta. */
public abstract class ApiControllerTemplate {
    protected final <T> T ejecutarEndpoint(Supplier<T> action) {
        validarSolicitud();
        T response = action.get();
        return normalizarRespuesta(response);
    }
    protected void validarSolicitud() { }
    protected <T> T normalizarRespuesta(T response) { return response; }
}
