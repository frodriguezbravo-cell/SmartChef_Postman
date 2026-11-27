package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.TipoCocina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITipoCocinaRepository extends JpaRepository<TipoCocina, Short> {

    Optional<TipoCocina> findByCodigo(String codigo);
}
