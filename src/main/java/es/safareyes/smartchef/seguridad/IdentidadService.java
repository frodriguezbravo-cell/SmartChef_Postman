package es.safareyes.smartchef.seguridad;

import es.safareyes.smartchef.excepciones.AccesoDenegadoException;
import es.safareyes.smartchef.excepciones.NoAutenticadoException;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdentidadService {

    private final IUsuarioRepository usuarioRepository;

    public Usuario usuarioActual(Integer idUsuario) {
        return usuarioOpcional(idUsuario)
                .orElseThrow(() -> new NoAutenticadoException(
                        "Hace falta iniciar sesión con un token válido"));
    }

    public Optional<Usuario> usuarioOpcional(Integer idUsuario) {
        if (idUsuario == null) {
            return Optional.empty();
        }
        return usuarioRepository.findById(idUsuario).filter(u -> Boolean.TRUE.equals(u.getActivo()));
    }

    public static Integer id(UsuarioAutenticado usuario) {
        return usuario == null ? null : usuario.getId();
    }

    public static String codigoRol(Usuario usuario) {
        return usuario.getRol() == null ? null : usuario.getRol().getCodigo();
    }

    public static boolean tieneRol(Usuario usuario, String... codigos) {
        String actual = codigoRol(usuario);
        return actual != null && Arrays.asList(codigos).contains(actual);
    }

    public static void exigirRol(Usuario usuario, String... codigos) {
        if (!tieneRol(usuario, codigos)) {
            throw new AccesoDenegadoException("Tu rol no permite hacer esto (se necesita: " + String.join(" o ", codigos) + ")");
        }
    }
}
