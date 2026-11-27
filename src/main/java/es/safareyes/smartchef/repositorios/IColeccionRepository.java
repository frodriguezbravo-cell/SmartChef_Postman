package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.Coleccion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IColeccionRepository extends JpaRepository<Coleccion, Integer> {

    @EntityGraph(attributePaths = {"recetas", "recetas.dificultad"})
    List<Coleccion> findByUsuarioIdOrderByNombreAsc(Integer idUsuario);

    @EntityGraph(attributePaths = {"recetas", "recetas.dificultad"})
    Optional<Coleccion> findByUsuarioIdAndNombreIgnoreCase(Integer idUsuario, String nombre);

    boolean existsByUsuarioIdAndEsFavoritasTrue(Integer idUsuario);

    List<Coleccion> findByRecetasId(Integer idReceta);
}
