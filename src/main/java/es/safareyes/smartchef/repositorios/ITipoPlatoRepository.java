package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.TipoPlato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITipoPlatoRepository extends JpaRepository<TipoPlato, Short> {

    Optional<TipoPlato> findByCodigo(String codigo);
}
