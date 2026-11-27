package es.safareyes.smartchef.dtos.v1;

import java.time.LocalDateTime;

public record FotoDto(
        Integer id,
        String nombreOriginal,
        String tipo,
        Long tamano,
        Short orden,
        boolean portada,
        LocalDateTime subidaEn,
        String url) {
}
