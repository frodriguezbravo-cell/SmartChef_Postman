package es.safareyes.smartchef.dtos.v1;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record IngredienteRecetaPeticion(
        @NotNull Integer idIngrediente,
        @NotNull @Positive @Digits(integer = 6, fraction = 2) BigDecimal cantidad,
        @NotBlank String unidad,
        @Positive @Digits(integer = 6, fraction = 2) BigDecimal cantidadGramos,
        Boolean opcional,
        @Size(max = 150) String notas,
        @Size(max = 80) String grupo) {
}
