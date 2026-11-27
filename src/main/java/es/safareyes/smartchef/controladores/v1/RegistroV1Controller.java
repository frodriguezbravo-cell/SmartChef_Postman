package es.safareyes.smartchef.controladores.v1;

import es.safareyes.smartchef.dtos.v1.RegistroDto;
import es.safareyes.smartchef.dtos.v1.RegistroPeticion;
import es.safareyes.smartchef.dtos.v1.SemanaDto;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.seguridad.UsuarioAutenticado;
import es.safareyes.smartchef.servicios.RegistroComidaService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;

@Tag(name = "Registros", description = "Historial de lo cocinado")
@RestController
@RequestMapping("/api/v1/registros")
@RequiredArgsConstructor
public class RegistroV1Controller {

    private final RegistroComidaService servicio;

    @Operation(summary = "14 · Registrar lo cocinado")
    @PostMapping
    public ResponseEntity<RegistroDto> registrar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody @Valid RegistroPeticion peticion) {
        RegistroDto creado = servicio.registrar(IdentidadService.id(usuario), peticion);
        return ResponseEntity.created(URI.create("/api/v1/registros/" + creado.id())).body(creado);
    }

    @Operation(summary = "15 · Semana por días con calorías totales")
    @GetMapping("/semana")
    public SemanaDto semana(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {
        return servicio.semana(IdentidadService.id(usuario), fecha);
    }
}
