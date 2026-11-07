package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.Presentacion;

import java.math.BigDecimal;

public record CatalogoProductoDTO(
        String id,
        String nombre,
        String marca,
        String categoria,
        String subCategoria,
        Presentacion presentacion,
        int unidadesPorPresentacion,
        BigDecimal precio,
        BigDecimal precioPorUnidad,
        long stock,
        int stockMinimo,
        boolean disponible,
        boolean enOferta,
        String descripcion,
        String imagenId
) {}

