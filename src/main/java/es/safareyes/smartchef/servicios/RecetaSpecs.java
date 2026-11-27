package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.modelos.FotoReceta;
import es.safareyes.smartchef.modelos.Receta;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.Locale;

public final class RecetaSpecs {

    private RecetaSpecs() {
    }

    public static Specification<Receta> visibles() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.<Boolean>get("publicada")),
                cb.isFalse(root.<Boolean>get("retirada")));
    }

    public static Specification<Receta> conPortada() {
        return (root, query, cb) -> {
            Subquery<Integer> portada = query.subquery(Integer.class);
            Root<FotoReceta> foto = portada.from(FotoReceta.class);
            portada.select(foto.<Integer>get("id"))
                    .where(cb.equal(foto.get("receta"), root), cb.isTrue(foto.<Boolean>get("portada")));
            return cb.exists(portada);
        };
    }

    public static Specification<Receta> tituloContiene(String texto) {
        return (root, query, cb) -> {
            String patron = "%" + texto.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(root.<String>get("titulo")), patron);
        };
    }

    public static Specification<Receta> dificultad(String codigo) {
        return (root, query, cb) -> cb.equal(root.get("dificultad").<String>get("codigo"), codigo);
    }

    public static Specification<Receta> tiempoMaximo(int minutos) {
        return (root, query, cb) -> cb.le(tiempoTotal(root, cb), minutos);
    }

    public static Specification<Receta> idsEn(Collection<Integer> ids) {
        return (root, query, cb) -> {
            if (ids.isEmpty()) {
                return cb.disjunction();
            }
            return root.<Integer>get("id").in(ids);
        };
    }

    private static Expression<Integer> tiempoTotal(Root<Receta> root, jakarta.persistence.criteria.CriteriaBuilder cb) {
        Expression<Integer> preparacion = cb.coalesce(root.<Short>get("tiempoPreparacionMin"), (short) 0)
                .as(Integer.class);
        Expression<Integer> coccion = cb.coalesce(root.<Short>get("tiempoCoccionMin"), (short) 0)
                .as(Integer.class);
        return cb.sum(preparacion, coccion);
    }

    public static Specification<Receta> y(Specification<Receta> base, Specification<Receta> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
