package es.safareyes.smartchef.dtos.v1;

import java.time.LocalDate;
import java.util.List;

public record SemanaDto(LocalDate desde, LocalDate hasta, Long caloriasTotales, List<DiaSemanaDto> dias) {
}
