package es.safareyes.smartchef.controladores.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ComidaDto(
        Integer id,
        LocalDate fecha,
        String tipoComida,
        Integer idReceta,
        String descripcion,
        Short raciones,
        LocalTime horaAviso,
        Boolean notificar,
        Boolean completada) {
}
