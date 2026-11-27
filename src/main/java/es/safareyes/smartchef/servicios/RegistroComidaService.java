package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.CaloriasDiaView;
import es.safareyes.smartchef.dtos.ResumenNutricional;
import es.safareyes.smartchef.dtos.v1.DiaSemanaDto;
import es.safareyes.smartchef.dtos.v1.RegistroDto;
import es.safareyes.smartchef.dtos.v1.RegistroPeticion;
import es.safareyes.smartchef.dtos.v1.SemanaDto;
import es.safareyes.smartchef.excepciones.ConflictoException;
import es.safareyes.smartchef.excepciones.ReglaIncumplidaException;
import es.safareyes.smartchef.mapeadores.RegistroMapper;
import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.RegistroComida;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.repositorios.IDespensaItemRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.IRegistroComidaRepository;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.servicios.calculos.CalculadoraDescuento;
import es.safareyes.smartchef.servicios.calculos.CalculadoraNutricion;
import es.safareyes.smartchef.servicios.calculos.Escalador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistroComidaService {

    private static final int MAX_RACIONES = 100;

    private final IRegistroComidaRepository registroRepository;
    private final IRecetaRepository recetaRepository;
    private final IDespensaItemRepository despensaItemRepository;
    private final IdentidadService identidad;
    private final RegistroMapper registroMapper;

    @Transactional
    public RegistroDto registrar(Integer idUsuario, RegistroPeticion p) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        LocalDate hoy = Fechas.hoy();
        LocalDate fecha = p.fecha() == null ? hoy : p.fecha();
        if (fecha.isAfter(hoy)) {
            throw new ReglaIncumplidaException("No se pueden registrar comidas de fechas futuras");
        }
        boolean hayReceta = p.idReceta() != null;
        boolean hayDescripcion = p.descripcion() != null && !p.descripcion().isBlank();
        if (!hayReceta && !hayDescripcion) {
            throw new ReglaIncumplidaException("Indica la receta cocinada o una descripción de la comida");
        }
        int raciones = p.raciones() == null ? 1 : p.raciones();
        if (raciones > MAX_RACIONES) {
            throw new ReglaIncumplidaException("Las raciones no pueden ser más de " + MAX_RACIONES);
        }
        boolean descontar = Boolean.TRUE.equals(p.descontarDespensa());
        if (descontar && !hayReceta) {
            throw new ReglaIncumplidaException("Solo se puede descontar de la despensa si se indica una receta");
        }
        if (registroRepository.existsByUsuarioIdAndFechaAndMomento(usuario.getId(), fecha, p.momento())) {
            throw new ConflictoException("Ya hay un registro el " + fecha + " para " + p.momento()
                    + ". Solo se permite uno por día y momento");
        }

        RegistroComida registro = new RegistroComida();
        registro.setUsuario(usuario);
        registro.setFecha(fecha);
        registro.setMomento(p.momento());
        registro.setRaciones((short) raciones);
        registro.setDescripcionLibre(hayDescripcion ? p.descripcion().trim() : null);
        registro.setCaloriasKcal(p.calorias());

        if (hayReceta) {
            Receta receta = Comprobaciones.existe(recetaRepository.findConIngredientesById(p.idReceta()),
                    "Receta", p.idReceta());
            if (!Boolean.TRUE.equals(receta.getPublicada())) {
                throw new es.safareyes.smartchef.excepciones.RecursoNoEncontradoException("Receta", p.idReceta());
            }
            if (Boolean.TRUE.equals(receta.getRetirada())) {
                throw new ConflictoException("La receta está retirada y no se puede registrar");
            }
            registro.setReceta(receta);
            ResumenNutricional nutricion = CalculadoraNutricion.paraRaciones(receta, raciones);
            boolean hayCalorias = Boolean.TRUE.equals(nutricion.getCompleto()) || nutricion.getCaloriasKcal().signum() > 0;
            registro.setCaloriasKcal(hayCalorias
                    ? nutricion.getCaloriasKcal().setScale(0, RoundingMode.HALF_UP).intValue() : p.calorias());
            if (descontar) {
                descontarDeLaDespensa(usuario, receta, raciones, hoy);
            }
        }
        return registroMapper.registro(registroRepository.save(registro));
    }

    public SemanaDto semana(Integer idUsuario, LocalDate fecha) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        LocalDate referencia = fecha == null ? Fechas.hoy() : fecha;
        LocalDate lunes = referencia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = lunes.plusDays(6);

        Map<LocalDate, Long> calorias = new HashMap<>();
        for (CaloriasDiaView v : registroRepository.caloriasPorDia(usuario.getId(), referencia)) {
            calorias.put(LocalDate.parse(v.getFecha()), v.getCalorias());
        }
        Map<LocalDate, List<RegistroDto>> comidas = new HashMap<>();
        for (RegistroComida r : registroRepository
                .findByUsuarioIdAndFechaBetweenOrderByFechaAsc(usuario.getId(), lunes, domingo)) {
            comidas.computeIfAbsent(r.getFecha(), f -> new ArrayList<>()).add(registroMapper.registro(r));
        }

        List<DiaSemanaDto> dias = new ArrayList<>();
        long total = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate dia = lunes.plusDays(i);
            long kcal = calorias.getOrDefault(dia, 0L);
            total += kcal;
            List<RegistroDto> delDia = new ArrayList<>(comidas.getOrDefault(dia, List.of()));
            delDia.sort(java.util.Comparator.comparing(RegistroDto::momento));
            dias.add(new DiaSemanaDto(dia, kcal, delDia));
        }
        return new SemanaDto(lunes, domingo, total, dias);
    }

    private void descontarDeLaDespensa(Usuario usuario, Receta receta, int raciones, LocalDate hoy) {
        List<DespensaItem> disponibles = despensaItemRepository.disponiblesDeUsuario(usuario.getId()).stream()
                .filter(i -> i.getFechaCaducidad() == null || !i.getFechaCaducidad().isBefore(hoy))
                .toList();
        BigDecimal factor = Escalador.factor(receta, raciones);
        List<DespensaItem> agotados = CalculadoraDescuento.descontar(receta.getIngredientes(), factor, disponibles);
        despensaItemRepository.deleteAll(agotados);
    }
}
