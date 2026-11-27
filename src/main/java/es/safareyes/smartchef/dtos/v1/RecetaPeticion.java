package es.safareyes.smartchef.dtos.v1;

import es.safareyes.smartchef.validacion.PasosConsecutivos;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RecetaPeticion(
        @NotBlank @Size(max = 200) String titulo,
        String descripcion,
        @NotBlank String tipoPlato,
        String tipoCocina,
        @NotBlank String dificultad,
        @NotNull @Min(1) @Max(12) Integer raciones,
        @NotNull @Positive @Max(Short.MAX_VALUE) Integer tiempoPreparacionMin,
        @PositiveOrZero @Max(Short.MAX_VALUE) Integer tiempoCoccionMin,
        @Size(max = 500) String imagenUrl,
        @NotNull @Size(min = 1, max = 30) @Valid List<IngredienteRecetaPeticion> ingredientes,
        @NotNull @Size(min = 1) @PasosConsecutivos @Valid List<PasoRecetaPeticion> pasos) {
}
