package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;
import es.safareyes.smartchef.pruebas.Datos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static es.safareyes.smartchef.pruebas.Datos.bd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadoraDescuentoTest {

    private final Receta receta = Datos.espaguetisAlPomodoro();
    private final Ingrediente pasta = receta.getIngredientes().get(0).getIngrediente();
    private final Ingrediente tomate = receta.getIngredientes().get(1).getIngrediente();
    private final UnidadMedida gramos = receta.getIngredientes().get(0).getUnidad();
    private final UnidadMedida kilos = Datos.unidad(2, "KG", "kilos (kg)");

    private static void igual(String esperado, BigDecimal real) {
        assertEquals(0, bd(esperado).compareTo(real), "esperado " + esperado + " pero fue " + real);
    }

    @Test
    void restaLoUsadoDeLaDespensa() {
        DespensaItem stock = Datos.stock(pasta, "500", gramos);
        List<DespensaItem> agotados = CalculadoraDescuento.descontar(receta.getIngredientes(), BigDecimal.ONE, List.of(stock));
        igual("300", stock.getCantidad());
        assertTrue(agotados.isEmpty());
    }

    @Test
    void nuncaBajaDeCeroYAvisaDeLoAgotado() {
        DespensaItem stock = Datos.stock(pasta, "150", gramos);
        List<DespensaItem> agotados = CalculadoraDescuento.descontar(receta.getIngredientes(), BigDecimal.ONE, List.of(stock));
        igual("0", stock.getCantidad());
        assertEquals(List.of(stock), agotados);
    }

    @Test
    void conviertePorEjemploKilosAGramos() {
        DespensaItem stock = Datos.stock(tomate, "1", kilos);
        CalculadoraDescuento.descontar(receta.getIngredientes(), BigDecimal.ONE, List.of(stock));
        igual("0.60", stock.getCantidad());
    }

    @Test
    void elFactorEscalaLoQueSeDescuenta() {
        DespensaItem stock = Datos.stock(tomate, "1", kilos);
        CalculadoraDescuento.descontar(receta.getIngredientes(), new BigDecimal("2"), List.of(stock));
        igual("0.20", stock.getCantidad());
    }

    @Test
    void repartePorVariosProductosDelMismoIngrediente() {
        DespensaItem primero = Datos.stock(pasta, "120", gramos);
        DespensaItem segundo = Datos.stock(pasta, "500", gramos);
        CalculadoraDescuento.descontar(receta.getIngredientes(), BigDecimal.ONE, List.of(primero, segundo));
        igual("0", primero.getCantidad());
        igual("420", segundo.getCantidad());
    }

    @Test
    void noTocaLoQueNoEstaEnLaReceta() {
        Ingrediente sal = Datos.ingrediente(99, "Sal", "0", "0", "0", "0", "0", "0", "0", "100");
        DespensaItem stock = Datos.stock(sal, "1000", gramos);
        CalculadoraDescuento.descontar(receta.getIngredientes(), BigDecimal.ONE, List.of(stock));
        igual("1000", stock.getCantidad());
    }

    @Test
    void noMezclaUnidadesIncompatibles() {
        UnidadMedida unidades = Datos.unidad(5, "UD", "unidades");
        DespensaItem stock = Datos.stock(pasta, "3", unidades);
        CalculadoraDescuento.descontar(receta.getIngredientes(), BigDecimal.ONE, List.of(stock));
        igual("3", stock.getCantidad());
    }
}
