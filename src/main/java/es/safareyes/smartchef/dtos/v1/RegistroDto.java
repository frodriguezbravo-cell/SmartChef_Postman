package es.safareyes.smartchef.dtos.v1;

import es.safareyes.smartchef.modelos.MomentoComida;

import java.time.LocalDate;

public record RegistroDto(
        Integer id,
        LocalDate fecha,
        MomentoComida momento,
        RecetaTarjetaDto receta,
        String descripcion,
        Short raciones,
        Integer calorias) {
}
