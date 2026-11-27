package es.safareyes.smartchef.dtos.v1;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginPeticion(
        @NotBlank @Email String email,
        @NotBlank String password) {
}
