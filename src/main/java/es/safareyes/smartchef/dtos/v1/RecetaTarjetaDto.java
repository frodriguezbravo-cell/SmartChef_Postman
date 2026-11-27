package es.safareyes.smartchef.dtos.v1;

public record RecetaTarjetaDto(
        Integer id,
        String titulo,
        String slug,
        String imagenUrl,
        String dificultad,
        Integer tiempoTotalMin,
        Short raciones,
        boolean retirada) {
}
