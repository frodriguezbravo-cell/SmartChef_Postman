package es.safareyes.smartchef.dtos.v1;

import java.math.BigDecimal;
import java.util.List;

public record CoincidenciaDto(
        RecetaTarjetaDto receta,
        BigDecimal porcentaje,
        int ingredientesTotales,
        int ingredientesPresentes,
        List<String> faltan) {
}
