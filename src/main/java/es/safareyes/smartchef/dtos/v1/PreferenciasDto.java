package es.safareyes.smartchef.dtos.v1;

public record PreferenciasDto(
        boolean vegetariana,
        boolean sinGluten,
        boolean rapida,
        boolean economica) {
}
