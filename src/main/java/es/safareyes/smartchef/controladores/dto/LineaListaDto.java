package es.safareyes.smartchef.controladores.dto;

import java.math.BigDecimal;

public record LineaListaDto(
        Integer id,
        Integer idIngrediente,
        String descripcion,
        BigDecimal cantidad,
        String unidad,
        String receta,
        Boolean comprado) {
}
