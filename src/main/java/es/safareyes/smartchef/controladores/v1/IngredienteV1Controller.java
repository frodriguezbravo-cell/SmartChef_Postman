package es.safareyes.smartchef.controladores.v1;

import es.safareyes.smartchef.dtos.v1.IngredienteCatalogoDto;
import es.safareyes.smartchef.dtos.v1.PaginaDto;
import es.safareyes.smartchef.servicios.IngredienteCatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ingredientes", description = "Catálogo común de ingredientes")
@RestController
@RequestMapping("/api/v1/ingredientes")
@RequiredArgsConstructor
public class IngredienteV1Controller {

    private final IngredienteCatalogoService servicio;

    @Operation(summary = "6 · Catálogo de ingredientes paginado")
    @GetMapping
    public PaginaDto<IngredienteCatalogoDto> buscar(
            @RequestParam(name = "texto", required = false) String texto,
            @RequestParam(name = "pagina", required = false) Integer pagina,
            @RequestParam(name = "tamano", required = false) Integer tamano,
            @RequestParam(name = "orden", required = false) String orden) {
        return servicio.buscar(texto, pagina, tamano, orden);
    }
}
