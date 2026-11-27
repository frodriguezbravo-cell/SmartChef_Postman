package es.safareyes.smartchef.mapeadores;

import es.safareyes.smartchef.dtos.v1.RegistroDto;
import es.safareyes.smartchef.modelos.RegistroComida;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = RecetaMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface RegistroMapper {

    @Mapping(target = "receta", source = "receta")
    @Mapping(target = "descripcion", source = "descripcionLibre")
    @Mapping(target = "calorias", source = "caloriasKcal")
    RegistroDto registro(RegistroComida registro);

    List<RegistroDto> registros(List<RegistroComida> registros);
}
