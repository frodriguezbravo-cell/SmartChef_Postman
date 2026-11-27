package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.Ingrediente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IIngredienteRepository extends JpaRepository<Ingrediente, Integer> {

    Optional<Ingrediente> findBySlug(String slug);

    List<Ingrediente> findByNombreContainingIgnoreCase(String texto);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"categoria"})
    Page<Ingrediente> findByNombreContainingIgnoreCaseAndPublicadaTrue(String texto, Pageable pageable);

    List<Ingrediente> findByCategoriaIdAndPublicadaTrue(Integer idCategoria);

    List<Ingrediente> findByAptoVeganoTrueAndPublicadaTrue();

    @Query("select i from Ingrediente i where i.publicada = true " +
            "and not exists (select a.id from i.alergenos a where a.codigo in :codigos)")
    List<Ingrediente> buscarSinAlergenos(@Param("codigos") List<String> codigos);
}
