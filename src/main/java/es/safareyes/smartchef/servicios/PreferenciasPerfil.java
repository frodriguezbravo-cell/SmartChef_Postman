package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.v1.PreferenciasDto;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.servicios.calculos.CalculadoraPreferencias.Preferencias;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class PreferenciasPerfil {

    private PreferenciasPerfil() {
    }

    public record Filtros(boolean vegetariana, boolean sinGluten, boolean rapida, boolean economica) {

        public boolean cumple(Preferencias p) {
            return (!vegetariana || p.vegetariana())
                    && (!sinGluten || p.sinGluten())
                    && (!rapida || p.rapida())
                    && (!economica || p.economica());
        }

        public Map<String, Object> comoMapa() {
            Map<String, Object> mapa = new LinkedHashMap<>();
            if (vegetariana) {
                mapa.put("vegetariana", true);
            }
            if (sinGluten) {
                mapa.put("sinGluten", true);
            }
            if (rapida) {
                mapa.put("rapida", true);
            }
            if (economica) {
                mapa.put("economica", true);
            }
            return mapa;
        }
    }

    public static PreferenciasDto delPerfil(Usuario usuario) {
        boolean vegetariana = usuario.getDietas().stream()
                .anyMatch(d -> "VEGETARIANA".equals(d.getCodigo()) || "VEGANA".equals(d.getCodigo()));
        boolean sinGluten = usuario.getDietas().stream().anyMatch(d -> "SIN_GLUTEN".equals(d.getCodigo()));
        return new PreferenciasDto(vegetariana, sinGluten,
                Boolean.TRUE.equals(usuario.getPrefRapida()), Boolean.TRUE.equals(usuario.getPrefEconomica()));
    }

    public static Filtros resolver(Optional<Usuario> usuario, Boolean vegetariana, Boolean sinGluten,
                                   Boolean rapida, Boolean economica) {
        PreferenciasDto perfil = usuario.map(PreferenciasPerfil::delPerfil)
                .orElse(new PreferenciasDto(false, false, false, false));
        return new Filtros(
                vegetariana != null ? vegetariana : perfil.vegetariana(),
                sinGluten != null ? sinGluten : perfil.sinGluten(),
                rapida != null ? rapida : perfil.rapida(),
                economica != null ? economica : perfil.economica());
    }
}
