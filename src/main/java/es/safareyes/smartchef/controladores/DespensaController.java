package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.BajaProductoPeticion;
import es.safareyes.smartchef.controladores.dto.DespensaDto;
import es.safareyes.smartchef.controladores.dto.NuevaDespensaPeticion;
import es.safareyes.smartchef.controladores.dto.NuevoProductoPeticion;
import es.safareyes.smartchef.controladores.dto.ProductoDto;
import es.safareyes.smartchef.controladores.dto.RecetaResumenDto;
import es.safareyes.smartchef.servicios.DespensaService;
import jakarta.validation.Valid;
import es.safareyes.smartchef.mapeadores.DespensaMapper;
import es.safareyes.smartchef.mapeadores.RecetaMapper;
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
public class DespensaController {

    private final DespensaService despensaService;
    private final DespensaMapper despensaMapper;
    private final RecetaMapper recetaMapper;

    @GetMapping("/despensas")
    @Transactional(readOnly = true)
    public List<DespensaDto> despensas(@PathVariable("idUsuario") Integer idUsuario) {
        return despensaMapper.despensas(despensaService.despensasDe(idUsuario));
    }

    @PostMapping("/despensas")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public DespensaDto crear(@PathVariable("idUsuario") Integer idUsuario, @RequestBody @Valid NuevaDespensaPeticion peticion) {
        return despensaMapper.despensa(despensaService.crearDespensa(idUsuario, peticion.nombre()));
    }

    @GetMapping("/despensas/{idDespensa}/productos")
    @Transactional(readOnly = true)
    public List<ProductoDto> productos(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idDespensa") Integer idDespensa) {
        despensaService.despensaDelUsuario(idUsuario, idDespensa);
        return despensaMapper.productos(despensaService.productosDisponibles(idDespensa));
    }

    @PostMapping("/despensas/{idDespensa}/productos")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ProductoDto anadirProducto(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idDespensa") Integer idDespensa,
                                      @RequestBody @Valid NuevoProductoPeticion peticion) {
        despensaService.despensaDelUsuario(idUsuario, idDespensa);
        return despensaMapper.producto(despensaService.anadirProducto(idDespensa, peticion.idIngrediente(), peticion.cantidad(),
                peticion.unidad(), peticion.ubicacion(), peticion.fechaCaducidad()));
    }

    @PostMapping("/despensas/{idDespensa}/productos/{idProducto}/baja")
    @Transactional
    public ProductoDto darDeBaja(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idDespensa") Integer idDespensa,
                                 @PathVariable("idProducto") Integer idProducto, @RequestBody @Valid BajaProductoPeticion peticion) {
        despensaService.productoDelUsuario(idUsuario, idDespensa, idProducto);
        return despensaMapper.producto(despensaService.darDeBaja(idProducto, peticion.motivo()));
    }

    @GetMapping("/despensas/{idDespensa}/cocinables")
    @Transactional(readOnly = true)
    public List<RecetaResumenDto> cocinables(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idDespensa") Integer idDespensa) {
        return recetaMapper.resumenes(despensaService.recetasCocinables(idUsuario, idDespensa));
    }

    @GetMapping("/productos/caducan")
    @Transactional(readOnly = true)
    public List<ProductoDto> caducan(@PathVariable("idUsuario") Integer idUsuario,
                                     @RequestParam(name = "dias", defaultValue = "3") int dias) {
        return despensaMapper.productos(despensaService.productosQueCaducan(idUsuario, dias));
    }
}
