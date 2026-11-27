package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.catalogos.Dieta;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public final class ReglasDieta {

    private ReglasDieta() {
    }

    public static boolean recetaCumple(Receta receta, Collection<String> dietasDelUsuario) {
        Set<String> dietasReceta = receta.getDietas().stream()
                .map(Dieta::getCodigo)
                .collect(Collectors.toSet());
        for (String dieta : dietasDelUsuario) {
            if (!cumple(dietasReceta, dieta)) {
                return false;
            }
        }
        return true;
    }

    private static boolean cumple(Set<String> dietasReceta, String dietaUsuario) {
        return switch (dietaUsuario) {
            case "OMNIVORA" -> true;
            case "VEGETARIANA" -> dietasReceta.contains("VEGETARIANA") || dietasReceta.contains("VEGANA");
            case "PESCETARIANA" -> dietasReceta.contains("PESCETARIANA")
                    || dietasReceta.contains("VEGETARIANA") || dietasReceta.contains("VEGANA");
            default -> dietasReceta.contains(dietaUsuario);
        };
    }
}
