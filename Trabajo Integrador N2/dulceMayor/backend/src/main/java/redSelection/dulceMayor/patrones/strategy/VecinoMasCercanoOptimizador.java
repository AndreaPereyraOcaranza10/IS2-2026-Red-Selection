package redSelection.dulceMayor.patrones.strategy;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * PATRÓN: Strategy (comportamiento) - estrategia concreta.
 * Heurística del vecino más cercano + mejora 2-opt sobre distancias en línea recta.
 * No necesita servicios externos: es el orden inicial de una ruta nueva y el respaldo cuando el servicio
 * de ruteo por calles no responde. Sus distancias y tiempos son una estimación (calles = 1,3 x línea recta, 30 km/h).
 */
@Component
public class VecinoMasCercanoOptimizador implements OptimizadorRuta {

    private static final double EPSILON = 1e-9;
    private static final int MAXIMO_PASADAS = 200;
    private static final double FACTOR_CALLES = 1.3;
    private static final double KM_POR_HORA = 30.0;

    @Override
    public PlanOptimizado ordenar(PuntoRuta origen, List<PuntoRuta> pendientes) {
        if (pendientes.isEmpty()) {
            return new PlanOptimizado(List.of(), 0, 0, List.of(), PlanOptimizado.FUENTE_ESTIMADA);
        }

        // camino[0] es siempre el punto fijo de partida; el resto se reordena.
        List<PuntoRuta> restantes = new ArrayList<>(pendientes);
        List<PuntoRuta> camino = new ArrayList<>();
        camino.add(origen != null ? origen : restantes.remove(0));

        while (!restantes.isEmpty()) {
            PuntoRuta actual = camino.get(camino.size() - 1);
            PuntoRuta masCercano = restantes.get(0);
            double mejor = actual.distanciaKm(masCercano);
            for (PuntoRuta candidato : restantes) {
                double distancia = actual.distanciaKm(candidato);
                if (distancia < mejor) {
                    mejor = distancia;
                    masCercano = candidato;
                }
            }
            camino.add(masCercano);
            restantes.remove(masCercano);
        }

        mejorarConDosOpt(camino);

        double km = 0;
        for (int i = 1; i < camino.size(); i++) {
            km += camino.get(i - 1).distanciaKm(camino.get(i));
        }
        km *= FACTOR_CALLES;

        List<String> orden = camino.stream().skip(origen != null ? 1 : 0).map(PuntoRuta::id).toList();
        List<double[]> geometria = camino.stream().map(p -> new double[]{p.latitud(), p.longitud()}).toList();
        int minutos = Math.max(1, (int) Math.round(km / KM_POR_HORA * 60));
        return new PlanOptimizado(orden, km, camino.size() > 1 ? minutos : 0, geometria, PlanOptimizado.FUENTE_ESTIMADA);
    }

    /** Invierte tramos mientras el recorrido se acorte. El primer punto no se mueve. Camino abierto (no vuelve). */
    private void mejorarConDosOpt(List<PuntoRuta> camino) {
        int n = camino.size();
        boolean mejoro = true;
        int pasadas = 0;
        while (mejoro && pasadas++ < MAXIMO_PASADAS) {
            mejoro = false;
            for (int i = 1; i < n - 1; i++) {
                for (int k = i + 1; k < n; k++) {
                    double antes = camino.get(i - 1).distanciaKm(camino.get(i));
                    double despues = camino.get(i - 1).distanciaKm(camino.get(k));
                    if (k + 1 < n) {
                        antes += camino.get(k).distanciaKm(camino.get(k + 1));
                        despues += camino.get(i).distanciaKm(camino.get(k + 1));
                    }
                    if (despues < antes - EPSILON) {
                        invertir(camino, i, k);
                        mejoro = true;
                    }
                }
            }
        }
    }

    private void invertir(List<PuntoRuta> camino, int desde, int hasta) {
        while (desde < hasta) {
            PuntoRuta temporal = camino.get(desde);
            camino.set(desde, camino.get(hasta));
            camino.set(hasta, temporal);
            desde++;
            hasta--;
        }
    }
}
