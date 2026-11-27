package es.safareyes.smartchef.dtos.v1;

import java.time.LocalDate;
import java.util.List;

public record DiaSemanaDto(LocalDate fecha, Long calorias, List<RegistroDto> comidas) {
}
