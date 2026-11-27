package es.safareyes.smartchef.seguridad;

import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioDetallesService implements UserDetailsService {

    private final IUsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UsuarioAutenticado loadUserByUsername(String email) {
        String buscado = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByEmail(buscado)
                .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario " + buscado));
        return new UsuarioAutenticado(usuario.getId(), usuario.getEmail(), IdentidadService.codigoRol(usuario),
                usuario.getPasswordHash(), Boolean.TRUE.equals(usuario.getActivo()));
    }
}
