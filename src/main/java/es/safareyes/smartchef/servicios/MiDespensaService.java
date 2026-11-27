package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.v1.DespensaPeticion;
import es.safareyes.smartchef.dtos.v1.LineaDespensaDto;
import es.safareyes.smartchef.excepciones.ReglaIncumplidaException;
import es.safareyes.smartchef.mapeadores.DespensaMapper;
import es.safareyes.smartchef.modelos.Despensa;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.catalogos.UbicacionDespensa;
import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;
import es.safareyes.smartchef.repositorios.IDespensaItemRepository;
import es.safareyes.smartchef.repositorios.IDespensaRepository;
import es.safareyes.smartchef.repositorios.IIngredienteRepository;
import es.safareyes.smartchef.repositorios.IUbicacionDespensaRepository;
import es.safareyes.smartchef.repositorios.IUnidadMedidaRepository;
import es.safareyes.smartchef.seguridad.IdentidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MiDespensaService {

    public static final int DIAS_AVISO = 3;
    public static final int DIAS_PARA_LIMPIAR = 7;

    private static final String UBICACION_POR_DEFECTO = "ALACENA";
    private static final String NOMBRE_DESPENSA_POR_DEFECTO = "Casa";

    private final IDespensaRepository despensaRepository;
    private final IDespensaItemRepository itemRepository;
    private final IIngredienteRepository ingredienteRepository;
    private final IUnidadMedidaRepository unidadRepository;
    private final IUbicacionDespensaRepository ubicacionRepository;
    private final IdentidadService identidad;
    private final DespensaMapper despensaMapper;

    public List<LineaDespensaDto> listar(Integer idUsuario) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        LocalDate hoy = Fechas.hoy();
        return despensaMapper.lineas(itemRepository.disponiblesDeUsuario(usuario.getId()), hoy);
    }

    @Transactional
    public Optional<LineaDespensaDto> guardar(Integer idUsuario, Integer idIngrediente, DespensaPeticion peticion) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        Ingrediente ingrediente = ingredienteRepository.findById(idIngrediente)
                .filter(i -> Boolean.TRUE.equals(i.getPublicada()))
                .orElseThrow(() -> new es.safareyes.smartchef.excepciones.RecursoNoEncontradoException(
                        "Ingrediente", idIngrediente));
        List<DespensaItem> actuales = itemRepository.activosDeIngrediente(usuario.getId(), idIngrediente);

        if (peticion.cantidad().signum() == 0) {
            itemRepository.deleteAll(actuales);
            return Optional.empty();
        }

        DespensaItem item = actuales.isEmpty() ? null : actuales.get(0);
        UnidadMedida unidad;
        if (peticion.unidad() != null && !peticion.unidad().isBlank()) {
            String codigo = peticion.unidad().trim().toUpperCase(Locale.ROOT);
            unidad = unidadRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new ReglaIncumplidaException("La unidad '" + codigo + "' no existe"));
        } else if (item != null) {
            unidad = item.getUnidad();
        } else {
            throw new ReglaIncumplidaException("Indica la unidad (G, KG, ML, L, UD...) al añadir un ingrediente nuevo");
        }

        if (item == null) {
            item = new DespensaItem();
            item.setDespensa(despensaDe(usuario));
            item.setIngrediente(ingrediente);
            item.setUbicacion(ubicacionPorDefecto());
            if (peticion.fechaCaducidad() == null && ingrediente.getDiasConservacion() != null) {
                item.setFechaCaducidad(Fechas.hoy().plusDays(ingrediente.getDiasConservacion()));
            }
        } else if (actuales.size() > 1) {
            itemRepository.deleteAll(actuales.subList(1, actuales.size()));
        }
        item.setCantidad(peticion.cantidad());
        item.setUnidad(unidad);
        if (peticion.fechaCaducidad() != null) {
            item.setFechaCaducidad(peticion.fechaCaducidad());
        }
        return Optional.of(despensaMapper.linea(itemRepository.save(item), Fechas.hoy()));
    }

    @Transactional
    public void sumar(Usuario usuario, Ingrediente ingrediente, UnidadMedida unidad, BigDecimal cantidad) {
        List<DespensaItem> actuales = itemRepository.activosDeIngrediente(usuario.getId(), ingrediente.getId());
        for (DespensaItem item : actuales) {
            if (Objects.equals(item.getUnidad().getId(), unidad.getId())) {
                item.setCantidad(item.getCantidad().add(cantidad));
                itemRepository.save(item);
                return;
            }
        }
        DespensaItem nuevo = new DespensaItem();
        nuevo.setDespensa(despensaDe(usuario));
        nuevo.setIngrediente(ingrediente);
        nuevo.setCantidad(cantidad);
        nuevo.setUnidad(unidad);
        nuevo.setUbicacion(ubicacionPorDefecto());
        if (ingrediente.getDiasConservacion() != null) {
            nuevo.setFechaCaducidad(Fechas.hoy().plusDays(ingrediente.getDiasConservacion()));
        }
        itemRepository.save(nuevo);
    }

    @Transactional
    public int limpiarCaducados() {
        return itemRepository.eliminarCaducadosAntesDe(Fechas.hoy().minusDays(DIAS_PARA_LIMPIAR));
    }

    @Transactional
    public Despensa despensaDe(Usuario usuario) {
        return despensaRepository.findFirstByUsuarioIdOrderByIdAsc(usuario.getId()).orElseGet(() -> {
            Despensa nueva = new Despensa();
            nueva.setUsuario(usuario);
            nueva.setNombre(NOMBRE_DESPENSA_POR_DEFECTO);
            return despensaRepository.save(nueva);
        });
    }

    private UbicacionDespensa ubicacionPorDefecto() {
        return ubicacionRepository.findByCodigo(UBICACION_POR_DEFECTO)
                .orElseThrow(() -> new IllegalStateException("Falta la ubicación " + UBICACION_POR_DEFECTO));
    }
}
