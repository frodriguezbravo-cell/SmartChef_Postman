package es.safareyes.smartchef.dtos.v1;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioPeticion(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[A-Za-z0-9._-]+$") String username,
        @NotBlank @Size(min = 6, max = 72) String password,
        @Size(max = 100) String nombre) {
}
