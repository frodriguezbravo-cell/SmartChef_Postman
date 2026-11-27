package es.safareyes.smartchef.dtos.v1;

public record UsuarioDto(
        Integer id,
        String email,
        String username,
        String nombre,
        String rol) {
}
