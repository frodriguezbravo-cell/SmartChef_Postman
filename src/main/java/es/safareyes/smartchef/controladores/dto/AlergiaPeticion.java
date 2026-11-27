package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.Size;

public record AlergiaPeticion(
        String gravedad,
        @Size(max = 255) String notas) {
}
