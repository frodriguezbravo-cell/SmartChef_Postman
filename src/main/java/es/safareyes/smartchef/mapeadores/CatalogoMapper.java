package es.safareyes.smartchef.mapeadores;

import es.safareyes.smartchef.controladores.dto.OpcionCatalogoDto;
import es.safareyes.smartchef.modelos.catalogos.Catalogo;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CatalogoMapper {

    OpcionCatalogoDto opcion(Catalogo catalogo);

    default List<OpcionCatalogoDto> opciones(Collection<? extends Catalogo> catalogos) {
        if (catalogos == null) {
            return List.of();
        }
        return catalogos.stream().map(this::opcion).toList();
    }
}
