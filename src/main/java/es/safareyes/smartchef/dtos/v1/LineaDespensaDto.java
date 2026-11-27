package es.safareyes.smartchef.dtos.v1;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LineaDespensaDto(
        Integer idIngrediente,
        String ingrediente,
        BigDecimal cantidad,
        String unidad,
        LocalDate fechaCaducidad,
        Long diasParaCaducar,
        String estado) {
}
