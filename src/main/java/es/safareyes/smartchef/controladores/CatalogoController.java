package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.OpcionCatalogoDto;
import es.safareyes.smartchef.servicios.CatalogoService;
import es.safareyes.smartchef.mapeadores.CatalogoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;
    private final CatalogoMapper catalogoMapper;

    @GetMapping("/{nombre}")
    @Transactional(readOnly = true)
    public List<OpcionCatalogoDto> opciones(@PathVariable("nombre") String nombre) {
        return catalogoMapper.opciones(catalogoService.opciones(nombre));
    }
}
