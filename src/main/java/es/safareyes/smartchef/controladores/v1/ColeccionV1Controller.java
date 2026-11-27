package es.safareyes.smartchef.controladores.v1;

import es.safareyes.smartchef.dtos.v1.ColeccionDto;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.seguridad.UsuarioAutenticado;
import es.safareyes.smartchef.servicios.ColeccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Colecciones", description = "Recetas guardadas en colecciones")
@RestController
@RequestMapping("/api/v1/colecciones")
@RequiredArgsConstructor
public class ColeccionV1Controller {

    private final ColeccionService servicio;

    @Operation(summary = "12 · Mis colecciones con sus recetas")
    @GetMapping
    public List<ColeccionDto> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicio.listar(IdentidadService.id(usuario));
    }

    @Operation(summary = "13 · Guardar receta en una colección (la crea si no existe)")
    @PutMapping("/{nombre}/recetas/{recetaId}")
    public ColeccionDto guardarReceta(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("nombre") String nombre,
            @PathVariable("recetaId") Integer recetaId) {
        return servicio.guardarReceta(IdentidadService.id(usuario), nombre, recetaId);
    }
}
