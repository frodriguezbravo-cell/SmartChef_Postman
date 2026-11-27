package es.safareyes.smartchef.dtos.v1;

import es.safareyes.smartchef.modelos.RecursoNotificacion;
import es.safareyes.smartchef.modelos.TipoNotificacion;

import java.time.LocalDateTime;

public record NotificacionDto(
        Integer id,
        TipoNotificacion tipo,
        String mensaje,
        RecursoNotificacion recurso,
        Integer recursoId,
        boolean leida,
        LocalDateTime creadaEn) {
}
