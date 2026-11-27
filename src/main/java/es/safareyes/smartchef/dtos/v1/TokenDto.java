package es.safareyes.smartchef.dtos.v1;

public record TokenDto(
        String token,
        String tipo,
        long expiraEnSegundos,
        UsuarioDto usuario) {
}
