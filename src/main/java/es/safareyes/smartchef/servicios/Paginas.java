package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Map;

public final class Paginas {

    public static final int TAMANO_POR_DEFECTO = 10;
    public static final int TAMANO_MAXIMO = 50;

    private Paginas() {
    }

    public static PageRequest de(Integer pagina, Integer tamano, String orden,
                                 Map<String, String> permitidos, String porDefecto) {
        int p = pagina == null ? 0 : pagina;
        int t = tamano == null ? TAMANO_POR_DEFECTO : tamano;
        if (p < 0) {
            throw new ReglaNegocioException("La página no puede ser negativa");
        }
        if (t < 1 || t > TAMANO_MAXIMO) {
            throw new ReglaNegocioException("El tamaño de página debe estar entre 1 y " + TAMANO_MAXIMO);
        }
        String propiedad = porDefecto;
        Sort.Direction direccion = Sort.Direction.ASC;
        if (orden != null && !orden.isBlank()) {
            String[] partes = orden.split(",");
            String campo = partes[0].trim();
            propiedad = permitidos.get(campo);
            if (propiedad == null) {
                throw new ReglaNegocioException("No se puede ordenar por '" + campo + "'. Campos válidos: "
                        + String.join(", ", permitidos.keySet()));
            }
            if (partes.length > 1) {
                String sentido = partes[1].trim();
                if (sentido.equalsIgnoreCase("desc")) {
                    direccion = Sort.Direction.DESC;
                } else if (!sentido.equalsIgnoreCase("asc")) {
                    throw new ReglaNegocioException("El sentido del orden debe ser asc o desc");
                }
            }
        }
        Sort sort = Sort.by(direccion, propiedad).and(Sort.by(Sort.Direction.ASC, "id"));
        return PageRequest.of(p, t, sort);
    }
}
