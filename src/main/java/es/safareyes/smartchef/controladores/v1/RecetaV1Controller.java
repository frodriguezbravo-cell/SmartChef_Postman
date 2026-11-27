package es.safareyes.smartchef.controladores.v1;

import es.safareyes.smartchef.dtos.v1.BuscarPorIngredientesPeticion;
import es.safareyes.smartchef.dtos.v1.CoincidenciaDto;
import es.safareyes.smartchef.dtos.v1.PaginaDto;
import es.safareyes.smartchef.dtos.v1.RecetaCompletaDto;
import es.safareyes.smartchef.dtos.v1.RecetaPeticion;
import es.safareyes.smartchef.dtos.v1.RecetaTarjetaDto;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.seguridad.UsuarioAutenticado;
import es.safareyes.smartchef.servicios.RecetaCatalogoService;
import es.safareyes.smartchef.servicios.RecetaGestionService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Tag(name = "Recetas", description = "Catálogo, detalle, alta, modificación, retirada y búsqueda por ingredientes")
@RestController
@RequestMapping("/api/v1/recetas")
@RequiredArgsConstructor
public class RecetaV1Controller {

    private final RecetaCatalogoService catalogoService;
    private final RecetaGestionService gestionService;

    @Operation(summary = "1 · Catálogo paginado con filtros")
    @GetMapping
    public PaginaDto<RecetaTarjetaDto> catalogo(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(name = "texto", required = false) String texto,
            @RequestParam(name = "dificultad", required = false) String dificultad,
            @RequestParam(name = "tiempoMaximo", required = false) Integer tiempoMaximo,
            @RequestParam(name = "vegetariana", required = false) Boolean vegetariana,
            @RequestParam(name = "sinGluten", required = false) Boolean sinGluten,
            @RequestParam(name = "rapida", required = false) Boolean rapida,
            @RequestParam(name = "economica", required = false) Boolean economica,
            @RequestParam(name = "pagina", required = false) Integer pagina,
            @RequestParam(name = "tamano", required = false) Integer tamano,
            @RequestParam(name = "orden", required = false) String orden) {
        return catalogoService.catalogo(IdentidadService.id(usuario),
                new RecetaCatalogoService.Filtro(texto, dificultad, tiempoMaximo, vegetariana, sinGluten,
                        rapida, economica),
                pagina, tamano, orden);
    }

    @Operation(summary = "2 · Detalle con ingredientes, pasos, coste, calorías, preferencias y fotos")
    @GetMapping("/{id}")
    public RecetaCompletaDto detalle(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") Integer id) {
        return catalogoService.detalle(IdentidadService.id(usuario), id);
    }

    @Operation(summary = "3 · Alta de receta")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN')")
    @PostMapping
    public ResponseEntity<RecetaCompletaDto> crear(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody @Valid RecetaPeticion peticion) {
        RecetaCompletaDto creada = gestionService.crear(IdentidadService.id(usuario), peticion);
        return ResponseEntity.created(URI.create("/api/v1/recetas/" + creada.id())).body(creada);
    }

    @Operation(summary = "4 · Modificación de receta (chef autor o admin)")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN')")
    @PutMapping("/{id}")
    public RecetaCompletaDto modificar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") Integer id,
            @RequestBody @Valid RecetaPeticion peticion) {
        return gestionService.modificar(IdentidadService.id(usuario), id, peticion);
    }

    @Operation(summary = "5 · Retirada de receta (baja lógica)")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") Integer id) {
        gestionService.retirar(IdentidadService.id(usuario), id);
    }

    @Operation(summary = "7 · Propuestas según la despensa o una lista de ingredientes")
    @PostMapping("/buscar-por-ingredientes")
    public List<CoincidenciaDto> buscarPorIngredientes(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody(required = false) @Valid BuscarPorIngredientesPeticion peticion) {
        return catalogoService.buscarPorIngredientes(IdentidadService.id(usuario), peticion);
    }
}
