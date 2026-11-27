package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.Dificultad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IDificultadRepository extends JpaRepository<Dificultad, Short> {

    Optional<Dificultad> findByCodigo(String codigo);
}
