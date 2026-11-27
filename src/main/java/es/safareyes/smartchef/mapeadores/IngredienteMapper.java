package es.safareyes.smartchef.mapeadores;

import es.safareyes.smartchef.dtos.v1.IngredienteCatalogoDto;
import es.safareyes.smartchef.modelos.Ingrediente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredienteMapper {

    @Mapping(target = "categoria", source = "categoria.nombre")
    @Mapping(target = "caloriasPor100g", source = "caloriasKcal")
    @Mapping(target = "aptoVegetariano", expression = "java(Boolean.TRUE.equals(ingrediente.getAptoVegetariano()))")
    @Mapping(target = "contieneGluten", expression = "java(contieneGluten(ingrediente))")
    IngredienteCatalogoDto ingrediente(Ingrediente ingrediente);

    List<IngredienteCatalogoDto> ingredientes(List<Ingrediente> ingredientes);

    @Named("contieneGluten")
    default boolean contieneGluten(Ingrediente ingrediente) {
        return ingrediente.getAlergenos().stream().anyMatch(a -> "GLUTEN".equals(a.getCodigo()));
    }
}
