package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.RecetaIngrediente;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CalculadoraPreferencias {

    public static final int MINUTOS_RAPIDA = 30;
    public static final BigDecimal COSTE_MAXIMO_RACION = new BigDecimal("2.50");
    private static final BigDecimal MIL = BigDecimal.valueOf(1000);

    private CalculadoraPreferencias() {
    }

    public record Preferencias(boolean vegetariana, boolean sinGluten, boolean rapida, boolean economica) {
    }

    public record Coste(BigDecimal porRacion, boolean completo) {
    }

    public static int tiempoTotal(Receta receta) {
        int prep = receta.getTiempoPreparacionMin() == null ? 0 : receta.getTiempoPreparacionMin();
        int coc = receta.getTiempoCoccionMin() == null ? 0 : receta.getTiempoCoccionMin();
        return prep + coc;
    }

    public static Coste coste(Receta receta) {
        BigDecimal total = BigDecimal.ZERO;
        boolean alguna = false;
        boolean completo = true;
        for (RecetaIngrediente linea : receta.getIngredientes()) {
            if (Boolean.TRUE.equals(linea.getEsOpcional())) {
                continue;
            }
            BigDecimal precioKg = linea.getIngrediente() == null ? null : linea.getIngrediente().getPrecioKg();
            if (linea.getCantidadGramos() == null || precioKg == null) {
                completo = false;
                continue;
            }
            alguna = true;
            total = total.add(linea.getCantidadGramos().multiply(precioKg).divide(MIL, 6, RoundingMode.HALF_UP));
        }
        if (!alguna || receta.getRaciones() == null || receta.getRaciones() <= 0) {
            return new Coste(null, false);
        }
        BigDecimal porRacion = total.divide(BigDecimal.valueOf(receta.getRaciones()), 6, RoundingMode.HALF_UP);
        return new Coste(porRacion.setScale(2, RoundingMode.HALF_UP), completo);
    }

    public static Preferencias calcular(Receta receta) {
        boolean vegetariana = true;
        boolean sinGluten = true;
        for (RecetaIngrediente linea : receta.getIngredientes()) {
            if (linea.getIngrediente() == null) {
                continue;
            }
            if (!Boolean.TRUE.equals(linea.getIngrediente().getAptoVegetariano())) {
                vegetariana = false;
            }
            boolean gluten = linea.getIngrediente().getAlergenos().stream()
                    .anyMatch(a -> "GLUTEN".equals(a.getCodigo()));
            if (gluten) {
                sinGluten = false;
            }
        }
        boolean rapida = tiempoTotal(receta) <= MINUTOS_RAPIDA;
        Coste coste = coste(receta);
        boolean economica = coste.completo() && coste.porRacion() != null
                && coste.porRacion().compareTo(COSTE_MAXIMO_RACION) <= 0;
        return new Preferencias(vegetariana, sinGluten, rapida, economica);
    }
}
