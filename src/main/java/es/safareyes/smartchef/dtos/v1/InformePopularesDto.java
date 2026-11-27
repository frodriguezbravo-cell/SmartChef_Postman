package es.safareyes.smartchef.dtos.v1;

import java.time.LocalDate;
import java.util.List;

public record InformePopularesDto(
        LocalDate desde,
        LocalDate hasta,
        List<RecetaPopularDto> masCocinadas,
        List<RecetaPopularDto> masGuardadas) {
}
