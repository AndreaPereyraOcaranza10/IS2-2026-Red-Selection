package redselection.ejercicio_c.patrones.estrategia;

// Por qué dato se busca. Cada criterio tiene una estrategia que sabe buscar por él.
public enum CriterioBusqueda {
    TITULO,
    GENERO,
    AUTOR;

    public static CriterioBusqueda desde(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Hay que indicar el criterio de búsqueda (TITULO, GENERO o AUTOR).");
        }

        String normalizado = texto.trim().toUpperCase()
                .replace("Í", "I")
                .replace("É", "E");

        for (CriterioBusqueda criterio : values()) {
            if (criterio.name().equals(normalizado)) {
                return criterio;
            }
        }

        throw new IllegalArgumentException(
                "Criterio de búsqueda inválido: " + texto + ". Los válidos son TITULO, GENERO y AUTOR.");
    }
}