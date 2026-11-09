package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import redSelection.dulceMayor.dto.CatalogoProductoDTO;
import redSelection.dulceMayor.dto.ChatbotPresupuestoResponseDTO;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.patrones.adapter.ChatbotProvider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ChatbotPresupuestoService {

    private final CatalogoService catalogo;
    private final ChatbotProvider chatbot;

    public ChatbotPresupuestoService(CatalogoService catalogo, ChatbotProvider chatbot) {
        this.catalogo = catalogo;
        this.chatbot = chatbot;
    }

    public ChatbotPresupuestoResponseDTO armar(String consulta) {
        List<CatalogoProductoDTO> productos = catalogo.listar(null, null, true);
        List<ChatbotProvider.ProductoDisponible> disponibles = productos.stream()
                .filter(p -> p.disponible() && p.precio() != null && p.stock() > 0)
                .map(p -> new ChatbotProvider.ProductoDisponible(p.id(), p.nombre(), p.marca(), p.categoria(),
                        p.subCategoria(), p.presentacion() == null ? "" : p.presentacion().name(),
                        p.unidadesPorPresentacion(), p.stock()))
                .toList();

        ChatbotProvider.Propuesta propuesta = chatbot.proponer(consulta.trim(), disponibles);
        if (propuesta == null) {
            throw new ReglaNegocioException("No se pudo armar una propuesta. Reformulá tu consulta.");
        }

        Map<String, CatalogoProductoDTO> porId = productos.stream()
                .collect(Collectors.toMap(CatalogoProductoDTO::id, Function.identity()));
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        List<String> observaciones = new ArrayList<>();
        if (propuesta.items() != null) {
            for (ChatbotProvider.ItemPropuesto item : propuesta.items()) {
                if (item == null || item.productoId() == null || item.productoId().isBlank()) {
                    observaciones.add("El asistente sugirió un producto sin identificar; se omitió.");
                    continue;
                }
                if (item.cantidad() <= 0) {
                    observaciones.add("La cantidad sugerida para el producto " + item.productoId()
                            + " no es válida; se omitió.");
                    continue;
                }
                try {
                    cantidades.merge(item.productoId(), item.cantidad(), Math::addExact);
                } catch (ArithmeticException ex) {
                    observaciones.add("La cantidad sugerida para el producto " + item.productoId()
                            + " excede el límite permitido; se omitió.");
                    cantidades.remove(item.productoId());
                }
            }
        }

        List<ChatbotPresupuestoResponseDTO.Linea> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        boolean todasValidas = !cantidades.isEmpty() && observaciones.isEmpty();
        for (Map.Entry<String, Integer> sugerencia : cantidades.entrySet()) {
            String id = sugerencia.getKey();
            int cantidad = sugerencia.getValue();
            CatalogoProductoDTO producto = porId.get(id);
            if (producto == null) {
                String detalle = "El producto sugerido no pertenece al catálogo vigente.";
                lineas.add(new ChatbotPresupuestoResponseDTO.Linea(id, "Producto no reconocido", null,
                        cantidad, 0, null, null, false, detalle));
                observaciones.add(detalle);
                todasValidas = false;
                continue;
            }
            String detalle = null;
            boolean valida = true;
            if (!producto.disponible() || producto.stock() <= 0) {
                detalle = "El producto no tiene stock disponible.";
            } else if (cantidad > producto.stock()) {
                detalle = "Solicitaste " + cantidad + "; hay " + producto.stock() + " disponibles.";
            } else if (producto.precio() == null || producto.precio().signum() <= 0) {
                detalle = "El producto no tiene un precio vigente para cotizar.";
            }
            if (detalle != null) {
                valida = false;
                todasValidas = false;
                observaciones.add(producto.nombre() + ": " + detalle);
            }
            BigDecimal subtotal = valida ? producto.precio().multiply(BigDecimal.valueOf(cantidad)) : null;
            if (subtotal != null) total = total.add(subtotal);
            lineas.add(new ChatbotPresupuestoResponseDTO.Linea(producto.id(), producto.nombre(), producto.marca(),
                    cantidad, producto.stock(), producto.precio(), subtotal, valida, detalle));
        }

        if (lineas.isEmpty()) {
            todasValidas = false;
            if (observaciones.isEmpty()) observaciones.add("No se identificaron productos. Indicá qué productos y cantidades buscás.");
        }
        return new ChatbotPresupuestoResponseDTO(propuesta.mensaje(), List.copyOf(lineas),
                todasValidas ? total : null, todasValidas, List.copyOf(observaciones));
    }
}
