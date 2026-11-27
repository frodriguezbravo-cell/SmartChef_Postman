package es.safareyes.smartchef.mapeadores;

import es.safareyes.smartchef.controladores.dto.ComidaDto;
import es.safareyes.smartchef.modelos.PlanComida;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlanComidaMapper {

    @Mapping(target = "tipoComida", source = "tipoComida.nombre")
    @Mapping(target = "idReceta", source = "receta.id")
    @Mapping(target = "descripcion",
            expression = "java(plan.getReceta() != null ? plan.getReceta().getTitulo() : plan.getDescripcionLibre())")
    ComidaDto comida(PlanComida plan);

    List<ComidaDto> comidas(List<PlanComida> planes);
}
