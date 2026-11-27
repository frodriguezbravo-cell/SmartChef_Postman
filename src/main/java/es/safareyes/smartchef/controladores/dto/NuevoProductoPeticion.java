package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record NuevoProductoPeticion(
        @NotNull Integer idIngrediente,
        @NotNull @Positive BigDecimal cantidad,
        @NotBlank String unidad,
        String ubicacion,
        LocalDate fechaCaducidad) {
}
