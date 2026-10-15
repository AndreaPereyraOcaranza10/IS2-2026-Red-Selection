package redselection.ejercicio_c.patrones.estrategia;

import java.text.Normalizer;

// Comparación de texto que usan todas las estrategias: sin importar mayúsculas ni tildes
// ("garcia" encuentra "García", "ficcion" encuentra "Ficción").
final class TextoBusqueda {

    private TextoBusqueda() {
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    // true si "texto" contiene "buscado" (ambos ya normalizados o no: se normalizan acá)
    static boolean contiene(String texto, String buscado) {
        return normalizar(texto).contains(normalizar(buscado));
    }
}