package es.safareyes.smartchef.dtos.v1;

import java.math.BigDecimal;

public record LineaRecetaDto(
        Integer idIngrediente,
        String ingrediente,
        BigDecimal cantidad,
        String unidad,
        BigDecimal cantidadGramos,
        boolean opcional,
        String grupo,
        String notas) {
}
