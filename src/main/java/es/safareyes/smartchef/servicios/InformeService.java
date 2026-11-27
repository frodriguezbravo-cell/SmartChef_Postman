package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.v1.InformePopularesDto;
import es.safareyes.smartchef.dtos.v1.RecetaPopularDto;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.mapeadores.InformeMapper;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.repositorios.IRegistroComidaRepository;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.seguridad.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InformeService {

    private static final int LIMITE_POR_DEFECTO = 10;
    private static final int LIMITE_MAXIMO = 50;
    private static final int DIAS_POR_DEFECTO = 30;

    private final IRegistroComidaRepository registroRepository;
    private final IdentidadService identidad;
    private final InformeMapper informeMapper;

    public InformePopularesDto recetasPopulares(Integer idUsuario, LocalDate desde, LocalDate hasta, Integer limite) {
        Usuario usuario = identidad.usuarioActual(idUsuario);
        IdentidadService.exigirRol(usuario, Roles.ADMIN, Roles.CHEF);

        LocalDate fin = hasta == null ? Fechas.hoy() : hasta;
        LocalDate inicio = desde == null ? fin.minusDays(DIAS_POR_DEFECTO) : desde;
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        int n = limite == null ? LIMITE_POR_DEFECTO : limite;
        if (n < 1 || n > LIMITE_MAXIMO) {
            throw new ReglaNegocioException("El límite debe estar entre 1 y " + LIMITE_MAXIMO);
        }

        List<RecetaPopularDto> cocinadas = registroRepository.masCocinadas(inicio, fin, PageRequest.of(0, n));
        List<RecetaPopularDto> guardadas = informeMapper.populares(registroRepository
                .masGuardadas(inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay(), n));
        return new InformePopularesDto(inicio, fin, cocinadas, guardadas);
    }
}
