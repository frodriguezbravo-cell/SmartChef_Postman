package es.safareyes.smartchef.validacion;

import es.safareyes.smartchef.dtos.v1.PasoRecetaPeticion;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class PasosConsecutivosValidator implements ConstraintValidator<PasosConsecutivos, List<PasoRecetaPeticion>> {

    @Override
    public boolean isValid(List<PasoRecetaPeticion> pasos, ConstraintValidatorContext contexto) {
        if (pasos == null) {
            return true;
        }
        return consecutivos(pasos.stream().map(p -> p == null ? null : p.numero()).toList());
    }

    public static boolean consecutivos(List<Integer> numeros) {
        if (numeros.stream().anyMatch(n -> n == null)) {
            return false;
        }
        List<Integer> ordenados = numeros.stream().sorted().toList();
        for (int i = 0; i < ordenados.size(); i++) {
            if (ordenados.get(i) != i + 1) {
                return false;
            }
        }
        return true;
    }
}
