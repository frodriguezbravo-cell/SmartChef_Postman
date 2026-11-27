package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.v1.IngredienteRecetaPeticion;
import es.safareyes.smartchef.dtos.v1.PasoRecetaPeticion;
import es.safareyes.smartchef.dtos.v1.RecetaCompletaDto;
import es.safareyes.smartchef.dtos.v1.RecetaPeticion;
import es.safareyes.smartchef.excepciones.AccesoDenegadoException;
import es.safareyes.smartchef.excepciones.ConflictoException;
import es.safareyes.smartchef.excepciones.ReglaIncumplidaException;
import es.safareyes.smartchef.mapeadores.RecetaMapper;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.RecetaIngrediente;
import es.safareyes.smartchef.modelos.RecetaPaso;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.catalogos.Dificultad;
import es.safareyes.smartchef.modelos.catalogos.TipoCocina;
import es.safareyes.smartchef.modelos.catalogos.TipoPlato;
import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;
import es.safareyes.smartchef.repositorios.IDificultadRepository;
import es.safareyes.smartchef.repositorios.IIngredienteRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.ITipoCocinaRepository;
import es.safareyes.smartchef.repositorios.ITipoPlatoRepository;
import es.safareyes.smartchef.repositorios.IUnidadMedidaRepository;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.seguridad.Roles;
import es.safareyes.smartchef.servicios.calculos.Equivalencias;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecetaGestionService {

    private static final String COCINA_POR_DEFECTO = "INTERNACIONAL";

    private final IRecetaRepository recetaRepository;
    private final IIngredienteRepository ingredienteRepository;
    private final IUnidadMedidaRepository unidadRepository;
    private final ITipoPlatoRepository tipoPlatoRepository;
    private final ITipoCocinaRepository tipoCocinaRepository;
    private final IDificultadRepository dificultadRepository;
    private final IdentidadService identidad;
    private final RecetaMapper recetaMapper;
    private final NotificacionService notificacionService;

    @Transactional
    public RecetaCompletaDto crear(Integer idUsuario, RecetaPeticion peticion) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        IdentidadService.exigirRol(usuario, Roles.CHEF, Roles.ADMIN);

        Receta receta = new Receta();
        receta.setCreadoPor(usuario);
        receta.setPublicada(true);
        receta.setEsPublica(true);
        receta.setRetirada(false);
        receta.setSlug(slugLibre(peticion.titulo()));
        aplicar(receta, peticion);
        Receta guardada = recetaRepository.save(receta);
        log.info("Receta {} creada por el usuario {}", guardada.getId(), usuario.getId());
        return recetaMapper.completa(guardada);
    }

    @Transactional
    public RecetaCompletaDto modificar(Integer idUsuario, Integer idReceta, RecetaPeticion peticion) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        Receta receta = Comprobaciones.existe(recetaRepository.findById(idReceta), "Receta", idReceta);
        exigirAutorOAdmin(usuario, receta);
        if (Boolean.TRUE.equals(receta.getRetirada())) {
            throw new ConflictoException("La receta está retirada y no se puede modificar");
        }
        receta.getIngredientes().clear();
        receta.getPasos().clear();
        recetaRepository.flush();
        aplicar(receta, peticion);
        return recetaMapper.completa(recetaRepository.save(receta));
    }

    @Transactional
    public void retirar(Integer idUsuario, Integer idReceta) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        Receta receta = Comprobaciones.existe(recetaRepository.findById(idReceta), "Receta", idReceta);
        exigirAutorOAdmin(usuario, receta);
        boolean yaRetirada = Boolean.TRUE.equals(receta.getRetirada());
        receta.setRetirada(true);
        recetaRepository.save(receta);
        if (!yaRetirada) {
            log.info("Receta {} retirada por el usuario {}", idReceta, usuario.getId());
            notificacionService.avisarRecetaRetirada(receta);
        }
    }

    private static void exigirAutorOAdmin(Usuario usuario, Receta receta) {
        boolean admin = IdentidadService.tieneRol(usuario, Roles.ADMIN);
        boolean autor = IdentidadService.tieneRol(usuario, Roles.CHEF)
                && receta.getCreadoPor() != null
                && receta.getCreadoPor().getId().equals(usuario.getId());
        if (!admin && !autor) {
            throw new AccesoDenegadoException("Solo el chef autor o un administrador pueden modificar o retirar la receta");
        }
    }

    private void aplicar(Receta receta, RecetaPeticion p) {
        TipoPlato tipoPlato = referencia(tipoPlatoRepository.findByCodigo(codigo(p.tipoPlato())),
                "El tipo de plato", p.tipoPlato());
        String codigoCocina = p.tipoCocina() == null || p.tipoCocina().isBlank()
                ? COCINA_POR_DEFECTO : codigo(p.tipoCocina());
        TipoCocina tipoCocina = referencia(tipoCocinaRepository.findByCodigo(codigoCocina),
                "El tipo de cocina", codigoCocina);
        Dificultad dificultad = referencia(dificultadRepository.findByCodigo(codigo(p.dificultad())),
                "La dificultad", p.dificultad());

        recetaMapper.actualizar(p, receta);
        receta.setTipoPlato(tipoPlato);
        receta.setTipoCocina(tipoCocina);
        receta.setDificultad(dificultad);

        Map<Integer, Ingrediente> ingredientes = new HashMap<>();
        for (Ingrediente i : ingredienteRepository.findAllById(
                p.ingredientes().stream().map(IngredienteRecetaPeticion::idIngrediente).distinct().toList())) {
            ingredientes.put(i.getId(), i);
        }
        short orden = 1;
        for (IngredienteRecetaPeticion linea : p.ingredientes()) {
            Ingrediente ingrediente = ingredientes.get(linea.idIngrediente());
            if (ingrediente == null || !Boolean.TRUE.equals(ingrediente.getPublicada())) {
                throw new ReglaIncumplidaException("El ingrediente " + linea.idIngrediente() + " no existe en el catálogo");
            }
            UnidadMedida unidad = referencia(unidadRepository.findByCodigo(codigo(linea.unidad())),
                    "La unidad", linea.unidad());
            BigDecimal gramos = linea.cantidadGramos() != null
                    ? linea.cantidadGramos()
                    : Equivalencias.aGramos(linea.cantidad(), unidad.getCodigo());

            RecetaIngrediente li = recetaMapper.aLinea(linea);
            li.setReceta(receta);
            li.setIngrediente(ingrediente);
            li.setUnidad(unidad);
            li.setCantidadGramos(gramos);
            li.setOrden(orden++);
            receta.getIngredientes().add(li);
        }

        List<PasoRecetaPeticion> pasos = p.pasos().stream()
                .sorted(Comparator.comparing(PasoRecetaPeticion::numero)).toList();
        for (PasoRecetaPeticion paso : pasos) {
            RecetaPaso rp = recetaMapper.aPaso(paso);
            rp.setReceta(receta);
            receta.getPasos().add(rp);
        }
    }

    private static String codigo(String texto) {
        return texto == null ? "" : texto.trim().toUpperCase(Locale.ROOT);
    }

    private static <T> T referencia(java.util.Optional<T> valor, String que, String codigo) {
        return valor.orElseThrow(() -> new ReglaIncumplidaException(que + " '" + codigo + "' no existe"));
    }

    private String slugLibre(String titulo) {
        String base = Textos.slug(titulo);
        String slug = base;
        int n = 2;
        while (recetaRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + n++;
        }
        return slug;
    }
}
