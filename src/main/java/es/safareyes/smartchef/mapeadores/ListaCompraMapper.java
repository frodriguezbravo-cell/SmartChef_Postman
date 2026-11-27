package es.safareyes.smartchef.mapeadores;

import es.safareyes.smartchef.controladores.dto.LineaListaDto;
import es.safareyes.smartchef.controladores.dto.ListaDetalleDto;
import es.safareyes.smartchef.controladores.dto.ListaDto;
import es.safareyes.smartchef.dtos.v1.LineaCompraDto;
import es.safareyes.smartchef.dtos.v1.ListaCompraDto;
import es.safareyes.smartchef.modelos.ListaCompra;
import es.safareyes.smartchef.modelos.ListaCompraItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ListaCompraMapper {

    @Mapping(target = "idIngrediente", source = "ingrediente.id")
    @Mapping(target = "descripcion", expression = "java(descripcion(linea))")
    @Mapping(target = "unidad", source = "unidad.codigo")
    @Mapping(target = "receta", source = "receta.titulo")
    @Mapping(target = "comprado", expression = "java(Boolean.TRUE.equals(linea.getComprado()))")
    LineaCompraDto lineaCompra(ListaCompraItem linea);

    List<LineaCompraDto> lineasCompra(List<ListaCompraItem> lineas);

    @Mapping(target = "estado", source = "completada", qualifiedByName = "estadoLista")
    @Mapping(target = "lineas", source = "items")
    ListaCompraDto listaCompra(ListaCompra lista);

    @Mapping(target = "idIngrediente", source = "ingrediente.id")
    @Mapping(target = "descripcion", expression = "java(descripcion(linea))")
    @Mapping(target = "unidad", source = "unidad.nombre")
    @Mapping(target = "receta", source = "receta.titulo")
    LineaListaDto linea(ListaCompraItem linea);

    List<LineaListaDto> lineas(List<ListaCompraItem> lineas);

    @Mapping(target = "totalLineas", expression = "java(lista.getItems().size())")
    @Mapping(target = "pendientes", expression = "java(pendientes(lista))")
    ListaDto lista(ListaCompra lista);

    List<ListaDto> listas(List<ListaCompra> listas);

    @Mapping(target = "lineas", source = "items")
    ListaDetalleDto detalle(ListaCompra lista);

    @Named("estadoLista")
    default String estadoLista(Boolean completada) {
        return Boolean.TRUE.equals(completada) ? "CERRADA" : "ABIERTA";
    }

    @Named("descripcion")
    default String descripcion(ListaCompraItem linea) {
        return linea.getIngrediente() != null ? linea.getIngrediente().getNombre() : linea.getDescripcionLibre();
    }

    @Named("pendientes")
    default int pendientes(ListaCompra lista) {
        return (int) lista.getItems().stream().filter(l -> !Boolean.TRUE.equals(l.getComprado())).count();
    }
}
