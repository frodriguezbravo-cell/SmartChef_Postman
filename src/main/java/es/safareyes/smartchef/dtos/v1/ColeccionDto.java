package es.safareyes.smartchef.dtos.v1;

import java.util.List;

public record ColeccionDto(
        Integer id,
        String nombre,
        boolean favoritas,
        List<RecetaTarjetaDto> recetas) {
}
