package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.RecetaIngrediente;
import es.safareyes.smartchef.modelos.catalogos.Alergeno;
import es.safareyes.smartchef.pruebas.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static es.safareyes.smartchef.pruebas.Datos.bd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadoraPreferenciasTest {

    private Receta receta;

    @BeforeEach
    void preparar() {
        receta = Datos.espaguetisAlPomodoro();
        receta.setTiempoPreparacionMin((short) 10);
        receta.setTiempoCoccionMin((short) 15);
        String[] precios = {"1.50", "2.20", "7.00", "8.50", "18.00"};
        for (int i = 0; i < precios.length; i++) {
            receta.getIngredientes().get(i).getIngrediente().setPrecioKg(bd(precios[i]));
        }
    }

    private RecetaIngrediente linea(int posicion) {
        return receta.getIngredientes().get(posicion);
    }

    @Test
    void elTiempoTotalSumaPreparacionYCoccion() {
        assertEquals(25, CalculadoraPreferencias.tiempoTotal(receta));
        receta.setTiempoCoccionMin(null);
        assertEquals(10, CalculadoraPreferencias.tiempoTotal(receta));
    }

    @Test
    void esRapidaSiTardaTreintaMinutosOMenos() {
        assertTrue(CalculadoraPreferencias.calcular(receta).rapida());
        receta.setTiempoCoccionMin((short) 20);
        assertTrue(CalculadoraPreferencias.calcular(receta).rapida());
        receta.setTiempoCoccionMin((short) 21);
        assertFalse(CalculadoraPreferencias.calcular(receta).rapida());
    }

    @Test
    void esVegetarianaSiTodosLosIngredientesLoSon() {
        assertTrue(CalculadoraPreferencias.calcular(receta).vegetariana());
        linea(4).getIngrediente().setAptoVegetariano(false);
        assertFalse(CalculadoraPreferencias.calcular(receta).vegetariana());
    }

    @Test
    void esSinGlutenSiNingunIngredienteTieneGluten() {
        assertTrue(CalculadoraPreferencias.calcular(receta).sinGluten());
        Alergeno gluten = new Alergeno();
        gluten.setCodigo("GLUTEN");
        linea(0).getIngrediente().getAlergenos().add(gluten);
        assertFalse(CalculadoraPreferencias.calcular(receta).sinGluten());
    }

    @Test
    void elCostePorRacionSaleDeLosGramosYElPrecioPorKilo() {
        CalculadoraPreferencias.Coste coste = CalculadoraPreferencias.coste(receta);
        assertEquals(0, bd("0.69").compareTo(coste.porRacion()));
        assertTrue(coste.completo());
        assertTrue(CalculadoraPreferencias.calcular(receta).economica());
    }

    @Test
    void noEsEconomicaSiSuperaLosDosEurosConCincuenta() {
        linea(3).getIngrediente().setPrecioKg(bd("1000"));
        CalculadoraPreferencias.Coste coste = CalculadoraPreferencias.coste(receta);
        assertTrue(coste.porRacion().compareTo(CalculadoraPreferencias.COSTE_MAXIMO_RACION) > 0);
        assertFalse(CalculadoraPreferencias.calcular(receta).economica());
    }

    @Test
    void conUnIngredienteSinPrecioElCosteEsIncompletoYNoEsEconomica() {
        linea(1).getIngrediente().setPrecioKg(null);
        CalculadoraPreferencias.Coste coste = CalculadoraPreferencias.coste(receta);
        assertFalse(coste.completo());
        assertFalse(CalculadoraPreferencias.calcular(receta).economica());
    }

    @Test
    void sinPreciosNoSePuedeCalcularElCoste() {
        for (RecetaIngrediente l : receta.getIngredientes()) {
            l.getIngrediente().setPrecioKg(null);
        }
        CalculadoraPreferencias.Coste coste = CalculadoraPreferencias.coste(receta);
        assertNull(coste.porRacion());
        assertFalse(coste.completo());
    }
}
