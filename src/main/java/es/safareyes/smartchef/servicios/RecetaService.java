package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.IngredienteEscalado;
import es.safareyes.smartchef.dtos.ResumenNutricional;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.UsuarioAlergeno;
import es.safareyes.smartchef.modelos.catalogos.Alergeno;
import es.safareyes.smartchef.modelos.catalogos.Dieta;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioAlergenoRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import es.safareyes.smartchef.servicios.calculos.CalculadoraNutricion;
import es.safareyes.smartchef.servicios.calculos.Escalador;
import es.safareyes.smartchef.servicios.calculos.ReglasDieta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecetaService {

    private final IRecetaRepository recetaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IUsuarioAlergenoRepository usuarioAlergenoRepository;

    public Receta obtenerPorId(Integer id) {
        return Comprobaciones.existe(recetaRepository.findById(id), "Receta", id);
    }

    public Receta obtenerPorSlug(String slug) {
        return Comprobaciones.existe(recetaRepository.findBySlug(slug), "Receta", slug);
    }

    public List<Receta> buscarPorTitulo(String texto) {
        return recetaRepository.findByTituloContainingIgnoreCaseAndPublicadaTrue(texto);
    }

    public List<Receta> recetasRapidas(int minutos) {
        if (minutos <= 0) {
            throw new ReglaNegocioException("Los minutos deben ser mayores que 0");
        }
        return recetaRepository.buscarRapidas(minutos);
    }

    public List<Receta> recetasSegurasPara(Integer idUsuario) {
        Usuario usuario = Comprobaciones.existe(usuarioRepository.findById(idUsuario), "Usuario", idUsuario);

        List<String> alergenos = usuarioAlergenoRepository.codigosDeAlergenos(idUsuario);
        List<Receta> candidatas = alergenos.isEmpty()
                ? recetaRepository.findByPublicadaTrue()
                : recetaRepository.buscarSinAlergenos(alergenos);

        List<String> dietas = usuario.getDietas().stream().map(Dieta::getCodigo).toList();
        return candidatas.stream()
                .filter(receta -> ReglasDieta.recetaCumple(receta, dietas))
                .toList();
    }

    public ResumenNutricional nutricion(Integer idReceta, int raciones) {
        validarRaciones(raciones);
        return CalculadoraNutricion.paraRaciones(obtenerPorId(idReceta), raciones);
    }

    public List<IngredienteEscalado> ingredientesPara(Integer idReceta, int raciones) {
        validarRaciones(raciones);
        return Escalador.escalar(obtenerPorId(idReceta), raciones);
    }

    private static void validarRaciones(int raciones) {
        if (raciones <= 0) {
            throw new ReglaNegocioException("Las raciones deben ser mayores que 0");
        }
    }

    public List<UsuarioAlergeno> avisosDeAlergenos(Integer idUsuario, Integer idReceta) {
        Comprobaciones.existe(usuarioRepository.findById(idUsuario), "Usuario", idUsuario);
        Receta receta = obtenerPorId(idReceta);
        Set<String> enLaReceta = receta.getIngredientes().stream()
                .flatMap(linea -> linea.getIngrediente().getAlergenos().stream())
                .map(Alergeno::getCodigo)
                .collect(Collectors.toSet());
        return usuarioAlergenoRepository.findByUsuarioId(idUsuario).stream()
                .filter(alergia -> enLaReceta.contains(alergia.getAlergeno().getCodigo()))
                .sorted(Comparator.comparing((UsuarioAlergeno alergia) -> alergia.getGravedad().getOrden()).reversed())
                .toList();
    }
}
