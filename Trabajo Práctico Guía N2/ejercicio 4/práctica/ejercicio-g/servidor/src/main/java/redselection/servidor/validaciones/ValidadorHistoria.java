package redselection.servidor.validaciones;

import redselection.servidor.model.HistoriaClinica;

public abstract class ValidadorHistoria {

    private ValidadorHistoria siguiente;

    // Devuelve el siguiente para poder encadenar: a.setSiguiente(b).setSiguiente(c)
    public ValidadorHistoria setSiguiente(ValidadorHistoria siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    public void validar(HistoriaClinica historia) {
        verificar(historia);                 // mi regla
        if (siguiente != null) {
            siguiente.validar(historia);     // paso al próximo eslabón
        }
    }

    protected abstract void verificar(HistoriaClinica historia);
}