package es.safareyes.smartchef.controladores.v1;

import es.safareyes.smartchef.dtos.v1.LoginPeticion;
import es.safareyes.smartchef.dtos.v1.RegistroUsuarioPeticion;
import es.safareyes.smartchef.dtos.v1.TokenDto;
import es.safareyes.smartchef.dtos.v1.UsuarioDto;
import es.safareyes.smartchef.seguridad.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticación", description = "Registro e inicio de sesión con JWT")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthV1Controller {

    private final AuthService authService;

    @Operation(summary = "Alta de usuario con el rol USUARIO")
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto registro(@RequestBody @Valid RegistroUsuarioPeticion peticion) {
        return authService.registrar(peticion);
    }

    @Operation(summary = "Inicio de sesión: devuelve el token JWT (caduca en 60 minutos)")
    @PostMapping("/login")
    public TokenDto login(@RequestBody @Valid LoginPeticion peticion) {
        return authService.login(peticion);
    }
}
