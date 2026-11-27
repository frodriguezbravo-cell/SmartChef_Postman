package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.v1.ListaCompraDto;
import es.safareyes.smartchef.dtos.v1.ListaDesdeRecetaPeticion;
import es.safareyes.smartchef.excepciones.AccesoDenegadoException;
import es.safareyes.smartchef.excepciones.ConflictoException;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.mapeadores.ListaCompraMapper;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.ListaCompra;
import es.safareyes.smartchef.modelos.ListaCompraItem;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.repositorios.IDespensaItemRepository;
import es.safareyes.smartchef.repositorios.IListaCompraRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.servicios.calculos.CalculadoraCompra;
import es.safareyes.smartchef.servicios.calculos.Escalador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MiListaCompraService {

    private static final String NOMBRE_POR_DEFECTO = "Lista de la compra";

    private final IListaCompraRepository listaRepository;
    private final IRecetaRepository recetaRepository;
    private final IDespensaItemRepository despensaItemRepository;
    private final MiDespensaService despensaService;
    private final IdentidadService identidad;
    private final ListaCompraMapper listaCompraMapper;
    private final NotificacionService notificacionService;

    @Transactional
    public ListaCompraDto desdeReceta(Integer idUsuario, ListaDesdeRecetaPeticion peticion) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        Receta receta = Comprobaciones.existe(recetaRepository.findConIngredientesById(peticion.idReceta()),
                "Receta", peticion.idReceta());
        if (!Boolean.TRUE.equals(receta.getPublicada())) {
            throw new RecursoNoEncontradoException("Receta", peticion.idReceta());
        }
        if (Boolean.TRUE.equals(receta.getRetirada())) {
            throw new ConflictoException("La receta está retirada y no se puede añadir a la lista");
        }

        ListaCompra lista = listaRepository.findFirstByUsuarioIdAndCompletadaFalseOrderByIdAsc(usuario.getId())
                .orElseGet(() -> {
                    ListaCompra nueva = new ListaCompra();
                    nueva.setUsuario(usuario);
                    nueva.setNombre(NOMBRE_POR_DEFECTO);
                    return listaRepository.save(nueva);
                });

        LocalDate hoy = Fechas.hoy();
        List<DespensaItem> enDespensa = despensaItemRepository.disponiblesDeUsuario(usuario.getId()).stream()
                .filter(i -> i.getFechaCaducidad() == null || !i.getFechaCaducidad().isBefore(hoy))
                .toList();

        BigDecimal factor = Escalador.factor(receta, peticion.raciones());
        for (CalculadoraCompra.Faltante faltante :
                CalculadoraCompra.faltantes(receta.getIngredientes(), factor, enDespensa)) {
            ListaCompraService.fusionar(lista, faltante, receta);
        }
        ListaCompra guardada = listaRepository.saveAndFlush(lista);
        notificacionService.avisarListaActualizada(usuario, guardada, receta);
        return listaCompraMapper.listaCompra(guardada);
    }

    @Transactional
    public ListaCompraDto cerrar(Integer idUsuario, Integer idLista) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        ListaCompra lista = Comprobaciones.existe(listaRepository.findById(idLista), "Lista de la compra", idLista);
        if (!lista.getUsuario().getId().equals(usuario.getId())) {
            throw new AccesoDenegadoException("Esa lista de la compra no es tuya");
        }
        if (Boolean.TRUE.equals(lista.getCompletada())) {
            throw new ConflictoException("La lista ya está cerrada");
        }
        for (ListaCompraItem linea : lista.getItems()) {
            boolean trasladable = Boolean.TRUE.equals(linea.getComprado()) && linea.getIngrediente() != null
                    && linea.getCantidad() != null && linea.getUnidad() != null;
            if (trasladable) {
                despensaService.sumar(usuario, linea.getIngrediente(), linea.getUnidad(), linea.getCantidad());
            }
        }
        lista.setCompletada(true);
        return listaCompraMapper.listaCompra(listaRepository.saveAndFlush(lista));
    }
}
