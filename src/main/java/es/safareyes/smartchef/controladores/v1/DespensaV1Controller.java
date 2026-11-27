package es.safareyes.smartchef.controladores.v1;

import es.safareyes.smartchef.dtos.v1.DespensaPeticion;
import es.safareyes.smartchef.dtos.v1.LineaDespensaDto;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.seguridad.UsuarioAutenticado;
import es.safareyes.smartchef.servicios.MiDespensaService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Despensa", description = "Mi despensa")
@RestController
@RequestMapping("/api/v1/despensa")
@RequiredArgsConstructor
public class DespensaV1Controller {

    private final MiDespensaService servicio;

    @Operation(summary = "8 · Mi despensa, con los próximos a caducar primero")
    @GetMapping
    public List<LineaDespensaDto> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicio.listar(IdentidadService.id(usuario));
    }

    @Operation(summary = "9 · Añadir o actualizar un ingrediente (cantidad 0 lo elimina)")
    @PutMapping("/{ingredienteId}")
    public ResponseEntity<LineaDespensaDto> guardar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("ingredienteId") Integer ingredienteId,
            @RequestBody @Valid DespensaPeticion peticion) {
        return servicio.guardar(IdentidadService.id(usuario), ingredienteId, peticion)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
