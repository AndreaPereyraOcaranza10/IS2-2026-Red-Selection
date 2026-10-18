package redselection.servidor.validaciones;

import redselection.servidor.model.HistoriaClinica;

public class ValidaPaciente extends ValidadorHistoria {

    @Override
    protected void verificar(HistoriaClinica h) {
        if (h.getPaciente() == null || h.getPaciente().getId() == null)
            throw new IllegalArgumentException("La historia clínica debe tener un paciente");
    }
}