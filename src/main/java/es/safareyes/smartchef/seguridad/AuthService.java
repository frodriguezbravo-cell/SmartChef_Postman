package es.safareyes.smartchef.seguridad;

import es.safareyes.smartchef.dtos.v1.LoginPeticion;
import es.safareyes.smartchef.dtos.v1.RegistroUsuarioPeticion;
import es.safareyes.smartchef.dtos.v1.TokenDto;
import es.safareyes.smartchef.dtos.v1.UsuarioDto;
import es.safareyes.smartchef.excepciones.ConflictoException;
import es.safareyes.smartchef.excepciones.NoAutenticadoException;
import es.safareyes.smartchef.mapeadores.UsuarioMapper;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.repositorios.IRolUsuarioRepository;
import es.safareyes.smartchef.repositorios.IUnidadTemperaturaRepository;
import es.safareyes.smartchef.repositorios.IUnidadVolumenRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import es.safareyes.smartchef.servicios.ColeccionService;
import es.safareyes.smartchef.servicios.Fechas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TEMPERATURA_POR_DEFECTO = "C";
    private static final String VOLUMEN_POR_DEFECTO = "L";

    private final IUsuarioRepository usuarioRepository;
    private final IRolUsuarioRepository rolRepository;
    private final IUnidadTemperaturaRepository temperaturaRepository;
    private final IUnidadVolumenRepository volumenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ColeccionService coleccionService;
    private final UsuarioMapper usuarioMapper;

    @Transactional
    public UsuarioDto registrar(RegistroUsuarioPeticion peticion) {
        String email = peticion.email().trim().toLowerCase(Locale.ROOT);
        String username = peticion.username().trim();
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictoException("Ya hay una cuenta con el email " + email);
        }
        if (usuarioRepository.existsByUsername(username)) {
            throw new ConflictoException("El nombre de usuario " + username + " ya está en uso");
        }
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setUsername(username);
        usuario.setNombre(peticion.nombre());
        usuario.setPasswordHash(passwordEncoder.encode(peticion.password()));
        usuario.setRol(rolRepository.findByCodigo(Roles.USUARIO)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + Roles.USUARIO)));
        usuario.setUnidadTemperatura(temperaturaRepository.findByCodigo(TEMPERATURA_POR_DEFECTO)
                .orElseThrow(() -> new IllegalStateException("Falta la unidad " + TEMPERATURA_POR_DEFECTO)));
        usuario.setUnidadVolumen(volumenRepository.findByCodigo(VOLUMEN_POR_DEFECTO)
                .orElseThrow(() -> new IllegalStateException("Falta la unidad " + VOLUMEN_POR_DEFECTO)));
        Usuario guardado = usuarioRepository.save(usuario);
        coleccionService.crearFavoritas(guardado);
        log.info("Nuevo usuario registrado: {} ({})", guardado.getId(), email);
        return usuarioMapper.usuario(guardado);
    }

    @Transactional
    public TokenDto login(LoginPeticion peticion) {
        String email = peticion.email().trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .filter(u -> passwordEncoder.matches(peticion.password(), u.getPasswordHash()))
                .orElseThrow(() -> new NoAutenticadoException("Email o contraseña incorrectos"));
        usuario.setUltimoAcceso(LocalDateTime.now(Fechas.MADRID));
        UsuarioAutenticado autenticado = new UsuarioAutenticado(usuario.getId(), usuario.getEmail(),
                IdentidadService.codigoRol(usuario), usuario.getPasswordHash(), true);
        log.info("Inicio de sesión del usuario {}", usuario.getId());
        return new TokenDto(jwtService.generar(autenticado), "Bearer", jwtService.segundosDeValidez(),
                usuarioMapper.usuario(usuario));
    }
}
