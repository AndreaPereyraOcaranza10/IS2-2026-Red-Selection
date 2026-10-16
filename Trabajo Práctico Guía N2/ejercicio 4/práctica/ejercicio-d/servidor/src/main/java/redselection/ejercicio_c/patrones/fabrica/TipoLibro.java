package redselection.ejercicio_c.patrones.fabrica;

// Identifica qué fábrica (y por lo tanto qué tipo de libro) hay que usar
public enum TipoLibro {
    FISICO,
    DIGITAL;

    public static TipoLibro desde(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Hay que indicar el tipo de libro (FISICO o DIGITAL).");
        }

        String normalizado = texto.trim().toUpperCase()
                .replace("Í", "I");

        for (TipoLibro tipo : values()) {
            if (tipo.name().equals(normalizado)) {
                return tipo;
            }
        }

        throw new IllegalArgumentException(
                "Tipo de libro inválido: " + texto + ". Los válidos son FISICO y DIGITAL.");
    }
}