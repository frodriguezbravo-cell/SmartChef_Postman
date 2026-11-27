package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.v1.ColeccionDto;
import es.safareyes.smartchef.excepciones.ConflictoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.mapeadores.ColeccionMapper;
import es.safareyes.smartchef.modelos.Coleccion;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.repositorios.IColeccionRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.seguridad.IdentidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ColeccionService {

    public static final String NOMBRE_FAVORITAS = "Favoritas";
    private static final int MAX_NOMBRE = 100;

    private final IColeccionRepository coleccionRepository;
    private final IRecetaRepository recetaRepository;
    private final IdentidadService identidad;
    private final ColeccionMapper coleccionMapper;

    @Transactional
    public List<ColeccionDto> listar(Integer idUsuario) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        crearFavoritas(usuario);
        return coleccionMapper.colecciones(coleccionRepository.findByUsuarioIdOrderByNombreAsc(usuario.getId()));
    }

    @Transactional
    public ColeccionDto guardarReceta(Integer idUsuario, String nombre, Integer idReceta) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty() || limpio.length() > MAX_NOMBRE) {
            throw new ReglaNegocioException("El nombre de la colección debe tener entre 1 y " + MAX_NOMBRE + " caracteres");
        }
        Receta receta = Comprobaciones.existe(recetaRepository.findById(idReceta), "Receta", idReceta);
        if (!Boolean.TRUE.equals(receta.getPublicada())) {
            throw new es.safareyes.smartchef.excepciones.RecursoNoEncontradoException("Receta", idReceta);
        }
        if (Boolean.TRUE.equals(receta.getRetirada())) {
            throw new ConflictoException("La receta está retirada y no se puede guardar en una colección");
        }

        crearFavoritas(usuario);
        Coleccion coleccion = coleccionRepository.findByUsuarioIdAndNombreIgnoreCase(usuario.getId(), limpio)
                .orElseGet(() -> {
                    Coleccion nueva = new Coleccion();
                    nueva.setUsuario(usuario);
                    nueva.setNombre(limpio);
                    nueva.setEsFavoritas(false);
                    return nueva;
                });
        boolean yaEstaba = coleccion.getRecetas().stream().anyMatch(r -> r.getId().equals(receta.getId()));
        if (!yaEstaba) {
            coleccion.getRecetas().add(receta);
        }
        return coleccionMapper.coleccion(coleccionRepository.saveAndFlush(coleccion));
    }

    @Transactional
    public void crearFavoritas(Usuario usuario) {
        if (!coleccionRepository.existsByUsuarioIdAndEsFavoritasTrue(usuario.getId())) {
            Coleccion favoritas = new Coleccion();
            favoritas.setUsuario(usuario);
            favoritas.setNombre(NOMBRE_FAVORITAS);
            favoritas.setEsFavoritas(true);
            coleccionRepository.saveAndFlush(favoritas);
        }
    }
}
