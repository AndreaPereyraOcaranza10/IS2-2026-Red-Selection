package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.Presentacion;

import java.math.BigDecimal;

public record ProductoRespuestaDTO(

        String id,
        String codigo,
        String nombre,
        String descripcion,
        String marca,
        Presentacion presentacion,
        int unidadesPorPresentacion,

        String categoria,
        String subCategoria,

        BigDecimal precio,
        int stockActual,
        int stockMinimo,

        boolean enOferta,
        boolean disponible,

        String imagenId
) {}
