package es.safareyes.smartchef.servicios.calculos;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Equivalencias {

    private static final BigDecimal MIL = BigDecimal.valueOf(1000);

    private Equivalencias() {
    }

    static BigDecimal factorBase(String codigo) {
        if (codigo == null) {
            return null;
        }
        return switch (codigo) {
            case "G", "ML" -> BigDecimal.ONE;
            case "KG", "L" -> MIL;
            default -> null;
        };
    }

    private static String familia(String codigo) {
        return switch (codigo) {
            case "G", "KG" -> "MASA";
            case "ML", "L" -> "VOLUMEN";
            default -> codigo;
        };
    }

    public static BigDecimal convertir(BigDecimal cantidad, String desde, String hacia) {
        if (cantidad == null || desde == null || hacia == null) {
            return null;
        }
        if (desde.equals(hacia)) {
            return cantidad;
        }
        BigDecimal fd = factorBase(desde);
        BigDecimal fh = factorBase(hacia);
        if (fd == null || fh == null || !familia(desde).equals(familia(hacia))) {
            return null;
        }
        return cantidad.multiply(fd).divide(fh, 6, RoundingMode.HALF_UP);
    }

    public static BigDecimal aGramos(BigDecimal cantidad, String unidad) {
        BigDecimal f = factorBase(unidad);
        return cantidad == null || f == null ? null : cantidad.multiply(f).setScale(2, RoundingMode.HALF_UP);
    }
}
