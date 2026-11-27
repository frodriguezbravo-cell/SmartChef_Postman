package es.safareyes.smartchef.mapeadores;

import es.safareyes.smartchef.controladores.dto.AlergiaDto;
import es.safareyes.smartchef.dtos.v1.PreferenciasPeticion;
import es.safareyes.smartchef.dtos.v1.UsuarioDto;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.UsuarioAlergeno;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "codigo", source = "alergeno.codigo")
    @Mapping(target = "nombre", source = "alergeno.nombre")
    @Mapping(target = "gravedad", source = "gravedad.nombre")
    AlergiaDto alergia(UsuarioAlergeno usuarioAlergeno);

    List<AlergiaDto> alergias(List<UsuarioAlergeno> alergias);

    @Mapping(target = "rol", source = "rol.codigo")
    UsuarioDto usuario(Usuario usuario);

    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "prefRapida", source = "rapida")
    @Mapping(target = "prefEconomica", source = "economica")
    void actualizarPreferencias(PreferenciasPeticion peticion, @MappingTarget Usuario usuario);
}
