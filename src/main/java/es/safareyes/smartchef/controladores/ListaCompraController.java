package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.AnadirRecetaPeticion;
import es.safareyes.smartchef.controladores.dto.CompradoPeticion;
import es.safareyes.smartchef.controladores.dto.LineaListaDto;
import es.safareyes.smartchef.controladores.dto.ListaDetalleDto;
import es.safareyes.smartchef.controladores.dto.ListaDto;
import es.safareyes.smartchef.controladores.dto.NuevaListaPeticion;
import es.safareyes.smartchef.controladores.dto.TrasladoDto;
import es.safareyes.smartchef.controladores.dto.TrasladoPeticion;
import es.safareyes.smartchef.servicios.ListaCompraService;
import jakarta.validation.Valid;
import es.safareyes.smartchef.mapeadores.ListaCompraMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN') or #idUsuario == authentication.principal.id")
@RestController
@RequestMapping("/api/usuarios/{idUsuario}/listas-compra")
@RequiredArgsConstructor
public class ListaCompraController {

    private final ListaCompraService listaService;
    private final ListaCompraMapper listaCompraMapper;

    @GetMapping
    @Transactional(readOnly = true)
    public List<ListaDto> listas(@PathVariable("idUsuario") Integer idUsuario) {
        return listaCompraMapper.listas(listaService.listasDe(idUsuario));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ListaDto crear(@PathVariable("idUsuario") Integer idUsuario, @RequestBody @Valid NuevaListaPeticion peticion) {
        return listaCompraMapper.lista(listaService.crearLista(idUsuario, peticion.nombre()));
    }

    @GetMapping("/{idLista}")
    @Transactional(readOnly = true)
    public ListaDetalleDto detalle(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idLista") Integer idLista) {
        return listaCompraMapper.detalle(listaService.listaDelUsuario(idUsuario, idLista));
    }

    @PostMapping("/{idLista}/recetas")
    @Transactional
    public ListaDetalleDto anadirReceta(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idLista") Integer idLista,
                                        @RequestBody @Valid AnadirRecetaPeticion peticion) {
        listaService.listaDelUsuario(idUsuario, idLista);
        return listaCompraMapper.detalle(listaService.anadirReceta(idLista, peticion.idReceta(), peticion.raciones(), peticion.idDespensa()));
    }

    @PatchMapping("/{idLista}/lineas/{idLinea}")
    @Transactional
    public LineaListaDto marcarComprado(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idLista") Integer idLista,
                                        @PathVariable("idLinea") Integer idLinea, @RequestBody @Valid CompradoPeticion peticion) {
        listaService.lineaDeLista(idUsuario, idLista, idLinea);
        return listaCompraMapper.linea(listaService.marcarComprado(idLinea, peticion.comprado()));
    }

    @PostMapping("/{idLista}/trasladar-comprados")
    @Transactional
    public TrasladoDto trasladarComprados(@PathVariable("idUsuario") Integer idUsuario, @PathVariable("idLista") Integer idLista,
                                          @RequestBody @Valid TrasladoPeticion peticion) {
        listaService.listaDelUsuario(idUsuario, idLista);
        return new TrasladoDto(listaService.pasarCompradosADespensa(idLista, peticion.idDespensa()));
    }
}
