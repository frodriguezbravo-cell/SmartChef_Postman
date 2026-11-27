package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.Continente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IContinenteRepository extends JpaRepository<Continente, Short> {

    List<Continente> findAllByNombreContainingAndOrdenIsLessThan(String nombre, Short orden);
}
