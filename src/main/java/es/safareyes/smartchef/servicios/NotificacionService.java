package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.v1.NotificacionDto;
import es.safareyes.smartchef.dtos.v1.PaginaDto;
import es.safareyes.smartchef.excepciones.AccesoDenegadoException;
import es.safareyes.smartchef.mapeadores.NotificacionMapper;
import es.safareyes.smartchef.modelos.Coleccion;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.ListaCompra;
import es.safareyes.smartchef.modelos.Notificacion;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.RecursoNotificacion;
import es.safareyes.smartchef.modelos.RegistroComida;
import es.safareyes.smartchef.modelos.TipoNotificacion;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.repositorios.IColeccionRepository;
import es.safareyes.smartchef.repositorios.IDespensaItemRepository;
import es.safareyes.smartchef.repositorios.INotificacionRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.IRegistroComidaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import es.safareyes.smartchef.seguridad.IdentidadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificacionService {

    public static final int DIAS_AVISO_CADUCIDAD = 2;

    private static final Map<String, String> ORDEN = Map.of("fecha", "creadaEn");

    private final INotificacionRepository notificacionRepository;
    private final IDespensaItemRepository despensaItemRepository;
    private final IRecetaRepository recetaRepository;
    private final IColeccionRepository coleccionRepository;
    private final IRegistroComidaRepository registroRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IdentidadService identidad;
    private final NotificacionMapper notificacionMapper;

    public PaginaDto<NotificacionDto> listar(Integer idUsuario, Boolean leida, Integer pagina, Integer tamano) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        PageRequest pageable = Paginas.de(pagina, tamano, "fecha,desc", ORDEN, "creadaEn");
        Page<Notificacion> resultado = leida == null
                ? notificacionRepository.findByDestinatarioId(usuario.getId(), pageable)
                : notificacionRepository.findByDestinatarioIdAndLeida(usuario.getId(), leida, pageable);
        Map<String, Object> filtros = new LinkedHashMap<>();
        if (leida != null) {
            filtros.put("leida", leida);
        }
        return new PaginaDto<>(notificacionMapper.notificaciones(resultado.getContent()), resultado.getNumber(),
                resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages(), filtros);
    }

    @Transactional
    public NotificacionDto marcarLeida(Integer idUsuario, Integer idNotificacion) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        Notificacion notificacion = Comprobaciones.existe(notificacionRepository.findById(idNotificacion),
                "Notificación", idNotificacion);
        if (!notificacion.getDestinatario().getId().equals(usuario.getId())) {
            throw new AccesoDenegadoException("Esa notificación no es tuya");
        }
        notificacion.setLeida(true);
        return notificacionMapper.notificacion(notificacionRepository.save(notificacion));
    }

    @Transactional
    public void crear(Usuario destinatario, TipoNotificacion tipo, String mensaje,
                      RecursoNotificacion recurso, Integer recursoId) {
        Notificacion notificacion = new Notificacion();
        notificacion.setDestinatario(destinatario);
        notificacion.setTipo(tipo);
        notificacion.setMensaje(mensaje.length() > 500 ? mensaje.substring(0, 500) : mensaje);
        notificacion.setRecurso(recurso);
        notificacion.setRecursoId(recursoId);
        notificacion.setLeida(false);
        notificacionRepository.save(notificacion);
        log.info("Notificación {} para el usuario {}: {}", tipo, destinatario.getId(), mensaje);
    }

    @Transactional
    public int avisarCaducidades() {
        LocalDate hoy = Fechas.hoy();
        int creadas = 0;
        for (DespensaItem item : despensaItemRepository
                .findByFechaBajaIsNullAndFechaCaducidadBetween(hoy, hoy.plusDays(DIAS_AVISO_CADUCIDAD))) {
            Usuario usuario = item.getDespensa().getUsuario();
            if (notificacionRepository.existsByDestinatarioIdAndTipoAndRecursoAndRecursoId(usuario.getId(),
                    TipoNotificacion.CADUCIDAD_PROXIMA, RecursoNotificacion.DESPENSA, item.getId())) {
                continue;
            }
            String ingrediente = item.getIngrediente().getNombre();
            StringBuilder mensaje = new StringBuilder(ingrediente)
                    .append(" caduca el ").append(item.getFechaCaducidad()).append('.');
            Optional<Receta> sugerencia = recetaRepository
                    .findFirstByIngredientesIngredienteIdAndPublicadaTrueAndRetiradaFalseOrderByTituloAsc(
                            item.getIngrediente().getId());
            sugerencia.ifPresent(r -> mensaje.append(" Puedes usarlo en: ").append(r.getTitulo()).append('.'));
            crear(usuario, TipoNotificacion.CADUCIDAD_PROXIMA, mensaje.toString(),
                    RecursoNotificacion.DESPENSA, item.getId());
            creadas++;
        }
        return creadas;
    }

    @Transactional
    public int resumenSemanal() {
        LocalDate lunes = Fechas.hoy().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        LocalDate domingo = lunes.plusDays(6);
        Integer semana = Integer.valueOf(lunes.format(DateTimeFormatter.BASIC_ISO_DATE));
        int creadas = 0;
        for (Usuario usuario : usuarioRepository.findByActivoTrue()) {
            if (notificacionRepository.existsByDestinatarioIdAndTipoAndRecursoAndRecursoId(usuario.getId(),
                    TipoNotificacion.RESUMEN_SEMANAL, RecursoNotificacion.SEMANA, semana)) {
                continue;
            }
            List<RegistroComida> registros = registroRepository
                    .findByUsuarioIdAndFechaBetweenOrderByFechaAsc(usuario.getId(), lunes, domingo);
            if (registros.isEmpty()) {
                continue;
            }
            long calorias = registros.stream().map(RegistroComida::getCaloriasKcal).filter(Objects::nonNull)
                    .mapToLong(Integer::longValue).sum();
            String mensaje = "Semana del " + lunes + " al " + domingo + ": " + registros.size()
                    + " comidas registradas y " + calorias + " kcal en total.";
            crear(usuario, TipoNotificacion.RESUMEN_SEMANAL, mensaje, RecursoNotificacion.SEMANA, semana);
            creadas++;
        }
        return creadas;
    }

    @Transactional
    public void avisarRecetaRetirada(Receta receta) {
        Map<Integer, Coleccion> porUsuario = new LinkedHashMap<>();
        for (Coleccion coleccion : coleccionRepository.findByRecetasId(receta.getId())) {
            porUsuario.putIfAbsent(coleccion.getUsuario().getId(), coleccion);
        }
        for (Coleccion coleccion : porUsuario.values()) {
            crear(coleccion.getUsuario(), TipoNotificacion.RECETA_RETIRADA,
                    "La receta \"" + receta.getTitulo() + "\" de tu colección \"" + coleccion.getNombre()
                            + "\" ha sido retirada.",
                    RecursoNotificacion.RECETA, receta.getId());
        }
    }

    @Transactional
    public void avisarListaActualizada(Usuario usuario, ListaCompra lista, Receta receta) {
        crear(usuario, TipoNotificacion.LISTA_ACTUALIZADA,
                "Tu lista \"" + lista.getNombre() + "\" se ha actualizado con lo que falta para \""
                        + receta.getTitulo() + "\" (" + lista.getItems().size() + " líneas).",
                RecursoNotificacion.LISTA_COMPRA, lista.getId());
    }
}
