package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.RecetaIngrediente;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CalculadoraDescuento {

    private CalculadoraDescuento() {
    }

    public static List<DespensaItem> descontar(List<RecetaIngrediente> lineas, BigDecimal factor,
                                               List<DespensaItem> productos) {
        List<DespensaItem> agotados = new ArrayList<>();
        for (RecetaIngrediente linea : lineas) {
            if (Boolean.TRUE.equals(linea.getEsOpcional()) || linea.getCantidad() == null || linea.getUnidad() == null) {
                continue;
            }
            BigDecimal pendiente = linea.getCantidad().multiply(factor);
            String unidadLinea = linea.getUnidad().getCodigo();
            for (DespensaItem producto : productos) {
                if (pendiente.signum() <= 0) {
                    break;
                }
                if (producto.getCantidad().signum() <= 0
                        || !Objects.equals(producto.getIngrediente().getId(), linea.getIngrediente().getId())) {
                    continue;
                }
                String unidadProducto = producto.getUnidad().getCodigo();
                BigDecimal pendienteEnProducto = Equivalencias.convertir(pendiente, unidadLinea, unidadProducto);
                if (pendienteEnProducto == null) {
                    continue;
                }
                BigDecimal quitar = pendienteEnProducto.min(producto.getCantidad());
                producto.setCantidad(producto.getCantidad().subtract(quitar).setScale(2, RoundingMode.HALF_UP));
                BigDecimal quitadoEnLinea = Equivalencias.convertir(quitar, unidadProducto, unidadLinea);
                pendiente = pendiente.subtract(quitadoEnLinea);
                if (producto.getCantidad().signum() <= 0 && !agotados.contains(producto)) {
                    agotados.add(producto);
                }
            }
        }
        return agotados;
    }
}
