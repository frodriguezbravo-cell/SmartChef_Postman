package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.v1.IngredienteCatalogoDto;
import es.safareyes.smartchef.dtos.v1.PaginaDto;
import es.safareyes.smartchef.mapeadores.IngredienteMapper;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.repositorios.IIngredienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredienteCatalogoService {

    private static final Map<String, String> ORDEN = Map.of(
            "nombre", "nombre",
            "calorias", "caloriasKcal",
            "precio", "precioKg");

    private final IIngredienteRepository ingredienteRepository;
    private final IngredienteMapper ingredienteMapper;

    public PaginaDto<IngredienteCatalogoDto> buscar(String texto, Integer pagina, Integer tamano, String orden) {
        PageRequest pageable = Paginas.de(pagina, tamano, orden, ORDEN, "nombre");
        String buscado = texto == null ? "" : texto.trim();
        Page<Ingrediente> resultado = ingredienteRepository
                .findByNombreContainingIgnoreCaseAndPublicadaTrue(buscado, pageable);

        Map<String, Object> filtros = new LinkedHashMap<>();
        if (!buscado.isEmpty()) {
            filtros.put("texto", buscado);
        }
        return new PaginaDto<>(ingredienteMapper.ingredientes(resultado.getContent()),
                resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(),
                resultado.getTotalPages(), filtros);
    }
}
