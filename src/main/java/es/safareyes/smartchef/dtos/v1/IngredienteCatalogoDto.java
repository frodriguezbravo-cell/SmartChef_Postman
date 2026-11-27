package es.safareyes.smartchef.dtos.v1;

import java.math.BigDecimal;

public record IngredienteCatalogoDto(
        Integer id,
        String nombre,
        String slug,
        String categoria,
        BigDecimal caloriasPor100g,
        BigDecimal precioKg,
        boolean aptoVegetariano,
        boolean contieneGluten) {
}
