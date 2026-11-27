package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AnadirRecetaPeticion(
        @NotNull Integer idReceta,
        @NotNull @Positive @Max(100) Integer raciones,
        Integer idDespensa) {
}
