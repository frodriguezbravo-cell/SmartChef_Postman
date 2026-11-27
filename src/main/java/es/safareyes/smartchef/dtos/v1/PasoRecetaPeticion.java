package es.safareyes.smartchef.dtos.v1;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PasoRecetaPeticion(
        @NotNull @Positive @Max(Short.MAX_VALUE) Integer numero,
        @NotBlank String descripcion,
        @PositiveOrZero @Max(Short.MAX_VALUE) Integer duracionMin) {
}
