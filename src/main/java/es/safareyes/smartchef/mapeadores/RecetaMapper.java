package es.safareyes.smartchef.mapeadores;

import es.safareyes.smartchef.controladores.dto.PasoDto;
import es.safareyes.smartchef.controladores.dto.RecetaDetalleDto;
import es.safareyes.smartchef.controladores.dto.RecetaResumenDto;
import es.safareyes.smartchef.dtos.CalculosReceta;
import es.safareyes.smartchef.dtos.IngredienteEscalado;
import es.safareyes.smartchef.dtos.RecetaTarjetaView;
import es.safareyes.smartchef.dtos.ResumenNutricional;
import es.safareyes.smartchef.dtos.v1.FotoDto;
import es.safareyes.smartchef.dtos.v1.IngredienteRecetaPeticion;
import es.safareyes.smartchef.dtos.v1.LineaRecetaDto;
import es.safareyes.smartchef.dtos.v1.PasoRecetaDto;
import es.safareyes.smartchef.dtos.v1.PasoRecetaPeticion;
import es.safareyes.smartchef.dtos.v1.PreferenciasDto;
import es.safareyes.smartchef.dtos.v1.RecetaCompletaDto;
import es.safareyes.smartchef.dtos.v1.RecetaPeticion;
import es.safareyes.smartchef.dtos.v1.RecetaTarjetaDto;
import es.safareyes.smartchef.modelos.FotoReceta;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.RecetaIngrediente;
import es.safareyes.smartchef.modelos.RecetaPaso;
import es.safareyes.smartchef.modelos.catalogos.Catalogo;
import es.safareyes.smartchef.servicios.calculos.CalculadoraNutricion;
import es.safareyes.smartchef.servicios.calculos.CalculadoraPreferencias;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", imports = CalculadoraPreferencias.class)
public interface RecetaMapper {

    @Mapping(target = "dificultad", source = "dificultad.nombre")
    @Mapping(target = "tiempoTotalMin", expression = "java(CalculadoraPreferencias.tiempoTotal(receta))")
    @Mapping(target = "retirada", expression = "java(Boolean.TRUE.equals(receta.getRetirada()))")
    RecetaTarjetaDto tarjeta(Receta receta);

    @Mapping(target = "dificultad", source = "dificultad.nombre")
    @Mapping(target = "tiempoTotalMin",
            expression = "java(minutos(vista.getTiempoPreparacionMin()) + minutos(vista.getTiempoCoccionMin()))")
    @Mapping(target = "retirada", expression = "java(false)")
    RecetaTarjetaDto tarjeta(RecetaTarjetaView vista);

    List<RecetaTarjetaDto> tarjetas(List<Receta> recetas);

    @Mapping(target = "idIngrediente", source = "ingrediente.id")
    @Mapping(target = "ingrediente", source = "ingrediente.nombre")
    @Mapping(target = "unidad", source = "unidad.codigo")
    @Mapping(target = "opcional", expression = "java(Boolean.TRUE.equals(linea.getEsOpcional()))")
    LineaRecetaDto linea(RecetaIngrediente linea);

    List<LineaRecetaDto> lineas(List<RecetaIngrediente> lineas);

    PasoRecetaDto paso(RecetaPaso paso);

    List<PasoRecetaDto> pasos(List<RecetaPaso> pasos);

    PreferenciasDto preferencias(CalculadoraPreferencias.Preferencias preferencias);

    @Mapping(target = "tipo", source = "tipoContenido")
    @Mapping(target = "tamano", source = "tamanoBytes")
    @Mapping(target = "portada", expression = "java(Boolean.TRUE.equals(foto.getPortada()))")
    @Mapping(target = "url", expression = "java(\"/api/v1/fotos/\" + foto.getId())")
    FotoDto foto(FotoReceta foto);

    List<FotoDto> fotos(List<FotoReceta> fotos);

    @Mapping(target = "id", source = "receta.id")
    @Mapping(target = "titulo", source = "receta.titulo")
    @Mapping(target = "slug", source = "receta.slug")
    @Mapping(target = "descripcion", source = "receta.descripcion")
    @Mapping(target = "imagenUrl", source = "receta.imagenUrl")
    @Mapping(target = "tipoPlato", source = "receta.tipoPlato.nombre")
    @Mapping(target = "tipoCocina", source = "receta.tipoCocina.nombre")
    @Mapping(target = "dificultad", source = "receta.dificultad.nombre")
    @Mapping(target = "raciones", source = "receta.raciones")
    @Mapping(target = "tiempoPreparacionMin", source = "receta.tiempoPreparacionMin")
    @Mapping(target = "tiempoCoccionMin", source = "receta.tiempoCoccionMin")
    @Mapping(target = "tiempoTotalMin", expression = "java(CalculadoraPreferencias.tiempoTotal(receta))")
    @Mapping(target = "autor", source = "receta.creadoPor.username")
    @Mapping(target = "retirada", expression = "java(Boolean.TRUE.equals(receta.getRetirada()))")
    @Mapping(target = "costePorRacion", source = "calculos.costePorRacion")
    @Mapping(target = "costeCompleto", source = "calculos.costeCompleto")
    @Mapping(target = "caloriasPorRacion", source = "calculos.caloriasPorRacion")
    @Mapping(target = "caloriasCompletas", source = "calculos.caloriasCompletas")
    @Mapping(target = "preferencias", source = "calculos.preferencias")
    @Mapping(target = "ingredientes", source = "receta.ingredientes")
    @Mapping(target = "pasos", source = "receta.pasos")
    @Mapping(target = "fotos", source = "receta.fotos")
    RecetaCompletaDto completa(Receta receta, CalculosReceta calculos);

