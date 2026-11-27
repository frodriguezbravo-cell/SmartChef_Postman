package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.excepciones.ConflictoException;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.modelos.Despensa;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.ListaCompra;
import es.safareyes.smartchef.modelos.ListaCompraItem;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.catalogos.UbicacionDespensa;
import es.safareyes.smartchef.repositorios.IDespensaItemRepository;
import es.safareyes.smartchef.repositorios.IDespensaRepository;
import es.safareyes.smartchef.repositorios.IListaCompraItemRepository;
import es.safareyes.smartchef.repositorios.IListaCompraRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.IUbicacionDespensaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import es.safareyes.smartchef.servicios.calculos.CalculadoraCompra;
import es.safareyes.smartchef.servicios.calculos.Escalador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListaCompraService {

    private static final String UBICACION_POR_DEFECTO = "ALACENA";

    private final IListaCompraRepository listaRepository;
    private final IListaCompraItemRepository itemRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IRecetaRepository recetaRepository;
    private final IDespensaRepository despensaRepository;
    private final IDespensaItemRepository despensaItemRepository;
    private final IUbicacionDespensaRepository ubicacionRepository;

    public List<ListaCompra> listasDe(Integer idUsuario) {
        return listaRepository.findByUsuarioIdOrderByCreadoEnDesc(idUsuario);
    }

    @Transactional
    public ListaCompra crearLista(Integer idUsuario, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("La lista necesita un nombre");
        }
        Usuario usuario = Comprobaciones.existe(usuarioRepository.findById(idUsuario), "Usuario", idUsuario);
        if (listaRepository.findFirstByUsuarioIdAndCompletadaFalseOrderByIdAsc(idUsuario).isPresent()) {
            throw new ConflictoException("Ya tienes una lista de la compra abierta: ciérrala antes de crear otra");
        }
        ListaCompra lista = new ListaCompra();
        lista.setUsuario(usuario);
        lista.setNombre(nombre.trim());
        return listaRepository.save(lista);
    }

    @Transactional
    public ListaCompra anadirReceta(Integer idLista, Integer idReceta, int raciones, Integer idDespensa) {
        if (raciones <= 0) {
            throw new ReglaNegocioException("Las raciones deben ser mayores que 0");
        }
        ListaCompra lista = Comprobaciones.existe(listaRepository.findById(idLista), "Lista de la compra", idLista);
        exigirAbierta(lista);
        Receta receta = Comprobaciones.existe(recetaRepository.findById(idReceta), "Receta", idReceta);

        List<DespensaItem> enDespensa = List.of();
        if (idDespensa != null) {
            Despensa despensa = despensaDelMismoUsuario(idDespensa, lista);
            enDespensa = despensaItemRepository.findByDespensaIdAndFechaBajaIsNull(despensa.getId());
        }

        BigDecimal factor = Escalador.factor(receta, raciones);
        for (CalculadoraCompra.Faltante faltante : CalculadoraCompra.faltantes(receta.getIngredientes(), factor, enDespensa)) {
            fusionar(lista, faltante, receta);
        }
        return listaRepository.save(lista);
    }

    @Transactional
    public ListaCompraItem marcarComprado(Integer idItem, boolean comprado) {
        ListaCompraItem item = Comprobaciones.existe(itemRepository.findById(idItem), "Línea de la lista", idItem);
        item.setComprado(comprado);
        return itemRepository.save(item);
    }

    @Transactional
    public int pasarCompradosADespensa(Integer idLista, Integer idDespensa) {
        ListaCompra lista = Comprobaciones.existe(listaRepository.findById(idLista), "Lista de la compra", idLista);
        exigirAbierta(lista);
        Despensa despensa = despensaDelMismoUsuario(idDespensa, lista);
        UbicacionDespensa ubicacion = Comprobaciones.existe(
                ubicacionRepository.findByCodigo(UBICACION_POR_DEFECTO), "Ubicación", UBICACION_POR_DEFECTO);

        List<ListaCompraItem> trasladados = new ArrayList<>();
        for (ListaCompraItem linea : lista.getItems()) {
            boolean trasladable = Boolean.TRUE.equals(linea.getComprado()) && linea.getIngrediente() != null
                    && linea.getCantidad() != null && linea.getUnidad() != null;
            if (!trasladable) {
                continue;
            }
            DespensaItem producto = new DespensaItem();
            producto.setDespensa(despensa);
            producto.setIngrediente(linea.getIngrediente());
            producto.setCantidad(linea.getCantidad());
            producto.setUnidad(linea.getUnidad());
            producto.setUbicacion(ubicacion);
            if (linea.getIngrediente().getDiasConservacion() != null) {
                producto.setFechaCaducidad(Fechas.hoy().plusDays(linea.getIngrediente().getDiasConservacion()));
            }
            despensaItemRepository.save(producto);
            trasladados.add(linea);
        }

        if (!trasladados.isEmpty()) {
            lista.getItems().removeAll(trasladados);
            boolean quedanPendientes = lista.getItems().stream()
                    .anyMatch(linea -> !Boolean.TRUE.equals(linea.getComprado()));
            lista.setCompletada(!quedanPendientes);
            listaRepository.save(lista);
        }
        return trasladados.size();
    }

    private static void exigirAbierta(ListaCompra lista) {
        if (Boolean.TRUE.equals(lista.getCompletada())) {
            throw new ConflictoException("La lista ya está cerrada");
        }
    }

    private Despensa despensaDelMismoUsuario(Integer idDespensa, ListaCompra lista) {
        Despensa despensa = Comprobaciones.existe(despensaRepository.findById(idDespensa), "Despensa", idDespensa);
        if (!despensa.getUsuario().getId().equals(lista.getUsuario().getId())) {
            throw new ReglaNegocioException("La despensa no pertenece al dueño de la lista");
        }
        return despensa;
    }

    static void fusionar(ListaCompra lista, CalculadoraCompra.Faltante faltante, Receta receta) {
        for (ListaCompraItem existente : lista.getItems()) {
            boolean mismaLinea = !Boolean.TRUE.equals(existente.getComprado())
                    && existente.getIngrediente() != null
                    && Objects.equals(existente.getIngrediente().getId(), faltante.ingrediente().getId())
                    && Objects.equals(idUnidad(existente), faltante.unidad() == null ? null : faltante.unidad().getId());
            if (mismaLinea) {
                if (faltante.cantidad() != null) {
                    existente.setCantidad(existente.getCantidad() == null
                            ? faltante.cantidad()
                            : existente.getCantidad().add(faltante.cantidad()));
                }
                return;
            }
        }
        ListaCompraItem nueva = new ListaCompraItem();
        nueva.setLista(lista);
        nueva.setIngrediente(faltante.ingrediente());
        nueva.setCantidad(faltante.cantidad());
        nueva.setUnidad(faltante.unidad());
        nueva.setReceta(receta);
        lista.getItems().add(nueva);
    }

    private static Short idUnidad(ListaCompraItem linea) {
        return linea.getUnidad() == null ? null : linea.getUnidad().getId();
    }

    public ListaCompra listaDelUsuario(Integer idUsuario, Integer idLista) {
        ListaCompra lista = Comprobaciones.existe(listaRepository.findById(idLista), "Lista de la compra", idLista);
        if (!lista.getUsuario().getId().equals(idUsuario)) {
            throw new RecursoNoEncontradoException("Lista de la compra", idLista);
        }
        return lista;
    }

    public ListaCompraItem lineaDeLista(Integer idUsuario, Integer idLista, Integer idLinea) {
        ListaCompra lista = listaDelUsuario(idUsuario, idLista);
        ListaCompraItem linea = Comprobaciones.existe(itemRepository.findById(idLinea), "Línea de la lista", idLinea);
        if (!linea.getLista().getId().equals(lista.getId())) {
            throw new RecursoNoEncontradoException("Línea de la lista", idLinea);
        }
        return linea;
    }
}
