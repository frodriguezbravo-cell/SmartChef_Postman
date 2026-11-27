package es.safareyes.smartchef.dtos.v1;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespensaPeticion(
        @NotNull @PositiveOrZero @Digits(integer = 6, fraction = 2) BigDecimal cantidad,
        String unidad,
        LocalDate fechaCaducidad) {
}
