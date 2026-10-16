package redselection.servidor.validaciones;

import redselection.servidor.model.DetalleHistoriaClinica;
import redselection.servidor.model.HistoriaClinica;

public class ValidaFechaDetalles extends ValidadorHistoria {

    @Override
    protected void verificar(HistoriaClinica h) {
        for (DetalleHistoriaClinica d : h.getDetalles()) {
            if (d.getFechaHistoria() == null)
                throw new IllegalArgumentException("Cada detalle necesita una fecha");
        }
    }
}