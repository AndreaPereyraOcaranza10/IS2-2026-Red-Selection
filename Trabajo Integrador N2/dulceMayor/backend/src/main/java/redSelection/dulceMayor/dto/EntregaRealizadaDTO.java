package redSelection.dulceMayor.dto;

/** Resultado de registrar una entrega: la parada cerrada, la próxima (o null si terminó) y si salió el aviso. */
public record EntregaRealizadaDTO(
        ParadaRutaDTO entregada,
        ParadaRutaDTO siguiente,
        boolean correoEnviado
) {}
