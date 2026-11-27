package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.RecetaIngrediente;
import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CalculadoraCompra {

    private CalculadoraCompra() {
    }

    public record Faltante(Ingrediente ingrediente, UnidadMedida unidad, BigDecimal cantidad) {
    }

    private static final class Necesidad {
        private final Ingrediente ingrediente;
        private final UnidadMedida unidad;
        private BigDecimal cantidad;

        private Necesidad(Ingrediente ingrediente, UnidadMedida unidad) {
            this.ingrediente = ingrediente;
            this.unidad = unidad;
        }
    }

    public static List<Faltante> faltantes(List<RecetaIngrediente> lineas, BigDecimal factor,
                                           List<DespensaItem> despensa) {
        Map<String, Necesidad> necesidades = new LinkedHashMap<>();
        for (RecetaIngrediente linea : lineas) {
            if (Boolean.TRUE.equals(linea.getEsOpcional())) {
                continue;
            }
            String clave = linea.getIngrediente().getId() + "|" + idUnidad(linea.getUnidad());
            Necesidad n = necesidades.computeIfAbsent(clave,
                    k -> new Necesidad(linea.getIngrediente(), linea.getUnidad()));
            if (linea.getCantidad() != null) {
                BigDecimal escalada = linea.getCantidad().multiply(factor).setScale(2, RoundingMode.HALF_UP);
                n.cantidad = n.cantidad == null ? escalada : n.cantidad.add(escalada);
            }
        }

        List<Faltante> faltantes = new ArrayList<>();
        for (Necesidad n : necesidades.values()) {
            List<DespensaItem> delIngrediente = new ArrayList<>();
            for (DespensaItem item : despensa) {
                if (item.getFechaBaja() == null
                        && Objects.equals(item.getIngrediente().getId(), n.ingrediente.getId())) {
                    delIngrediente.add(item);
                }
            }
            if (delIngrediente.isEmpty()) {
                faltantes.add(new Faltante(n.ingrediente, n.unidad, n.cantidad));
                continue;
            }
            if (n.cantidad == null) {
                continue;
            }
            BigDecimal disponible = BigDecimal.ZERO;
            boolean hayMismaUnidad = false;
            for (DespensaItem item : delIngrediente) {
                if (Objects.equals(idUnidad(item.getUnidad()), idUnidad(n.unidad))) {
                    hayMismaUnidad = true;
                    disponible = disponible.add(item.getCantidad());
                }
            }
            if (!hayMismaUnidad) {
                continue;
            }
            BigDecimal falta = n.cantidad.subtract(disponible);
            if (falta.signum() > 0) {
                faltantes.add(new Faltante(n.ingrediente, n.unidad, falta));
            }
        }
        return faltantes;
    }

    private static Short idUnidad(UnidadMedida unidad) {
        return unidad == null ? null : unidad.getId();
    }
}
