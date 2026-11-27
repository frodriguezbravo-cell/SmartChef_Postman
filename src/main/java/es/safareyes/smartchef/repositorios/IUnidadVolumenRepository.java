package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.UnidadVolumen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUnidadVolumenRepository extends JpaRepository<UnidadVolumen, Short> {

    Optional<UnidadVolumen> findByCodigo(String codigo);
}
