package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.almacenamiento.StorageService;
import es.safareyes.smartchef.dtos.v1.FotoDto;
import es.safareyes.smartchef.excepciones.AccesoDenegadoException;
import es.safareyes.smartchef.excepciones.ConflictoException;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaIncumplidaException;
import es.safareyes.smartchef.mapeadores.RecetaMapper;
import es.safareyes.smartchef.modelos.FotoReceta;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.repositorios.IFotoRecetaRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.seguridad.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FotoService {

    public static final int MAX_ADICIONALES = 4;

    private final IFotoRecetaRepository fotoRepository;
    private final IRecetaRepository recetaRepository;
    private final StorageService storage;
    private final IdentidadService identidad;
    private final RecetaMapper recetaMapper;

    public record Descarga(Resource fichero, String tipo, String nombre) {
    }

    @Transactional
    public FotoDto subir(Integer idUsuario, Integer idReceta, MultipartFile fichero, boolean portada) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        Receta receta = Comprobaciones.existe(recetaRepository.findById(idReceta), "Receta", idReceta);
        exigirAutor(usuario, receta);
        if (Boolean.TRUE.equals(receta.getRetirada())) {
            throw new ConflictoException("La receta está retirada y no admite fotos nuevas");
        }
        String tipo = storage.validar(fichero);
        long adicionales = fotoRepository.countByRecetaIdAndPortadaFalse(idReceta);
        if (portada && fotoRepository.existsByRecetaIdAndPortadaTrue(idReceta)) {
            throw new ConflictoException("La receta ya tiene foto de portada: bórrala antes de subir otra");
        }
        if (!portada && adicionales >= MAX_ADICIONALES) {
            throw new ReglaIncumplidaException("Una receta admite como máximo " + MAX_ADICIONALES
                    + " fotos además de la portada");
        }

        String clave = storage.guardar(fichero);
        FotoReceta foto = new FotoReceta();
        foto.setReceta(receta);
        foto.setNombreOriginal(nombreOriginal(fichero));
        foto.setClave(clave);
        foto.setTipoContenido(tipo);
        foto.setTamanoBytes(fichero.getSize());
        foto.setPortada(portada);
        foto.setOrden((short) (portada ? 0 : adicionales + 1));
        FotoReceta guardada = fotoRepository.save(foto);
        log.info("Foto {} subida a la receta {} por el usuario {} (portada: {})",
                guardada.getId(), idReceta, usuario.getId(), portada);
        return recetaMapper.foto(guardada);
    }

    public Descarga descargar(Integer idFoto) {
        FotoReceta foto = Comprobaciones.existe(fotoRepository.findById(idFoto), "Foto", idFoto);
        return new Descarga(storage.cargar(foto.getClave()), foto.getTipoContenido(), foto.getNombreOriginal());
    }

    @Transactional
    public void borrar(Integer idUsuario, Integer idReceta, Integer idFoto) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        Receta receta = Comprobaciones.existe(recetaRepository.findById(idReceta), "Receta", idReceta);
        exigirAutor(usuario, receta);
        FotoReceta foto = fotoRepository.findByIdAndRecetaId(idFoto, idReceta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Foto", idFoto));
        String clave = foto.getClave();
        fotoRepository.delete(foto);
        fotoRepository.flush();
        storage.borrar(clave);
        log.info("Foto {} de la receta {} borrada por el usuario {}", idFoto, idReceta, usuario.getId());
    }

    private static void exigirAutor(Usuario usuario, Receta receta) {
        boolean admin = IdentidadService.tieneRol(usuario, Roles.ADMIN);
        boolean autor = IdentidadService.tieneRol(usuario, Roles.CHEF)
                && receta.getCreadoPor() != null
                && receta.getCreadoPor().getId().equals(usuario.getId());
        if (!admin && !autor) {
            throw new AccesoDenegadoException("Solo el chef autor de la receta puede gestionar sus fotos");
        }
    }

    private static String nombreOriginal(MultipartFile fichero) {
        String nombre = fichero.getOriginalFilename();
        if (nombre == null || nombre.isBlank()) {
            return "foto";
        }
        String limpio = nombre.replace("\\", "/");
        limpio = limpio.substring(limpio.lastIndexOf('/') + 1);
        return limpio.length() > 255 ? limpio.substring(0, 255) : limpio;
    }
}
