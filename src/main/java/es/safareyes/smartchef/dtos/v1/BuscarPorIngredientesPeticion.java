package es.safareyes.smartchef.dtos.v1;

import jakarta.validation.constraints.Size;

import java.util.List;

public record BuscarPorIngredientesPeticion(
        @Size(max = 100) List<Integer> idsIngredientes,
        Boolean vegetariana,
        Boolean sinGluten,
        Boolean rapida,
        Boolean economica) {
}
