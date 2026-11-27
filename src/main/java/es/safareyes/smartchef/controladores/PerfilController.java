package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.AlergiaDto;
import es.safareyes.smartchef.controladores.dto.AlergiaPeticion;
import es.safareyes.smartchef.controladores.dto.DietasPeticion;
import es.safareyes.smartchef.controladores.dto.OpcionCatalogoDto;
import es.safareyes.smartchef.dtos.v1.PreferenciasDto;
import es.safareyes.smartchef.dtos.v1.PreferenciasPeticion;
import es.safareyes.smartchef.servicios.PerfilUsuarioService;
import jakarta.validation.Valid;
import es.safareyes.smartchef.mapeadores.UsuarioMapper;
import es.safareyes.smartchef.mapeadores.CatalogoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN') or #idUsuario == authentication.principal.id")
@RestController
@RequestMapping("/api/usuarios/{idUsuario}")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilUsuarioService perfilService;
    private final UsuarioMapper usuarioMapper;
    private final CatalogoMapper catalogoMapper;

    @GetMapping("/alergias")
    @Transactional(readOnly = true)
    public List<AlergiaDto> alergias(@PathVariable("idUsuario") Integer idUsuario) {
        return usuarioMapper.alergias(perfilService.alergiasDe(idUsuario));
    }

    @PutMapping("/alergias/{codigoAlergeno}")
    @Transactional
    public AlergiaDto anadirAlergia(@PathVariable("idUsuario") Integer idUsuario,
                                    @PathVariable("codigoAlergeno") String codigoAlergeno,
                                    @RequestBody(required = false) @Valid AlergiaPeticion peticion) {
        String gravedad = peticion == null ? null : peticion.gravedad();
        String notas = peticion == null ? null : peticion.notas();
        return usuarioMapper.alergia(perfilService.anadirAlergeno(idUsuario, codigoAlergeno, gravedad, notas));
    }

    @DeleteMapping("/alergias/{codigoAlergeno}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void quitarAlergia(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("codigoAlergeno") String codigoAlergeno) {
        perfilService.quitarAlergeno(idUsuario, codigoAlergeno);
    }

    @GetMapping("/dietas")
    @Transactional(readOnly = true)
    public List<OpcionCatalogoDto> dietas(@PathVariable("idUsuario") Integer idUsuario) {
        return catalogoMapper.opciones(perfilService.dietasDe(idUsuario));
    }

    @PutMapping("/dietas")
    @Transactional
    public List<OpcionCatalogoDto> establecerDietas(@PathVariable("idUsuario") Integer idUsuario,
                                                    @RequestBody @Valid DietasPeticion peticion) {
        return catalogoMapper.opciones(perfilService.establecerDietas(idUsuario, peticion.dietas()).getDietas());
    }

    @GetMapping("/preferencias")
    @Transactional(readOnly = true)
    public PreferenciasDto preferencias(@PathVariable("idUsuario") Integer idUsuario) {
        return perfilService.preferenciasDe(idUsuario);
    }

    @PutMapping("/preferencias")
    @Transactional
    public PreferenciasDto establecerPreferencias(@PathVariable("idUsuario") Integer idUsuario,
                                                  @RequestBody @Valid PreferenciasPeticion peticion) {
        return perfilService.establecerPreferencias(idUsuario, peticion.rapida(), peticion.economica());
    }
}
