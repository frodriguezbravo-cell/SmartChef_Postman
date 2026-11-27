package es.safareyes.smartchef.dtos.v1;

import es.safareyes.smartchef.modelos.MomentoComida;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegistroPeticion(
        LocalDate fecha,
        @NotNull MomentoComida momento,
        Integer idReceta,
        @Size(max = 150) String descripcion,
        @Positive Integer raciones,
        @PositiveOrZero Integer calorias,
        Boolean descontarDespensa) {
}
