package es.safareyes.smartchef.validacion;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasosConsecutivosValidatorTest {

    @Test
    void aceptaNumeracionSinHuecosEnCualquierOrden() {
        assertTrue(PasosConsecutivosValidator.consecutivos(List.of(1)));
        assertTrue(PasosConsecutivosValidator.consecutivos(List.of(1, 2, 3)));
        assertTrue(PasosConsecutivosValidator.consecutivos(List.of(3, 1, 2)));
    }

    @Test
    void rechazaHuecosRepetidosYNumerosQueNoEmpiezanEnUno() {
        assertFalse(PasosConsecutivosValidator.consecutivos(List.of(1, 3)));
        assertFalse(PasosConsecutivosValidator.consecutivos(List.of(1, 1, 2)));
        assertFalse(PasosConsecutivosValidator.consecutivos(List.of(2, 3)));
        assertFalse(PasosConsecutivosValidator.consecutivos(Arrays.asList(1, null)));
    }
}