    default RecetaCompletaDto completa(Receta receta) {
        CalculadoraPreferencias.Coste coste = CalculadoraPreferencias.coste(receta);
        ResumenNutricional nutricion = CalculadoraNutricion.porRacion(receta);
        boolean completo = Boolean.TRUE.equals(nutricion.getCompleto());
        boolean hayCalorias = completo || nutricion.getCaloriasKcal().signum() > 0;
        CalculosReceta calculos = new CalculosReceta(coste.porRacion(), coste.completo(),
                hayCalorias ? nutricion.getCaloriasKcal() : null, completo,
                preferencias(CalculadoraPreferencias.calcular(receta)));
        return completa(receta, calculos);
    }

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "titulo", expression = "java(peticion.titulo().trim())")
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "raciones", source = "raciones")
    @Mapping(target = "tiempoPreparacionMin", source = "tiempoPreparacionMin")
    @Mapping(target = "tiempoCoccionMin", source = "tiempoCoccionMin")
    @Mapping(target = "imagenUrl", source = "imagenUrl")
    void actualizar(RecetaPeticion peticion, @MappingTarget Receta receta);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receta", ignore = true)
    @Mapping(target = "ingrediente", ignore = true)
    @Mapping(target = "unidad", ignore = true)
    @Mapping(target = "cantidadGramos", ignore = true)
    @Mapping(target = "orden", ignore = true)
    @Mapping(target = "esOpcional", expression = "java(Boolean.TRUE.equals(peticion.opcional()))")
    RecetaIngrediente aLinea(IngredienteRecetaPeticion peticion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receta", ignore = true)
    @Mapping(target = "imagenUrl", ignore = true)
    @Mapping(target = "descripcion", expression = "java(peticion.descripcion().trim())")
    RecetaPaso aPaso(PasoRecetaPeticion peticion);

    @Mapping(target = "tipoPlato", source = "tipoPlato.nombre")
    @Mapping(target = "tipoCocina", source = "tipoCocina.nombre")
    @Mapping(target = "dificultad", source = "dificultad.nombre")
    @Mapping(target = "tiempoTotalMin",
            expression = "java(minutos(receta.getTiempoPreparacionMin()) + minutos(receta.getTiempoCoccionMin()))")
    @Mapping(target = "dietas", expression = "java(codigos(receta.getDietas()))")
    RecetaResumenDto resumen(Receta receta);

    List<RecetaResumenDto> resumenes(List<Receta> recetas);

    PasoDto pasoDetalle(RecetaPaso paso);

    List<PasoDto> pasosDetalle(List<RecetaPaso> pasos);

    @Mapping(target = "id", source = "receta.id")
    @Mapping(target = "titulo", source = "receta.titulo")
    @Mapping(target = "slug", source = "receta.slug")
    @Mapping(target = "descripcion", source = "receta.descripcion")
    @Mapping(target = "imagenUrl", source = "receta.imagenUrl")
    @Mapping(target = "tipoPlato", source = "receta.tipoPlato.nombre")
    @Mapping(target = "tipoCocina", source = "receta.tipoCocina.nombre")
    @Mapping(target = "dificultad", source = "receta.dificultad.nombre")
    @Mapping(target = "tiempoPreparacionMin", source = "receta.tiempoPreparacionMin")
    @Mapping(target = "tiempoCoccionMin", source = "receta.tiempoCoccionMin")
    @Mapping(target = "racionesReceta", source = "receta.raciones")
    @Mapping(target = "racionesSolicitadas", source = "racionesSolicitadas")
    @Mapping(target = "dietas", expression = "java(codigos(receta.getDietas()))")
    @Mapping(target = "alergenos", expression = "java(alergenos(receta))")
    @Mapping(target = "ingredientes", source = "escalados")
    @Mapping(target = "pasos", source = "receta.pasos")
    @Mapping(target = "nutricion", source = "nutricion")
    RecetaDetalleDto detalle(Receta receta, Integer racionesSolicitadas, List<IngredienteEscalado> escalados,
                             ResumenNutricional nutricion);

    @Named("ordenadasPorTitulo")
    default List<RecetaTarjetaDto> ordenadasPorTitulo(Set<Receta> recetas) {
        if (recetas == null) {
            return List.of();
        }
        return recetas.stream()
                .sorted(Comparator.comparing(Receta::getTitulo, String.CASE_INSENSITIVE_ORDER))
                .map(this::tarjeta)
                .toList();
    }

    @Named("minutos")
    default int minutos(Short valor) {
        return valor == null ? 0 : valor;
    }

    @Named("codigos")
    default List<String> codigos(Collection<? extends Catalogo> catalogos) {
        if (catalogos == null) {
            return List.of();
        }
        return catalogos.stream().map(Catalogo::getCodigo).toList();
    }

    @Named("alergenos")
    default List<String> alergenos(Receta receta) {
        return receta.getIngredientes().stream()
                .flatMap(linea -> linea.getIngrediente().getAlergenos().stream())
                .map(Catalogo::getNombre)
                .distinct()
                .toList();
    }
}
