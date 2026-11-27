package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.RecetaTarjetaView;
import es.safareyes.smartchef.dtos.CoincidenciaView;
import es.safareyes.smartchef.dtos.v1.BuscarPorIngredientesPeticion;
import es.safareyes.smartchef.dtos.v1.CoincidenciaDto;
import es.safareyes.smartchef.dtos.v1.PaginaDto;
import es.safareyes.smartchef.dtos.v1.RecetaCompletaDto;
import es.safareyes.smartchef.dtos.v1.RecetaTarjetaDto;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.mapeadores.RecetaMapper;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.RecetaIngrediente;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.repositorios.IDespensaItemRepository;
import es.safareyes.smartchef.repositorios.IDificultadRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.seguridad.Roles;
import es.safareyes.smartchef.servicios.calculos.CalculadoraPreferencias;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecetaCatalogoService {

    private static final Map<String, String> ORDEN = Map.of(
            "titulo", "titulo",
            "fecha", "creadoEn",
            "raciones", "raciones",
            "dificultad", "dificultad.orden",
            "preparacion", "tiempoPreparacionMin");

    private final IRecetaRepository recetaRepository;
    private final IDificultadRepository dificultadRepository;
    private final IDespensaItemRepository despensaItemRepository;
    private final IdentidadService identidad;
    private final RecetaMapper recetaMapper;

    public record Filtro(String texto, String dificultad, Integer tiempoMaximo,
                         Boolean vegetariana, Boolean sinGluten, Boolean rapida, Boolean economica) {
    }

    public PaginaDto<RecetaTarjetaDto> catalogo(Integer idUsuario, Filtro filtro,
                                                Integer pagina, Integer tamano, String orden) {
        PageRequest pageable = Paginas.de(pagina, tamano, orden, ORDEN, "titulo");
        Map<String, Object> aplicados = new LinkedHashMap<>();

        Specification<Receta> spec = RecetaSpecs.visibles().and(RecetaSpecs.conPortada());
        if (filtro.texto() != null && !filtro.texto().isBlank()) {
            spec = spec.and(RecetaSpecs.tituloContiene(filtro.texto()));
            aplicados.put("texto", filtro.texto().trim());
        }
        if (filtro.dificultad() != null && !filtro.dificultad().isBlank()) {
            String codigo = filtro.dificultad().trim().toUpperCase(Locale.ROOT);
            if (dificultadRepository.findByCodigo(codigo).isEmpty()) {
                throw new ReglaNegocioException("La dificultad '" + filtro.dificultad() + "' no existe");
            }
            spec = spec.and(RecetaSpecs.dificultad(codigo));
            aplicados.put("dificultad", codigo);
        }
        if (filtro.tiempoMaximo() != null) {
            if (filtro.tiempoMaximo() <= 0) {
                throw new ReglaNegocioException("El tiempo máximo debe ser mayor que 0");
            }
            spec = spec.and(RecetaSpecs.tiempoMaximo(filtro.tiempoMaximo()));
            aplicados.put("tiempoMaximo", filtro.tiempoMaximo());
        }

        Optional<Usuario> usuario = identidad.usuarioOpcional(idUsuario);
        PreferenciasPerfil.Filtros f = PreferenciasPerfil.resolver(usuario, filtro.vegetariana(),
                filtro.sinGluten(), filtro.rapida(), filtro.economica());
        if (f.vegetariana()) {
            spec = spec.and(RecetaSpecs.idsEn(recetaRepository.idsVegetarianas()));
        }
        if (f.sinGluten()) {
            spec = spec.and(RecetaSpecs.idsEn(recetaRepository.idsSinGluten()));
        }
        if (f.rapida()) {
            spec = spec.and(RecetaSpecs.tiempoMaximo(CalculadoraPreferencias.MINUTOS_RAPIDA));
        }
        if (f.economica()) {
            spec = spec.and(RecetaSpecs.idsEn(recetaRepository.idsEconomicas(
                    CalculadoraPreferencias.COSTE_MAXIMO_RACION)));
        }
        aplicados.putAll(f.comoMapa());

        final Specification<Receta> especificacion = spec;
        Page<RecetaTarjetaView> resultado = recetaRepository.findBy(especificacion,
                consulta -> consulta.as(RecetaTarjetaView.class).page(pageable));
        List<RecetaTarjetaDto> contenido = resultado.getContent().stream().map(recetaMapper::tarjeta).toList();
        return new PaginaDto<>(contenido, resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages(), aplicados);
    }

    public RecetaCompletaDto detalle(Integer idUsuario, Integer idReceta) {
        Receta receta = Comprobaciones.existe(recetaRepository.findConIngredientesById(idReceta), "Receta", idReceta);
        recetaRepository.findConPasosById(idReceta);
        if (!Boolean.TRUE.equals(receta.getPublicada())) {
            Optional<Usuario> usuario = identidad.usuarioOpcional(idUsuario);
            boolean puede = usuario.isPresent()
                    && (IdentidadService.tieneRol(usuario.get(), Roles.ADMIN)
                    || (receta.getCreadoPor() != null
                    && receta.getCreadoPor().getId().equals(usuario.get().getId())));
            if (!puede) {
                throw new RecursoNoEncontradoException("Receta", idReceta);
            }
        }
        return recetaMapper.completa(receta);
    }

    public List<CoincidenciaDto> buscarPorIngredientes(Integer idUsuario, BuscarPorIngredientesPeticion peticion) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        Set<Integer> tengo = new LinkedHashSet<>();
        if (peticion != null && peticion.idsIngredientes() != null) {
            for (Integer id : peticion.idsIngredientes()) {
                if (id != null) {
                    tengo.add(id);
                }
            }
        }
        if (tengo.isEmpty()) {
            tengo.addAll(despensaItemRepository.idsIngredientesDisponibles(usuario.getId(), Fechas.hoy()));
        }
        if (tengo.isEmpty()) {
            return List.of();
        }

        List<CoincidenciaView> coincidencias = recetaRepository.coincidenciasConIngredientes(tengo);
        if (coincidencias.isEmpty()) {
            return List.of();
        }
        List<Integer> idsRecetas = coincidencias.stream().map(CoincidenciaView::getIdReceta).toList();
        Map<Integer, Receta> recetas = new LinkedHashMap<>();
        for (Receta r : recetaRepository.findConIngredientesByIdIn(idsRecetas)) {
            recetas.put(r.getId(), r);
        }

        PreferenciasPerfil.Filtros filtros = PreferenciasPerfil.resolver(Optional.of(usuario),
                peticion == null ? null : peticion.vegetariana(), peticion == null ? null : peticion.sinGluten(),
                peticion == null ? null : peticion.rapida(), peticion == null ? null : peticion.economica());

        List<CoincidenciaDto> resultado = new ArrayList<>();
        for (CoincidenciaView c : coincidencias) {
            Receta receta = recetas.get(c.getIdReceta());
            if (receta == null || !filtros.cumple(CalculadoraPreferencias.calcular(receta))) {
                continue;
            }
            Set<Integer> vistos = new HashSet<>();
            List<String> faltan = new ArrayList<>();
            for (RecetaIngrediente linea : receta.getIngredientes()) {
                Integer idIngrediente = linea.getIngrediente().getId();
                if (!tengo.contains(idIngrediente) && vistos.add(idIngrediente)) {
                    faltan.add(linea.getIngrediente().getNombre());
                }
            }
            BigDecimal porcentaje = BigDecimal.valueOf(c.getPresentes() * 100L)
                    .divide(BigDecimal.valueOf(c.getTotal()), 1, RoundingMode.HALF_UP);
            resultado.add(new CoincidenciaDto(recetaMapper.tarjeta(receta), porcentaje,
                    c.getTotal().intValue(), c.getPresentes().intValue(), faltan));
        }
        return resultado;
    }
}
