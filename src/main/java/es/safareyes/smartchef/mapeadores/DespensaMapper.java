package es.safareyes.smartchef.mapeadores;

import es.safareyes.smartchef.controladores.dto.DespensaDto;
import es.safareyes.smartchef.controladores.dto.ProductoDto;
import es.safareyes.smartchef.dtos.v1.LineaDespensaDto;
import es.safareyes.smartchef.modelos.Despensa;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.servicios.Fechas;
import es.safareyes.smartchef.servicios.MiDespensaService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Mapper(componentModel = "spring", imports = Fechas.class)
public interface DespensaMapper {

    DespensaDto despensa(Despensa despensa);

    List<DespensaDto> despensas(List<Despensa> despensas);

    @Mapping(target = "idIngrediente", source = "ingrediente.id")
    @Mapping(target = "ingrediente", source = "ingrediente.nombre")
    @Mapping(target = "unidad", source = "unidad.nombre")
    @Mapping(target = "ubicacion", source = "ubicacion.nombre")
    @Mapping(target = "motivoBaja", source = "motivoBaja.nombre")
    @Mapping(target = "diasParaCaducar", expression = "java(diasHasta(item.getFechaCaducidad(), Fechas.hoy()))")
    ProductoDto producto(DespensaItem item);

    List<ProductoDto> productos(List<DespensaItem> items);

    @Mapping(target = "idIngrediente", source = "ingrediente.id")
    @Mapping(target = "ingrediente", source = "ingrediente.nombre")
    @Mapping(target = "cantidad", source = "cantidad")
    @Mapping(target = "unidad", source = "unidad.codigo")
    @Mapping(target = "fechaCaducidad", source = "fechaCaducidad")
    @Mapping(target = "diasParaCaducar", expression = "java(diasHasta(item.getFechaCaducidad(), hoy))")
    @Mapping(target = "estado", expression = "java(estado(item.getFechaCaducidad(), hoy))")
    LineaDespensaDto linea(DespensaItem item, @Context LocalDate hoy);

    List<LineaDespensaDto> lineas(List<DespensaItem> items, @Context LocalDate hoy);

    @Named("diasHasta")
    default Long diasHasta(LocalDate fecha, LocalDate hoy) {
        return fecha == null ? null : ChronoUnit.DAYS.between(hoy, fecha);
    }

    @Named("estado")
    default String estado(LocalDate fecha, LocalDate hoy) {
        Long dias = diasHasta(fecha, hoy);
        if (dias == null) {
            return "SIN_CADUCIDAD";
        }
        if (dias < 0) {
            return "CADUCADO";
        }
        if (dias <= MiDespensaService.DIAS_AVISO) {
            return "POR_CADUCAR";
        }
        return "OK";
    }
}
