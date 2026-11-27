package es.safareyes.smartchef.dtos.v1;

import java.math.BigDecimal;

public record LineaCompraDto(
        Integer id,
        Integer idIngrediente,
        String descripcion,
        BigDecimal cantidad,
        String unidad,
        String receta,
        boolean comprado) {
}
