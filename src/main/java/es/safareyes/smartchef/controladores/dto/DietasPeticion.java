package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DietasPeticion(
        @NotNull List<@NotBlank String> dietas) {
}
