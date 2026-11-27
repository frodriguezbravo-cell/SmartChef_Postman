package es.safareyes.smartchef.mapeadores;

import es.safareyes.smartchef.dtos.v1.ColeccionDto;
import es.safareyes.smartchef.modelos.Coleccion;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = RecetaMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ColeccionMapper {

    @Mapping(target = "favoritas", expression = "java(Boolean.TRUE.equals(coleccion.getEsFavoritas()))")
    @Mapping(target = "recetas", source = "recetas", qualifiedByName = "ordenadasPorTitulo")
    ColeccionDto coleccion(Coleccion coleccion);

    List<ColeccionDto> colecciones(List<Coleccion> colecciones);
}
