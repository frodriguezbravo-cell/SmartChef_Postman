package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.Despensa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IDespensaRepository extends JpaRepository<Despensa, Integer> {

    List<Despensa> findByUsuarioId(Integer idUsuario);

    Optional<Despensa> findFirstByUsuarioIdOrderByIdAsc(Integer idUsuario);

    boolean existsByUsuarioIdAndNombre(Integer idUsuario, String nombre);
}
