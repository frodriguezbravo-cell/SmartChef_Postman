package es.safareyes.smartchef.dtos.v1;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ListaDesdeRecetaPeticion(
        @NotNull Integer idReceta,
        @NotNull @Min(1) @Max(100) Integer raciones) {
}
