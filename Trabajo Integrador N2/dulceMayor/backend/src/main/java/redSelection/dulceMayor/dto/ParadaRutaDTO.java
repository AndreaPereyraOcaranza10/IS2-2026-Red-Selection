package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.EstadoParada;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ParadaRutaDTO(
        String id,
        int orden,
        String clienteId,
        String comercio,
        String responsable,
        String telefono,
        String direccion,
        Double latitud,
        Double longitud,
        EstadoParada estado,
        String pedidoId,
        LocalDateTime fechaHora,
        /** Km en línea recta desde la parada anterior (la primera, desde el depósito). Null si falta alguna coordenada. */
        Double kmDesdeAnterior,
        /** Total del pedido a entregar (solo reparto). */
        BigDecimal totalPedido
) {}
