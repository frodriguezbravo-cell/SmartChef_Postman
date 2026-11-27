package es.safareyes.smartchef.controladores.v1;

import es.safareyes.smartchef.dtos.v1.InformePopularesDto;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.seguridad.UsuarioAutenticado;
import es.safareyes.smartchef.servicios.InformeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Informes", description = "Informes para el equipo de SmartChef")
@RestController
@RequestMapping("/api/v1/informes")
@RequiredArgsConstructor
public class InformeV1Controller {

    private final InformeService servicio;

    @Operation(summary = "16 · Recetas más cocinadas y más guardadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF')")
    @GetMapping("/recetas-populares")
    public InformePopularesDto recetasPopulares(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(name = "limite", required = false) Integer limite) {
        return servicio.recetasPopulares(IdentidadService.id(usuario), desde, hasta, limite);
    }
}
