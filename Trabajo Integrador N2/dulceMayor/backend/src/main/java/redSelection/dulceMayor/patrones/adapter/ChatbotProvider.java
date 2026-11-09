package redSelection.dulceMayor.patrones.adapter;

import java.util.List;

/** Interfaz del proveedor conversacional; permite sustituir Ollama sin acoplar la lógica del presupuesto. */
public interface ChatbotProvider {

    Propuesta proponer(String consulta, List<ProductoDisponible> productos);

    record ProductoDisponible(String id, String nombre, String marca, String categoria,
                              String subCategoria, String presentacion, int unidadesPorPresentacion,
                              long stock) { }

    record Propuesta(String mensaje, List<ItemPropuesto> items) { }

    record ItemPropuesto(String productoId, int cantidad) { }
}
