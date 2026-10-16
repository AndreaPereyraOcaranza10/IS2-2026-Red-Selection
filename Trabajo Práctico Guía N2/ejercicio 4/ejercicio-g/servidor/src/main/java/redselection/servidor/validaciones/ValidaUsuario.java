package redselection.servidor.validaciones;

import redselection.servidor.model.HistoriaClinica;

public class ValidaUsuario extends ValidadorHistoria {

    @Override
    protected void verificar(HistoriaClinica h) {
        if (h.getUsuario() == null || h.getUsuario().getId() == null)
            throw new IllegalArgumentException("La historia clínica debe tener un usuario");
    }
}
