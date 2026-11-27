package es.safareyes.smartchef.dtos.v1;

import java.math.BigDecimal;
import java.util.List;

public record RecetaCompletaDto(
        Integer id,
        String titulo,
        String slug,
        String descripcion,
        String imagenUrl,
        String tipoPlato,
        String tipoCocina,
        String dificultad,
        Short raciones,
        Short tiempoPreparacionMin,
        Short tiempoCoccionMin,
        Integer tiempoTotalMin,
        String autor,
        boolean retirada,
        BigDecimal costePorRacion,
        boolean costeCompleto,
        BigDecimal caloriasPorRacion,
        boolean caloriasCompletas,
        PreferenciasDto preferencias,
        List<LineaRecetaDto> ingredientes,
        List<PasoRecetaDto> pasos,
        List<FotoDto> fotos) {
}
