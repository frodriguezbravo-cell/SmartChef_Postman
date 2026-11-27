package es.safareyes.smartchef.dtos;

import es.safareyes.smartchef.dtos.v1.PreferenciasDto;

import java.math.BigDecimal;

public record CalculosReceta(
        BigDecimal costePorRacion,
        boolean costeCompleto,
        BigDecimal caloriasPorRacion,
        boolean caloriasCompletas,
        PreferenciasDto preferencias) {
}
