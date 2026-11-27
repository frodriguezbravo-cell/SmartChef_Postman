package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.ListaGuardada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IListaGuardadaRepository extends JpaRepository<ListaGuardada, Short> {

    Optional<ListaGuardada> findByCodigo(String codigo);
}
