package es.safareyes.smartchef.servicios.calculos;

import org.junit.jupiter.api.Test;

import static es.safareyes.smartchef.pruebas.Datos.bd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EquivalenciasTest {

    @Test
    void conviertePesosYVolumenes() {
        assertEquals(0, bd("1000").compareTo(Equivalencias.convertir(bd("1"), "KG", "G")));
        assertEquals(0, bd("0.5").compareTo(Equivalencias.convertir(bd("500"), "ML", "L")));
        assertEquals(0, bd("3").compareTo(Equivalencias.convertir(bd("3"), "UD", "UD")));
    }

    @Test
    void noConvierteUnidadesIncompatibles() {
        assertNull(Equivalencias.convertir(bd("1"), "UD", "G"));
        assertNull(Equivalencias.convertir(bd("1"), "KG", "L"));
        assertNull(Equivalencias.convertir(null, "G", "KG"));
    }

    @Test
    void calculaGramos() {
        assertEquals(0, bd("2000").compareTo(Equivalencias.aGramos(bd("2"), "KG")));
        assertEquals(0, bd("250").compareTo(Equivalencias.aGramos(bd("250"), "ML")));
        assertNull(Equivalencias.aGramos(bd("2"), "UD"));
    }
}
