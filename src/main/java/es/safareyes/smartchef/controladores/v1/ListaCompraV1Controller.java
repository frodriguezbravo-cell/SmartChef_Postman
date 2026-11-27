package es.safareyes.smartchef.controladores.v1;

import es.safareyes.smartchef.dtos.v1.ListaCompraDto;
import es.safareyes.smartchef.dtos.v1.ListaDesdeRecetaPeticion;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.seguridad.UsuarioAutenticado;
import es.safareyes.smartchef.servicios.MiListaCompraService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Listas de la compra", description = "Lista de la compra abierta")
@RestController
@RequestMapping("/api/v1/listas-compra")
@RequiredArgsConstructor
public class ListaCompraV1Controller {

    private final MiListaCompraService servicio;

    @Operation(summary = "10 · Añadir a la lista abierta lo que falta de una receta")
    @PostMapping("/desde-receta")
    public ListaCompraDto desdeReceta(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody @Valid ListaDesdeRecetaPeticion peticion) {
        return servicio.desdeReceta(IdentidadService.id(usuario), peticion);
    }

    @Operation(summary = "11 · Cerrar la lista y pasar lo comprado a la despensa")
    @PatchMapping("/{id}/cerrar")
    public ListaCompraDto cerrar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") Integer id) {
        return servicio.cerrar(IdentidadService.id(usuario), id);
    }
}
