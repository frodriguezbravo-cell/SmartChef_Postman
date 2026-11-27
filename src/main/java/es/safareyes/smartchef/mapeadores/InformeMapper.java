package es.safareyes.smartchef.mapeadores;

import es.safareyes.smartchef.dtos.GuardadaView;
import es.safareyes.smartchef.dtos.v1.RecetaPopularDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InformeMapper {

    RecetaPopularDto popular(GuardadaView vista);

    List<RecetaPopularDto> populares(List<GuardadaView> vistas);
}
