package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.AlergiaDto;
import es.safareyes.smartchef.controladores.dto.RecetaDetalleDto;
import es.safareyes.smartchef.controladores.dto.RecetaResumenDto;
import es.safareyes.smartchef.dtos.IngredienteEscalado;
import es.safareyes.smartchef.dtos.ResumenNutricional;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.servicios.RecetaService;
import es.safareyes.smartchef.mapeadores.RecetaMapper;
import es.safareyes.smartchef.mapeadores.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecetaController {

    private final RecetaService recetaService;
    private final RecetaMapper recetaMapper;
    private final UsuarioMapper usuarioMapper;

    @GetMapping("/recetas")
    @Transactional(readOnly = true)
    public List<RecetaResumenDto> buscar(@RequestParam("titulo") String titulo) {
        return recetaMapper.resumenes(recetaService.buscarPorTitulo(titulo));
    }

    @GetMapping("/recetas/rapidas")
    @Transactional(readOnly = true)
    public List<RecetaResumenDto> rapidas(@RequestParam(name = "minutos", defaultValue = "30") int minutos) {
        return recetaMapper.resumenes(recetaService.recetasRapidas(minutos));
    }

    @GetMapping("/recetas/{idReceta}")
    @Transactional(readOnly = true)
    public RecetaDetalleDto detalle(@PathVariable("idReceta") Integer idReceta,
                                    @RequestParam(name = "raciones", required = false) Integer raciones) {
        Receta receta = recetaService.obtenerPorId(idReceta);
        int solicitadas = raciones == null ? receta.getRaciones() : raciones;
        List<IngredienteEscalado> ingredientes = recetaService.ingredientesPara(idReceta, solicitadas);
        ResumenNutricional nutricion = recetaService.nutricion(idReceta, solicitadas);
        return recetaMapper.detalle(receta, solicitadas, ingredientes, nutricion);
    }

    @PreAuthorize("hasRole('ADMIN') or #idUsuario == authentication.principal.id")
    @GetMapping("/usuarios/{idUsuario}/recetas-seguras")
    @Transactional(readOnly = true)
    public List<RecetaResumenDto> seguras(@PathVariable("idUsuario") Integer idUsuario) {
        return recetaMapper.resumenes(recetaService.recetasSegurasPara(idUsuario));
    }

    @PreAuthorize("hasRole('ADMIN') or #idUsuario == authentication.principal.id")
    @GetMapping("/usuarios/{idUsuario}/recetas/{idReceta}/avisos")
    @Transactional(readOnly = true)
    public List<AlergiaDto> avisos(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idReceta") Integer idReceta) {
        return usuarioMapper.alergias(recetaService.avisosDeAlergenos(idUsuario, idReceta));
    }
}
