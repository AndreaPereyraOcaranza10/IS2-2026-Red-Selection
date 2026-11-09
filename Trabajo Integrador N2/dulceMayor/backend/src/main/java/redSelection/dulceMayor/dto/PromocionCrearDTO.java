package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.*;
import redSelection.dulceMayor.enums.TipoPromocion;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Alta y modificación de una promoción. Según el tipo:
 * VOLUMEN exige cantidadMinima (unidades del pedido); PRODUCTO_DESTACADO exige productoId.
 */
public record PromocionCrearDTO(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9]{3,20}$", message = "El código lleva de 3 a 20 letras o números, sin espacios")
        String codigo,

        @NotNull TipoPromocion tipoPromocion,

        @NotNull
        @DecimalMin(value = "0.01", message = "El descuento tiene que ser mayor a 0")
        @DecimalMax(value = "100", message = "El descuento no puede superar el 100%")
        BigDecimal porcentajeDescuento,

        @Min(value = 1, message = "La cantidad mínima tiene que ser al menos 1")
        Integer cantidadMinima,

        String productoId,

        @NotNull LocalDate fechaDesde,

        LocalDate fechaHasta,     // null = sin fecha de fin

        Boolean activa            // null = activa
) { }
