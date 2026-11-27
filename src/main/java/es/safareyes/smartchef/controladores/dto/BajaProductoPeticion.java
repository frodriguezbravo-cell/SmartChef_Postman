package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.NotBlank;

public record BajaProductoPeticion(
        @NotBlank String motivo) {
}
