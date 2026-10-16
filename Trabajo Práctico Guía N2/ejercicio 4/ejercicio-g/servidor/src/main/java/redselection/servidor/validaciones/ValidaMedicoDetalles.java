package redselection.servidor.validaciones;

import redselection.servidor.model.DetalleHistoriaClinica;
import redselection.servidor.model.HistoriaClinica;

public class ValidaMedicoDetalles extends ValidadorHistoria {

    @Override
    protected void verificar(HistoriaClinica h) {
        for (DetalleHistoriaClinica d : h.getDetalles()) {
            if (d.getMedico() == null || d.getMedico().getId() == null)
                throw new IllegalArgumentException("Cada detalle necesita un médico");
        }
    }
}