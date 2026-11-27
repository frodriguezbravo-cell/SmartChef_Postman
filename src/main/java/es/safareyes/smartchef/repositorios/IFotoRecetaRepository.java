package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.FotoReceta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IFotoRecetaRepository extends JpaRepository<FotoReceta, Integer> {

    boolean existsByRecetaIdAndPortadaTrue(Integer idReceta);

    long countByRecetaIdAndPortadaFalse(Integer idReceta);

    Optional<FotoReceta> findByIdAndRecetaId(Integer id, Integer idReceta);
}
