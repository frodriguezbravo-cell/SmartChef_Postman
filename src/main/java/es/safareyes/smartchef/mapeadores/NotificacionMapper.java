package es.safareyes.smartchef.mapeadores;

import es.safareyes.smartchef.dtos.v1.NotificacionDto;
import es.safareyes.smartchef.modelos.Notificacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificacionMapper {

    @Mapping(target = "leida", expression = "java(Boolean.TRUE.equals(notificacion.getLeida()))")
    NotificacionDto notificacion(Notificacion notificacion);

    List<NotificacionDto> notificaciones(List<Notificacion> notificaciones);
}
