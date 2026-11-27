package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextosYPaginasTest {

    private static final Map<String, String> ORDEN = Map.of("titulo", "titulo", "fecha", "creadoEn");

    @Test
    void elSlugNoLlevaTildesNiSimbolos() {
        assertEquals("pollo-al-ajillo-con-arroz", Textos.slug("Pollo al ajillo con arroz!"));
        assertEquals("creme-brulee", Textos.slug("  Crème brûlée "));
        assertEquals("receta", Textos.slug("¡¡¡"));
        assertEquals("receta", Textos.slug(null));
    }

    @Test
    void laPaginacionTieneValoresPorDefecto() {
        PageRequest p = Paginas.de(null, null, null, ORDEN, "titulo");
        assertEquals(0, p.getPageNumber());
        assertEquals(Paginas.TAMANO_POR_DEFECTO, p.getPageSize());
        assertEquals(Sort.Direction.ASC, p.getSort().getOrderFor("titulo").getDirection());
    }

    @Test
    void sePuedeOrdenarDescendentePorUnCampoPermitido() {
        PageRequest p = Paginas.de(2, 5, "fecha,desc", ORDEN, "titulo");
        assertEquals(2, p.getPageNumber());
        assertEquals(5, p.getPageSize());
        assertEquals(Sort.Direction.DESC, p.getSort().getOrderFor("creadoEn").getDirection());
    }

    @Test
    void rechazaPaginasTamanosYOrdenesNoValidos() {
        assertThrows(ReglaNegocioException.class, () -> Paginas.de(-1, 10, null, ORDEN, "titulo"));
        assertThrows(ReglaNegocioException.class, () -> Paginas.de(0, 0, null, ORDEN, "titulo"));
        assertThrows(ReglaNegocioException.class, () -> Paginas.de(0, Paginas.TAMANO_MAXIMO + 1, null, ORDEN, "titulo"));
        assertThrows(ReglaNegocioException.class, () -> Paginas.de(0, 10, "password", ORDEN, "titulo"));
        assertThrows(ReglaNegocioException.class, () -> Paginas.de(0, 10, "titulo,arriba", ORDEN, "titulo"));
    }
}
