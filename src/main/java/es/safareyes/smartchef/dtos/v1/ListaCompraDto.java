package es.safareyes.smartchef.dtos.v1;

import java.util.List;

public record ListaCompraDto(
        Integer id,
        String nombre,
        String estado,
        List<LineaCompraDto> lineas) {
}
