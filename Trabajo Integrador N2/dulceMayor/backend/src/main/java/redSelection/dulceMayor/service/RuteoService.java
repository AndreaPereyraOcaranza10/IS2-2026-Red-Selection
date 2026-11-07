package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import redSelection.dulceMayor.dto.TrayectoDTO;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.patrones.adapter.RuteoProvider;
import redSelection.dulceMayor.patrones.strategy.PuntoRuta;

/** "Cómo llegar": el front pide el trayecto al backend, nunca al servicio de ruteo externo. */
@Service
public class RuteoService {

    private final RuteoProvider ruteo;

    public RuteoService(RuteoProvider ruteo) {
        this.ruteo = ruteo;
    }

    public TrayectoDTO trayecto(double desdeLat, double desdeLng, double hastaLat, double hastaLng) {
        validar(desdeLat, desdeLng, "origen");
        validar(hastaLat, hastaLng, "destino");
        return ruteo.trayecto(new PuntoRuta("desde", desdeLat, desdeLng), new PuntoRuta("hasta", hastaLat, hastaLng));
    }

    private void validar(double latitud, double longitud, String punto) {
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            throw new ReglaNegocioException("Las coordenadas del " + punto + " no son válidas.");
        }
    }
}
