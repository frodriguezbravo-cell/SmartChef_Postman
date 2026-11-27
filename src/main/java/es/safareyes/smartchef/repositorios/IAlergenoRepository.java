package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.Alergeno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IAlergenoRepository extends JpaRepository<Alergeno, Short> {

    Optional<Alergeno> findByCodigo(String codigo);
}
