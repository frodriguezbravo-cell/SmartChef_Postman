package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.Despensa;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.pruebas.PruebaBaseDatos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DespensaRepositoriesTest extends PruebaBaseDatos {

    @Autowired private IDespensaRepository despensas;
    @Autowired private IDespensaItemRepository productos;

    @Test
    void cadaUsuarioVeSoloSusDespensas() {
        int ana = bd().usuario("ana");
        int luis = bd().usuario("luis");
        bd().despensa(ana, "Casa");
        bd().despensa(ana, "Playa");
        bd().despensa(luis, "Casa");

        List<Despensa> deAna = despensas.findByUsuarioId(ana);
        assertEquals(2, deAna.size());
        assertTrue(deAna.stream().allMatch(d -> d.getUsuario().getId().equals(ana)));
        assertEquals(1, despensas.findByUsuarioId(luis).size());
        assertTrue(despensas.findByUsuarioId(luis + 1000).isEmpty());
    }

    @Test
    void elNombreDeLaDespensaSeComprueba() {
        int ana = bd().usuario("ana");
        int luis = bd().usuario("luis");
        bd().despensa(ana, "Playa");

        assertTrue(despensas.existsByUsuarioIdAndNombre(ana, "Playa"));
        assertFalse(despensas.existsByUsuarioIdAndNombre(ana, "Casa"));
        assertFalse(despensas.existsByUsuarioIdAndNombre(luis, "Playa"));
    }

    @Test
    void losProductosDisponiblesSonLosQueNoSeHanDadoDeBaja() {
        int despensa = bd().despensa(bd().usuario("ana"), "Casa");
        bd().producto(despensa, "Tomate", 5, false);
        bd().producto(despensa, "Ajo", null, false);
        bd().producto(despensa, "Cebolla", 5, true);

        List<DespensaItem> disponibles = productos.findByDespensaIdAndFechaBajaIsNull(despensa);
        assertEquals(2, disponibles.size());
        assertTrue(disponibles.stream().noneMatch(p -> p.getIngrediente().getNombre().equals("Cebolla")));
        assertTrue(disponibles.stream().allMatch(p -> p.getFechaBaja() == null && p.getMotivoBaja() == null));
        assertEquals("gramos (g)", disponibles.get(0).getUnidad().getNombre());
        assertEquals("ALACENA", disponibles.get(0).getUbicacion().getCodigo());
        assertTrue(productos.findByDespensaIdAndFechaBajaIsNull(despensa + 1000).isEmpty());
    }

    @Test
    void unProductoDadoDeBajaConservaSuMotivo() {
        int despensa = bd().despensa(bd().usuario("ana"), "Casa");
        int baja = bd().producto(despensa, "Cebolla", null, true);
        bd().producto(despensa, "Tomate", null, false);

        DespensaItem item = productos.findById(baja).orElseThrow();
        assertNotNull(item.getFechaBaja());
        assertEquals("CONSUMIDO", item.getMotivoBaja().getCodigo());
        assertFalse(item.getFechaBaja().isBefore(item.getFechaCompra()));
    }

    @Test
    void losQueCaducanAntesDeLaFechaLimiteVanOrdenadosPorCaducidad() {
        int ana = bd().usuario("ana");
        int casa = bd().despensa(ana, "Casa");
        int playa = bd().despensa(ana, "Playa");
        bd().producto(casa, "Tomate", 2, false);
        bd().producto(playa, "Ajo", -1, false);
        bd().producto(casa, "Cebolla", 10, false);
        bd().producto(casa, "Huevo", null, false);
        bd().producto(casa, "Leche entera", 1, true);
        int otroUsuario = bd().usuario("luis");
        bd().producto(bd().despensa(otroUsuario, "Casa"), "Salmón", 1, false);

        LocalDate limite = bd().hoy().plusDays(3);
        List<DespensaItem> resultado = productos.buscarQueCaducanAntesDe(ana, limite);
        assertEquals(2, resultado.size());
        assertEquals("Ajo", resultado.get(0).getIngrediente().getNombre());
        assertEquals("Tomate", resultado.get(1).getIngrediente().getNombre());
        assertTrue(resultado.get(0).getFechaCaducidad().isBefore(resultado.get(1).getFechaCaducidad()));

        assertEquals(3, productos.buscarQueCaducanAntesDe(ana, bd().hoy().plusDays(10)).size());
        assertTrue(productos.buscarQueCaducanAntesDe(ana, bd().hoy().minusDays(5)).isEmpty());
        assertEquals(1, productos.buscarQueCaducanAntesDe(otroUsuario, limite).size());
    }
}
