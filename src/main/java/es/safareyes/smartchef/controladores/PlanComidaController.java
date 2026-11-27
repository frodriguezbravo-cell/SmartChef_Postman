package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.ComidaDto;
import es.safareyes.smartchef.controladores.dto.NuevaComidaPeticion;
import es.safareyes.smartchef.dtos.ResumenDia;
import es.safareyes.smartchef.servicios.PlanComidaService;
import es.safareyes.smartchef.servicios.Fechas;
import jakarta.validation.Valid;
import es.safareyes.smartchef.mapeadores.PlanComidaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@PreAuthorize("hasRole('ADMIN') or #idUsuario == authentication.principal.id")
@RestController
@RequestMapping("/api/usuarios/{idUsuario}/plan-comidas")
@RequiredArgsConstructor
public class PlanComidaController {

    private final PlanComidaService planService;
    private final PlanComidaMapper planComidaMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ComidaDto planificar(@PathVariable("idUsuario") Integer idUsuario, @RequestBody @Valid NuevaComidaPeticion peticion) {
        return planComidaMapper.comida(planService.planificar(idUsuario, peticion.fecha(), peticion.tipoComida(), peticion.idReceta(),
                peticion.descripcion(), peticion.raciones(), peticion.horaAviso()));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ComidaDto> semana(@PathVariable("idUsuario") Integer idUsuario,
                                  @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde) {
        LocalDate inicio = desde == null ? Fechas.hoy() : desde;
        return planComidaMapper.comidas(planService.planSemana(idUsuario, inicio));
    }

    @GetMapping("/resumen")
    @Transactional(readOnly = true)
    public ResumenDia resumen(@PathVariable("idUsuario") Integer idUsuario,
                              @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return planService.resumenDia(idUsuario, fecha == null ? Fechas.hoy() : fecha);
    }

    @PostMapping("/{idPlan}/completar")
    @Transactional
    public ComidaDto completar(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idPlan") Integer idPlan) {
        planService.comidaDelUsuario(idUsuario, idPlan);
        return planComidaMapper.comida(planService.marcarCompletada(idPlan));
    }
}
