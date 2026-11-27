package es.safareyes.smartchef.servicios;

import java.text.Normalizer;
import java.util.Locale;

public final class Textos {

    private Textos() {
    }

    public static String slug(String texto) {
        if (texto == null) {
            return "receta";
        }
        String s = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (s.length() > 190) {
            s = s.substring(0, 190).replaceAll("-+$", "");
        }
        return s.isEmpty() ? "receta" : s;
    }
}
