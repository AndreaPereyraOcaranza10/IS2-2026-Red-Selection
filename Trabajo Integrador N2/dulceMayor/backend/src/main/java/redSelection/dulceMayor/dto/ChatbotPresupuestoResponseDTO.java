package redSelection.dulceMayor.dto;

import java.math.BigDecimal;
import java.util.List;

public record ChatbotPresupuestoResponseDTO(String mensaje, List<Linea> items, BigDecimal total,
                                           boolean validado, List<String> observaciones) {
    public record Linea(String productoId, String nombre, String marca, int cantidad,
                        long stockDisponible, BigDecimal precioPorPresentacion,
                        BigDecimal subtotal, boolean valida, String observacion) { }
}
