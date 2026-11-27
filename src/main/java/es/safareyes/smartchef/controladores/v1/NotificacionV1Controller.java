package es.safareyes.smartchef.controladores.v1;

import es.safareyes.smartchef.dtos.v1.NotificacionDto;
import es.safareyes.smartchef.dtos.v1.PaginaDto;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.seguridad.UsuarioAutenticado;
import es.safareyes.smartchef.servicios.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notificaciones", description = "Avisos de la aplicación")
@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionV1Controller {

    private final NotificacionService servicio;

    @Operation(summary = "Mis notificaciones, paginadas y filtrables por leida")
    @GetMapping
    public PaginaDto<NotificacionDto> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(name = "leida", required = false) Boolean leida,
            @RequestParam(name = "pagina", required = false) Integer pagina,
            @RequestParam(name = "tamano", required = false) Integer tamano) {
        return servicio.listar(IdentidadService.id(usuario), leida, pagina, tamano);
    }

    @Operation(summary = "Marcar una notificación como leída")
    @PatchMapping("/{id}/leida")
    public NotificacionDto marcarLeida(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") Integer id) {
        return servicio.marcarLeida(IdentidadService.id(usuario), id);
    }
}
